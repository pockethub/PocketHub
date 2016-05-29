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
package com.github.pockethub.ui.ref;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.alorma.github.sdk.bean.dto.response.GitBlob;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.core.code.RefreshBlobTask;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.ui.BaseActivity;
import com.github.pockethub.ui.MarkdownLoader;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.MarkdownUtils;
import com.github.pockethub.util.PreferenceUtils;
import com.github.pockethub.util.ShareUtils;
import com.github.pockethub.util.SourceEditor;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

import static com.github.pockethub.Intents.EXTRA_BASE;
import static com.github.pockethub.Intents.EXTRA_HEAD;
import static com.github.pockethub.Intents.EXTRA_PATH;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.util.PreferenceUtils.RENDER_MARKDOWN;
import static com.github.pockethub.util.PreferenceUtils.WRAP;

/**
 * Activity to view a file on a branch
 */
public class BranchFileViewActivity extends BaseActivity implements
    LoaderCallbacks<CharSequence> {

    private static final String TAG = "BranchFileViewActivity";

    private static final String ARG_TEXT = "text";

    private static final String ARG_REPO = "repo";

    /**
     * Create intent to show file in commit
     *
     * @param repository
     * @param branch
     * @param file
     * @param blobSha
     * @return intent
     */
    public static Intent createIntent(Repo repository, String branch,
        String file, String blobSha) {
        Builder builder = new Builder("branch.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, blobSha);
        builder.add(EXTRA_PATH, file);
        builder.add(EXTRA_HEAD, branch);
        return builder.toIntent();
    }

    private Repo repo;

    private String sha;

    private String path;

    private String file;

    private String branch;

    private boolean isMarkdownFile;

    private String renderedMarkdown;

    private GitBlob blob;

    private ProgressBar loadingBar;

    private WebView codeView;

    private SourceEditor editor;

    private MenuItem markdownItem;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private HttpImageGetter imageGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_commit_file_view);

        repo = getParcelableExtra(EXTRA_REPOSITORY);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);
        branch = getStringExtra(EXTRA_HEAD);

        loadingBar = finder.find(R.id.pb_loading);
        codeView = finder.find(R.id.wv_code);

        codeView.getSettings().setBuiltInZoomControls(true);
        codeView.getSettings().setUseWideViewPort(true);

        file = CommitUtils.getName(path);
        isMarkdownFile = MarkdownUtils.isMarkdown(file);
        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
            WRAP, false));

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file);
        actionBar.setSubtitle(branch);
        avatars.bind(actionBar, repo.owner);

        loadContent();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu optionsMenu) {
        getMenuInflater().inflate(R.menu.activity_file_view, optionsMenu);

        MenuItem wrapItem = optionsMenu.findItem(R.id.m_wrap);
        if (PreferenceUtils.getCodePreferences(this).getBoolean(WRAP, false))
            wrapItem.setTitle(R.string.disable_wrapping);
        else
            wrapItem.setTitle(R.string.enable_wrapping);

        markdownItem = optionsMenu.findItem(R.id.m_render_markdown);
        if (isMarkdownFile) {
            markdownItem.setEnabled(blob != null);
            markdownItem.setVisible(true);
            if (PreferenceUtils.getCodePreferences(this).getBoolean(
                RENDER_MARKDOWN, true))
                markdownItem.setTitle(R.string.show_raw_markdown);
            else
                markdownItem.setTitle(R.string.render_markdown);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_wrap:
                if (editor.getWrap())
                    item.setTitle(R.string.enable_wrapping);
                else
                    item.setTitle(R.string.disable_wrapping);
                editor.toggleWrap();
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(WRAP, editor.getWrap()));
                return true;

            case R.id.m_share:
                shareFile();
                return true;

            case R.id.m_render_markdown:
                if (editor.isMarkdown()) {
                    item.setTitle(R.string.render_markdown);
                    editor.toggleMarkdown();
                    editor.setSource(file, blob);
                } else {
                    item.setTitle(R.string.show_raw_markdown);
                    editor.toggleMarkdown();
                    if (renderedMarkdown != null)
                        editor.setSource(file, renderedMarkdown, false);
                    else
                        loadMarkdown();
                }
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(RENDER_MARKDOWN, editor.isMarkdown()));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<CharSequence> onCreateLoader(int loader, Bundle args) {
        final String raw = args.getString(ARG_TEXT);
        final Repo repo = (Repo) args
            .getParcelable(ARG_REPO);
        return new MarkdownLoader(this, repo, raw, imageGetter, false);
    }

    @Override
    public void onLoadFinished(Loader<CharSequence> loader,
        CharSequence rendered) {
        if (rendered == null)
            ToastUtils.show(this, R.string.error_rendering_markdown);

        ViewUtils.setGone(loadingBar, true);
        ViewUtils.setGone(codeView, false);

        if (!TextUtils.isEmpty(rendered)) {
            renderedMarkdown = rendered.toString();
            if (markdownItem != null)
                markdownItem.setEnabled(true);
            editor.setMarkdown(true).setSource(file, renderedMarkdown, false);
        }
    }

    @Override
    public void onLoaderReset(Loader<CharSequence> loader) {
    }

    private void shareFile() {
        String id = InfoUtils.createRepoId(repo);
        startActivity(ShareUtils.create(path + " at " + branch + " on " + id,
            "https://github.com/" + id + "/blob/" + branch + '/' + path));
    }

    private void loadMarkdown() {
        ViewUtils.setGone(loadingBar, false);
        ViewUtils.setGone(codeView, true);

        String markdown = new String(Base64.decode(blob.content, Base64.DEFAULT));
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TEXT, markdown);
        args.putParcelable(ARG_REPO, repo);
        getSupportLoaderManager().restartLoader(0, args, this);
    }

    private void loadContent() {
        ViewUtils.setGone(loadingBar, false);
        ViewUtils.setGone(codeView, true);

        new RefreshBlobTask(repo, sha, this) {

            @Override
            protected void onSuccess(GitBlob blob) throws Exception {
                super.onSuccess(blob);

                BranchFileViewActivity.this.blob = blob;

                if (markdownItem != null)
                    markdownItem.setEnabled(true);

                if (isMarkdownFile
                    && PreferenceUtils.getCodePreferences(
                    BranchFileViewActivity.this).getBoolean(
                    RENDER_MARKDOWN, true))
                    loadMarkdown();
                else {
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
                    R.string.error_file_load);
            }
        }.execute();
    }

}
