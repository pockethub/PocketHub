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
package com.github.pockethub.android.ui.ref;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import androidx.appcompat.app.ActionBar;
import butterknife.BindView;
import com.github.pockethub.android.Intents.*;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.MarkdownLoader;
import com.github.pockethub.android.util.*;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitBlob;
import com.meisolsson.githubsdk.service.git.GitService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Inject;

import static com.github.pockethub.android.Intents.*;
import static com.github.pockethub.android.util.PreferenceUtils.RENDER_MARKDOWN;
import static com.github.pockethub.android.util.PreferenceUtils.WRAP;

/**
 * Activity to view a file on a branch
 */
public class BranchFileViewActivity extends BaseActivity {

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
    public static Intent createIntent(Repository repository, String branch,
                                      String file, String blobSha) {
        Builder builder = new Builder("branch.file.VIEW");
        builder.repo(repository);
        builder.add(EXTRA_BASE, blobSha);
        builder.add(EXTRA_PATH, file);
        builder.add(EXTRA_HEAD, branch);
        return builder.toIntent();
    }

    @BindView(R.id.pb_loading)
    protected ProgressBar loadingBar;

    @BindView(R.id.wv_code)
    protected WebView codeView;

    private Repository repo;

    private String sha;

    private String path;

    private String file;

    private String branch;

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

        repo = getParcelableExtra(EXTRA_REPOSITORY);
        sha = getStringExtra(EXTRA_BASE);
        path = getStringExtra(EXTRA_PATH);
        branch = getStringExtra(EXTRA_HEAD);

        codeView.getSettings().setBuiltInZoomControls(true);
        codeView.getSettings().setUseWideViewPort(true);

        file = CommitUtils.getName(path);
        isMarkdownFile = MarkdownUtils.isMarkdown(file);
        editor = new SourceEditor(codeView);
        editor.setWrap(PreferenceUtils.getCodePreferences(this).getBoolean(
                WRAP, false));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file);
        actionBar.setSubtitle(branch);
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
        startActivity(ShareUtils.create(path + " at " + branch + " on " + id,
                "https://github.com/" + id + "/blob/" + branch + '/' + path));
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
                }, e -> ToastUtils.show(this, R.string.error_rendering_markdown));
    }

    private void loadContent() {
        loadingBar.setVisibility(View.VISIBLE);
        codeView.setVisibility(View.GONE);

        ServiceGenerator.createService(this, GitService.class)
                .getGitBlob(repo.owner().login(), repo.name(), sha)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> {
                    blob = response.body();

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

                        editor.setMarkdown(false).setSource(file, blob);
                    }
                }, e -> {
                    Log.d(TAG, "Loading file contents failed", e);

                    loadingBar.setVisibility(View.GONE);
                    codeView.setVisibility(View.VISIBLE);
                    ToastUtils.show(this, R.string.error_file_load);
                });
    }

}
