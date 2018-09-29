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
package com.github.pockethub.android.ui.commit;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.MarkdownLoader;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.MarkdownUtils;
import com.github.pockethub.android.util.PreferenceUtils;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.SourceEditor;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.git.GitBlob;
import com.meisolsson.githubsdk.service.git.GitService;
import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_BASE;
import static com.github.pockethub.android.Intents.EXTRA_HEAD;
import static com.github.pockethub.android.Intents.EXTRA_PATH;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.util.PreferenceUtils.RENDER_MARKDOWN;
import static com.github.pockethub.android.util.PreferenceUtils.WRAP;

/**
 * Activity to display the contents of a file in a commit
 */
public class CommitFileViewActivity extends BaseActivity {

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
    public static Intent createIntent(Repository repository, String commit, GitHubFile file) {
        Builder builder = new Builder("commit.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_HEAD, commit);
        builder.add(EXTRA_PATH, file.filename());
        builder.add(EXTRA_BASE, file.sha());
        return builder.toIntent();
    }

    @BindView(R.id.pb_loading)
    protected ProgressBar loadingBar;

    @BindView(R.id.wv_code)
    protected WebView codeView;

    private Repository repo;

    private String commit;

    private String sha;

    private String path;

    private String file;

    private boolean isMarkdownFile;

    private String renderedMarkdown;

    private GitBlob blob;

    private SourceEditor editor;

    private MenuItem markdownItem;

    @Inject
    protected AvatarLoader avatars;

    @Inject
    protected HttpImageGetter imageGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_file_view);

        repo = getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        commit = getStringExtra(EXTRA_HEAD);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);

        file = CommitUtils.getName(path);
        isMarkdownFile = MarkdownUtils.isMarkdown(file);

        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
            WRAP, false));

        ActionBar actionBar = getSupportActionBar();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            actionBar.setTitle(path.substring(lastSlash + 1));
        } else {
            actionBar.setTitle(path);
        }
        actionBar.setSubtitle(getString(R.string.commit_prefix)
            + CommitUtils.abbreviate(commit));
        avatars.bind(actionBar, repo.owner());

        loadContent();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu optionsMenu) {
        getMenuInflater().inflate(R.menu.activity_file_view, optionsMenu);

        MenuItem wrapItem = optionsMenu.findItem(R.id.m_wrap);
        if (PreferenceUtils.getCodePreferences(this).getBoolean(WRAP, false)) {
            wrapItem.setTitle(R.string.disable_wrapping);
        } else {
            wrapItem.setTitle(R.string.enable_wrapping);
        }

        markdownItem = optionsMenu.findItem(R.id.m_render_markdown);
        if (isMarkdownFile) {
            markdownItem.setEnabled(blob != null);
            markdownItem.setVisible(true);
            if (PreferenceUtils.getCodePreferences(this).getBoolean(
                RENDER_MARKDOWN, true)) {
                markdownItem.setTitle(R.string.show_raw_markdown);
            } else {
                markdownItem.setTitle(R.string.render_markdown);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_wrap:
                if (editor.getWrap()) {
                    item.setTitle(R.string.enable_wrapping);
                } else {
                    item.setTitle(R.string.disable_wrapping);
                }
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
                    if (renderedMarkdown != null) {
                        editor.setSource(file, renderedMarkdown, false);
                    } else {
                        loadMarkdown();
                    }
                }
                PreferenceUtils.save(PreferenceUtils.getCodePreferences(this)
                    .edit().putBoolean(RENDER_MARKDOWN, editor.isMarkdown()));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareFile() {
        String id = InfoUtils.createRepoId(repo);
        startActivity(ShareUtils.create(
            path + " at " + CommitUtils.abbreviate(commit) + " on " + id,
            "https://github.com/" + id + "/blob/" + commit + '/' + path));
    }

    private void loadMarkdown() {
        loadingBar.setVisibility(View.VISIBLE);
        codeView.setVisibility(View.GONE);

        String markdown = new String(Base64.decode(blob.content(), Base64.DEFAULT));

        MarkdownLoader.load(this, markdown, repo, imageGetter, false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rendered -> {
                    loadingBar.setVisibility(View.GONE);
                    codeView.setVisibility(View.VISIBLE);

                    if (!TextUtils.isEmpty(rendered)) {
                        renderedMarkdown = rendered.toString();
                        if (markdownItem != null) {
                            markdownItem.setEnabled(true);
                        }
                        editor.setMarkdown(true).setSource(file, renderedMarkdown, false);
                    }
                } , e -> ToastUtils.show(this, R.string.error_rendering_markdown));
    }

    private void loadContent() {
        ServiceGenerator.createService(this, GitService.class)
                .getGitBlob(repo.owner().login(), repo.name(), sha)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> {
                    GitBlob gitBlob = response.body();
                    loadingBar.setVisibility(View.GONE);
                    codeView.setVisibility(View.VISIBLE);

                    editor.setSource(path, gitBlob);
                    blob = gitBlob;

                    if (markdownItem != null) {
                        markdownItem.setEnabled(true);
                    }

                    if (isMarkdownFile
                            && PreferenceUtils.getCodePreferences(this).getBoolean(
                            RENDER_MARKDOWN, true)) {
                        loadMarkdown();
                    } else {
                        loadingBar.setVisibility(View.GONE);
                        codeView.setVisibility(View.VISIBLE);
                        editor.setSource(path, gitBlob);
                    }
                }, error -> {
                    Log.e(TAG, "Loading commit file contents failed", error);

                    loadingBar.setVisibility(View.GONE);
                    codeView.setVisibility(View.VISIBLE);
                    ToastUtils.show(this, R.string.error_file_load);
                });
    }

}
