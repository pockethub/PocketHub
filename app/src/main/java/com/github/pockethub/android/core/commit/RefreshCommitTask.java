/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.core.commit;

import android.app.Activity;
import android.content.Context;

import com.github.pockethub.android.util.HttpImageGetter;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

/**
 * Task to load a commit by SHA-1 id
 */
public class RefreshCommitTask implements Observable.OnSubscribe<FullCommit> {

    private final Context context;

    @Inject
    private CommitStore store;

    private final Repository repository;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * @param repository
     * @param id
     * @param imageGetter
     */
    public RefreshCommitTask(Activity activity, Repository repository,
                             String id, HttpImageGetter imageGetter) {

        this.repository = repository;
        this.id = id;
        this.imageGetter = imageGetter;
        this.context = activity;
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super FullCommit> subscriber) {
        try {
            Commit commit = store.refreshCommit(repository, id);
            GitCommit rawCommit = commit.commit();
            if (rawCommit != null && rawCommit.commentCount() > 0) {
                List<GitComment> comments = ServiceGenerator.createService(context, RepositoryCommentService.class)
                        .getCommitComments(repository.owner().login(), repository.name(), commit.sha(), 1)
                        .toBlocking()
                        .first()
                        .items();

                for (GitComment comment : comments) {
                    imageGetter.encode(comment, comment.bodyHtml());
                }
                subscriber.onNext(new FullCommit(commit, comments));
            } else
                subscriber.onNext(new FullCommit(commit));
        }catch (IOException e){
            subscriber.onError(e);
        }
    }
}
