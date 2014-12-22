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
import static com.github.mobile.util.PreferenceUtils.RENDER_MARKDOWN;
import static com.github.mobile.util.PreferenceUtils.WRAP;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
import com.github.mobile.util.MarkdownUtils;
import com.github.mobile.util.PreferenceUtils;
import com.github.mobile.util.ShareUtils;
import com.github.mobile.util.SourceEditor;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import java.io.Serializable;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.util.EncodingUtils;

/**
 * Activity to display the contents of a file in a commit
 */
public class CommitFileViewActivity extends BaseActivity implements
        LoaderCallbacks<CharSequence> {

    private static final String TAG = "CommitFileViewActivity";

    private static final String ARG_TEXT = "text";

    private static final String ARG_REPO = "repo";

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

    private Repository repo;

    private String commit;

    private String sha;

    private String path;

    private String file;

    private boolean isMarkdownFile;

    private String renderedMarkdown;

    private Blob blob;

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

        setContentView(layout.commit_file_view);

        repo = getSerializableExtra(EXTRA_REPOSITORY);
        commit = getStringExtra(EXTRA_HEAD);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);

        loadingBar = finder.find(id.pb_loading);
        codeView = finder.find(id.wv_code);

        file = CommitUtils.getName(path);
        isMarkdownFile = MarkdownUtils.isMarkdown(file);

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

        markdownItem = optionsMenu.findItem(id.m_render_markdown);
        if (isMarkdownFile) {
            markdownItem.setEnabled(blob != null);
            markdownItem.setVisible(true);
            if (PreferenceUtils.getCodePreferences(this).getBoolean(
                    RENDER_MARKDOWN, true))
                markdownItem.setTitle(string.show_raw_markdown);
            else
                markdownItem.setTitle(string.render_markdown);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_wrap:
            if (editor.getWrap())
                item.setTitle(string.enable_wrapping);
            else
                item.setTitle(string.disable_wrapping);
            editor.toggleWrap();
            PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(WRAP, editor.getWrap()));
            return true;

        case id.m_share:
            shareFile();
            return true;

        case id.m_render_markdown:
            if (editor.isMarkdown()) {
                item.setTitle(string.render_markdown);
                editor.toggleMarkdown();
                editor.setSource(file, blob);
            } else {
                item.setTitle(string.show_raw_markdown);
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
        String id = repo.generateId();
        startActivity(ShareUtils.create(
                path + " at " + CommitUtils.abbreviate(commit) + " on " + id,
                "https://github.com/" + id + "/blob/" + commit + '/' + path));
    }

    private void loadMarkdown() {
        ViewUtils.setGone(loadingBar, false);
        ViewUtils.setGone(codeView, true);

        String markdown = new String(
                EncodingUtils.fromBase64(blob.getContent()));
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TEXT, markdown);
        args.putSerializable(ARG_REPO, repo);
        getSupportLoaderManager().restartLoader(0, args, this);
    }

    private void loadContent() {
        new RefreshBlobTask(repo, sha, this) {

            @Override
            protected void onSuccess(Blob blob) throws Exception {
                super.onSuccess(blob);

                ViewUtils.setGone(loadingBar, true);
                ViewUtils.setGone(codeView, false);

                editor.setSource(path, blob);
                CommitFileViewActivity.this.blob = blob;

                if (markdownItem != null)
                    markdownItem.setEnabled(true);

                if (isMarkdownFile
                        && PreferenceUtils.getCodePreferences(
                                CommitFileViewActivity.this).getBoolean(
                                RENDER_MARKDOWN, true))
                    loadMarkdown();
                else {
                    ViewUtils.setGone(loadingBar, true);
                    ViewUtils.setGone(codeView, false);
                    editor.setSource(path, blob);
                }
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
