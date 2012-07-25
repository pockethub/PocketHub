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
package com.github.mobile.ui.commit;

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_PATH;
import static com.github.mobile.Intents.EXTRA_RAW_URL;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.string;
import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpRequestUtils;
import com.github.mobile.util.SourceEditor;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to display the contents of a file in a commit
 */
public class CommitFileViewActivity extends RoboSherlockActivity {

    private static final String TAG = "CommitFileViewActivity";

    /**
     * Create intent to show file in commit
     *
     * @param repository
     * @param commit
     * @param file
     * @return intent
     */
    public static Intent createIntent(Repository repository, String commit,
            CommitFile file) {
        Builder builder = new Builder("commit.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, commit);
        builder.add(EXTRA_PATH, file.getFilename());
        builder.add(EXTRA_RAW_URL, file.getRawUrl());
        return builder.toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @InjectExtra(EXTRA_BASE)
    private String commit;

    @InjectExtra(EXTRA_PATH)
    private String path;

    @InjectExtra(EXTRA_RAW_URL)
    private String url;

    @InjectView(id.pb_loading)
    private ProgressBar loadingBar;

    @InjectView(id.wv_code)
    private WebView codeView;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.commit_file_view);

        ActionBar actionBar = getSupportActionBar();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1)
            actionBar.setTitle(path.substring(lastSlash + 1));
        else
            actionBar.setTitle(path);
        actionBar.setSubtitle(getString(string.commit_prefix)
                + CommitUtils.abbreviate(commit));
        avatars.bind(actionBar, repo.getOwner());

        loadContent();
    }

    private void loadContent() {
        new AuthenticatedUserTask<String>(this) {

            @Override
            protected String run(Account account) throws Exception {
                HttpRequest request = HttpRequest.get(url);
                if (HttpRequestUtils.isSecure(request)) {
                    String password = AccountManager.get(
                            CommitFileViewActivity.this).getPassword(account);
                    if (!TextUtils.isEmpty(password))
                        request.basic(account.name, password);
                }
                return request.ok() ? request.body() : null;
            }

            @Override
            protected void onSuccess(String body) throws Exception {
                super.onSuccess(body);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);
                SourceEditor.showSource(codeView, path, body);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Loading commit file contents failed", e);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);
                ToastUtils.show(CommitFileViewActivity.this, e,
                        string.error_file_load);
            }
        }.execute();
    }
}
