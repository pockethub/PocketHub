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
package com.github.mobile.ui.ref;

import static com.github.mobile.Intents.EXTRA_BASE;
import static com.github.mobile.Intents.EXTRA_HEAD;
import static com.github.mobile.Intents.EXTRA_PATH;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.util.PreferenceUtils.WRAP;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.github.mobile.ui.BaseActivity;
import com.github.mobile.ui.MarkdownLoader;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.PreferenceUtils;
import com.github.mobile.util.ShareUtils;
import com.github.mobile.util.SourceEditor;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import java.io.Serializable;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.util.EncodingUtils;

/**
 * Activity to view a file on a branch
 */
public class BranchFileViewActivity extends BaseActivity implements
    LoaderManager.LoaderCallbacks<CharSequence> {

    private static final String TAG = "BranchFileViewActivity";

    private static final String ARG_TEXT = "text";

    private static final String ARG_REPO = "repo";

    private static final String[] MARKDOWN_EXTENSIONS =
            {"md", "mkdn", "mdwn", "mdown", "markdown"};

    /**
     * Create intent to show file in commit
     *
     * @param repository
     * @param branch
     * @param file
     * @param blobSha
     * @return intent
     */
    public static Intent createIntent(Repository repository, String branch,
            String file, String blobSha) {
        Builder builder = new Builder("branch.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, blobSha);
        builder.add(EXTRA_PATH, file);
        builder.add(EXTRA_HEAD, branch);
        return builder.toIntent();
    }

    private Repository repo;

    private String sha;

    private String path;

    private String file;

    private String branch;

    private ProgressBar loadingBar;

    private WebView codeView;

    private SourceEditor editor;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private HttpImageGetter imageGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.commit_file_view);

        repo = getSerializableExtra(EXTRA_REPOSITORY);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);
        branch = getStringExtra(EXTRA_HEAD);

        loadingBar = finder.find(id.pb_loading);
        codeView = finder.find(id.wv_code);

        file = CommitUtils.getName(path);
        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
                WRAP, false));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file);
        actionBar.setSubtitle(branch);
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

    @Override
    public Loader<CharSequence> onCreateLoader(int loader, Bundle args) {
        final String raw = args.getString(ARG_TEXT);
        final IRepositoryIdProvider repo = (IRepositoryIdProvider) args
            .getSerializable(ARG_REPO);
        return new MarkdownLoader(this, repo, raw, imageGetter, false);
    }

    @Override
    public void onLoadFinished(Loader<CharSequence> loader,
        CharSequence rendered) {
        if (rendered == null)
            ToastUtils.show(this, string.error_rendering_markdown);

        ViewUtils.setGone(loadingBar, true);
        ViewUtils.setGone(codeView, false);

        editor.setMarkdown(true).setSource(file, rendered.toString(), false);
    }

    @Override
    public void onLoaderReset(Loader<CharSequence> loader) {
    }

    private void shareFile() {
        String id = repo.generateId();
        startActivity(ShareUtils.create(path + " at " + branch + " on " + id,
                "https://github.com/" + id + "/blob/" + branch + '/' + path));
    }

    private void loadContent() {
        new RefreshBlobTask(repo, sha, this) {

            @Override
            protected void onSuccess(Blob blob) throws Exception {
                super.onSuccess(blob);

                if (isMarkdown(file)) {
                    String markdown = new String(EncodingUtils.fromBase64(blob.getContent()));
                    Bundle args = new Bundle();
                    args.putCharSequence(ARG_TEXT, markdown);
                    if (repo instanceof Serializable)
                        args.putSerializable(ARG_REPO, (Serializable) repo);
                    getSupportLoaderManager().restartLoader(0, args, BranchFileViewActivity.this);
                } else {
                    ViewUtils.setGone(loadingBar, true);
                    ViewUtils.setGone(codeView, false);

                    editor.setMarkdown(false).setSource(file, blob);
                }
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                Log.d(TAG, "Loading file contents failed", e);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);
                ToastUtils.show(BranchFileViewActivity.this, e,
                        string.error_file_load);
            }
        }.execute();
    }

    private boolean isMarkdown(String name) {
        for (int i = 0; i < MARKDOWN_EXTENSIONS.length; i++)
            if (name.endsWith(MARKDOWN_EXTENSIONS[i]))
                return true;

        return false;
    }
}
