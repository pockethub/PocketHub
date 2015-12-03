/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui.gist;

import static com.github.pockethub.Intents.EXTRA_GIST;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.alorma.github.basesdk.client.BaseClient;
import com.alorma.github.sdk.bean.dto.request.CommentRequest;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.services.gists.PublishGistCommentClient;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.util.ToastUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Activity to create a comment on a {@link Gist}
 */
public class CreateCommentActivity extends
        com.github.pockethub.ui.comment.CreateCommentActivity {

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
        actionBar.setTitle(getString(R.string.gist_title) + gist.id);
        User user = gist.user;
        if (user != null)
            actionBar.setSubtitle(user.login);
        avatars.bind(actionBar, user);
    }

    @Override
    protected void createComment(final String comment) {
        PublishGistCommentClient commentClient = new PublishGistCommentClient(this,
                gist.id, new CommentRequest(comment));
        commentClient.setOnResultCallback(new BaseClient.OnResultCallback<GithubComment>() {
            @Override
            public void onResponseOk(GithubComment githubComment, Response r) {
                finish(githubComment);
            }

            @Override
            public void onFail(RetrofitError error) {
                Log.d(TAG, "Exception creating comment on gist", error);

                ToastUtils.show(CreateCommentActivity.this, error.getMessage());
            }
        });
        commentClient.execute();
    }
}
