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
package com.github.pockethub.android.ui.gist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.request.CommentRequest;
import com.meisolsson.githubsdk.service.gists.GistCommentService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_GIST;

/**
 * Activity to create a comment on a {@link Gist}
 */
public class CreateCommentActivity extends
        com.github.pockethub.android.ui.comment.CreateCommentActivity {

    /**
     * Create intent to create a comment
     *
     * @param gist
     * @return intent
     */
    public static Intent createIntent(Gist gist) {
        Builder builder = new Builder("gist.comment.create.VIEW");
        builder.gist(gist);
        return builder.toIntent();
    }

    private Gist gist;

    private String TAG = "CreateCommentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gist = getParcelableExtra(EXTRA_GIST);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.gist_title) + gist.id());
        User user = gist.owner();
        if (user != null) {
            actionBar.setSubtitle(user.login());
        }
        avatars.bind(actionBar, user);
    }

    @Override
    protected void createComment(final String comment) {
        CommentRequest commentRequest = CommentRequest.builder()
                .body(comment)
                .build();

        ServiceGenerator.createService(this, GistCommentService.class)
                .createGistComment(gist.id(), commentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> finish(response.body()), error -> {
                    Log.e(TAG, "Exception creating comment on gist", error);

                    ToastUtils.show(this, error.getMessage());
                });
    }
}
