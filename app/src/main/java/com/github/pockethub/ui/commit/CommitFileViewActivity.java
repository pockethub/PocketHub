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
package com.github.pockethub.ui.commit;

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

import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.alorma.github.sdk.bean.dto.response.GitBlob;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.git.GetGitBlobClient;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.rx.ObserverAdapter;
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

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.Intents.EXTRA_BASE;
import static com.github.pockethub.Intents.EXTRA_HEAD;
import static com.github.pockethub.Intents.EXTRA_PATH;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.util.PreferenceUtils.RENDER_MARKDOWN;
import static com.github.pockethub.util.PreferenceUtils.WRAP;

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
    public static Intent createIntent(Repo repository, String commit,
        CommitFile file) {
        Builder builder = new Builder("commit.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_HEAD, commit);
        builder.add(EXTRA_PATH, file.filename);
        builder.add(EXTRA_BASE, file.sha);
        return builder.toIntent();
    }

    private Repo repo;

    private String commit;

    private String sha;

    private String path;

    private String file;

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

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        repo = getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        commit = getStringExtra(EXTRA_HEAD);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);

        loadingBar = finder.find(R.id.pb_loading);
        codeView = finder.find(R.id.wv_code);

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
        actionBar.setSubtitle(getString(R.string.commit_prefix)
            + CommitUtils.abbreviate(commit));
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
        final Repo repo = args.getParcelable(ARG_REPO);
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
        startActivity(ShareUtils.create(
            path + " at " + CommitUtils.abbreviate(commit) + " on " + id,
            "https://github.com/" + id + "/blob/" + commit + '/' + path));
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
        new GetGitBlobClient(InfoUtils.createCommitInfo(repo, sha))
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GitBlob>bindToLifecycle())
                .subscribe(new ObserverAdapter<GitBlob>() {
                    @Override
                    public void onNext(GitBlob gitBlob) {
                        ViewUtils.setGone(loadingBar, true);
                        ViewUtils.setGone(codeView, false);

                        editor.setSource(path, gitBlob);
                        CommitFileViewActivity.this.blob = gitBlob;

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
                            editor.setSource(path, gitBlob);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "Loading commit file contents failed", error);

                        ViewUtils.setGone(loadingBar, true);
                        ViewUtils.setGone(codeView, false);
                        ToastUtils.show(CommitFileViewActivity.this, error,
                                R.string.error_file_load);
                    }
                });
    }

}
