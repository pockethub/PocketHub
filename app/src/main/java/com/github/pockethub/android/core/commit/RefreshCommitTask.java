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

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.GitCommit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.commit.GetCommitCommentsClient;
import com.github.pockethub.android.util.HtmlUtils;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
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

    @Inject
    private CommitStore store;

    private final Repo repository;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * @param repository
     * @param id
     * @param imageGetter
     */
    public RefreshCommitTask(Activity activity, Repo repository,
                             String id, HttpImageGetter imageGetter) {

        this.repository = repository;
        this.id = id;
        this.imageGetter = imageGetter;
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super FullCommit> subscriber) {
        try {
            Commit commit = store.refreshCommit(repository, id);
            GitCommit rawCommit = commit.commit;
            if (rawCommit != null && rawCommit.comment_count > 0) {
                List<CommitComment> comments = new GetCommitCommentsClient(InfoUtils.createCommitInfo(repository, commit.sha))
                        .observable().toBlocking().first().first;
                for (CommitComment comment : comments) {
                    String formatted = HtmlUtils.format(comment.body_html).toString();
                    comment.body_html = formatted;
                    imageGetter.encode(comment, formatted);
                }
                subscriber.onNext(new FullCommit(commit, comments));
            } else
                subscriber.onNext(new FullCommit(commit));
        }catch (IOException e){
            subscriber.onError(e);
        }
    }
}
