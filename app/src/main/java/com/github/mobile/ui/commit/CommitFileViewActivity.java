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
import static com.github.mobile.Intents.EXTRA_HEAD;
import static com.github.mobile.Intents.EXTRA_PATH;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.util.PreferenceUtils.WRAP;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.code.RefreshBlobTask;
import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.PreferenceUtils;
import com.github.mobile.util.ShareUtils;
import com.github.mobile.util.SourceEditor;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Blob;
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
        builder.add(EXTRA_HEAD, commit);
        builder.add(EXTRA_PATH, file.getFilename());
        builder.add(EXTRA_BASE, file.getSha());
        return builder.toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @InjectExtra(EXTRA_HEAD)
    private String commit;

    @InjectExtra(EXTRA_BASE)
    private String sha;

    @InjectExtra(EXTRA_PATH)
    private String path;

    @InjectView(id.pb_loading)
    private ProgressBar loadingBar;

    @InjectView(id.wv_code)
    private WebView codeView;

    private SourceEditor editor;

    @Inject
    private AvatarLoader avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.commit_file_view);

        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
                WRAP, false));

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

    @Override
    public boolean onCreateOptionsMenu(final Menu optionsMenu) {
        getSupportMenuInflater().inflate(menu.file_view, optionsMenu);

        MenuItem wrapItem = optionsMenu.findItem(id.m_wrap);
        if (PreferenceUtils.getCodePreferences(this).getBoolean(WRAP, false))
            wrapItem.setTitle(string.disable_wrapping);
        else
            wrapItem.setTitle(string.enable_wrapping);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_wrap:
            if (editor.getWrap()) {
                item.setTitle(string.enable_wrapping);
                editor.setWrap(false);
            } else {
                item.setTitle(string.disable_wrapping);
                editor.setWrap(true);
            }
            PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(WRAP, editor.getWrap()));
            return true;
        case id.m_share:
            shareFile();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void shareFile() {
        String id = repo.generateId();
        startActivity(ShareUtils.create(
                path + " at " + CommitUtils.abbreviate(commit) + " on " + id,
                "https://github.com/" + id + "/blob/" + commit + '/' + path));
    }

    private void loadContent() {
        new RefreshBlobTask(repo, sha, this) {

            @Override
            protected void onSuccess(Blob blob) throws Exception {
                super.onSuccess(blob);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);

                editor.setSource(path, blob);
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
