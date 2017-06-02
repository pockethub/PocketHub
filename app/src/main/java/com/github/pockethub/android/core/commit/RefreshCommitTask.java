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
import com.github.pockethub.android.util.RxPageUtil;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;
import com.google.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import roboguice.RoboGuice;

/**
 * Task to load a commit by SHA-1 id
 */
public class RefreshCommitTask {

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

    /**
     * Fetches a commit with it's comments.
     *
     * @return Single for a FullCommit
     */
    public Single<FullCommit> refresh() {
        RepositoryCommentService commentService =
                ServiceGenerator.createService(context, RepositoryCommentService.class);

        return store.refreshCommit(repository, id)
                .flatMap(commit -> RxPageUtil.getAllPages((page) ->
                        commentService.getCommitComments(repository.owner().login(),
                                repository.name(), commit.sha(), page), 1)
                        .flatMap(page -> Observable.fromIterable(page.items()))
                        .map(comment -> {
                            imageGetter.encode(comment, comment.bodyHtml());
                            return comment;
                        })
                        .toList()
                        .map(comments -> new FullCommit(commit, comments)));
    }
}
