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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.core.commit.RefreshCommitTaskFactory;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.ui.item.LoadingItem;
import com.github.pockethub.android.ui.item.commit.CommitCommentItem;
import com.github.pockethub.android.ui.item.TextItem;
import com.github.pockethub.android.ui.item.commit.CommitFileHeaderItem;
import com.github.pockethub.android.ui.item.commit.CommitFileLineItem;
import com.github.pockethub.android.ui.item.commit.CommitHeaderItem;
import com.github.pockethub.android.ui.item.commit.CommitParentItem;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitStore;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.core.commit.FullCommitFile;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.Section;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.Intents.EXTRA_BASE;
import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.RequestCodes.COMMENT_CREATE;

/**
 * Fragment to display commit details with diff output
 */
public class CommitDiffListFragment extends BaseFragment implements OnItemClickListener {


    @BindView(R.id.list)
    protected RecyclerView list;

    @BindView(R.id.pb_loading)
    protected ProgressBar progress;

    private DiffStyler diffStyler;

    private Repository repository;

    private String base;

    private Commit commit;

    private List<GitComment> comments;

    private List<FullCommitFile> files;

    @Inject
    protected AvatarLoader avatars;

    @Inject
    protected CommitStore store;

    private GroupAdapter adapter = new GroupAdapter();

    private Section mainSection = new Section();

    private Section commitSection = new Section();

    private Section filesSection = new Section();

    private Section commentSection = new Section();

    @Inject
    protected RefreshCommitTaskFactory refreshCommitTaskFactory;

    @Inject
    protected HttpImageGetter commentImageGetter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        base = args.getString(EXTRA_BASE);
        repository = args.getParcelable(EXTRA_REPOSITORY);

        mainSection.add(commitSection);
        mainSection.add(filesSection);
        mainSection.add(commentSection);

        adapter.add(mainSection);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commit = store.getCommit(repository, base);

        if (files == null
                || (commit != null && commit.commit().commentCount() > 0 && comments == null)) {
            mainSection.setFooter(new LoadingItem(R.string.loading_files_and_comments));
        }

        if (commit != null && comments != null && files != null) {
            updateList(commit, comments, files);
        } else {
            if (commit != null) {
                updateHeader(commit);
            }
            refreshCommit();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        diffStyler = new DiffStyler(getResources());

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commit_diff_list, container, false);
    }

    private void addComment(final GitComment comment) {
        if (comments != null && files != null) {
            comments.add(comment);
            GitCommit rawCommit = commit.commit();
            if (rawCommit != null) {
                rawCommit = rawCommit.toBuilder()
                        .commentCount(rawCommit.commentCount() + 1)
                        .build();

                commit = commit.toBuilder()
                        .commit(rawCommit)
                        .build();
            }
            commentImageGetter.encode(comment, comment.bodyHtml());
            updateItems(comments, files);
        } else {
            refreshCommit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && COMMENT_CREATE == requestCode
                && data != null) {
            GitComment comment = data.getParcelableExtra(EXTRA_COMMENT);
            addComment(comment);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_commit_view, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isAdded()) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.m_refresh:
                refreshCommit();
                return true;
            case R.id.m_copy_hash:
                copyHashToClipboard();
                return true;
            case R.id.m_comment:
                startActivityForResult(
                        CreateCommentActivity.createIntent(repository, base),
                        COMMENT_CREATE);
                return true;
            case R.id.m_share:
                shareCommit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void copyHashToClipboard() {
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("hash", commit.sha());
        manager.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.toast_msg_copied, Toast.LENGTH_SHORT).show();
    }

    private void shareCommit() {
        String id = InfoUtils.createRepoId(repository);
        startActivity(ShareUtils.create(
                "Commit " + CommitUtils.abbreviate(base) + " on " + id,
                "https://github.com/" + id + "/commit/" + base));
    }

    private void refreshCommit() {
        refreshCommitTaskFactory.create(getActivity(), repository, base)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(full -> {
                    List<GitHubFile> files = full.getCommit().files();
                    diffStyler.setFiles(files);
                    if (files != null) {
                        Collections.sort(files, new CommitFileComparator());
                    }

                    updateList(full.getCommit(), full, full.getFiles());
                }, e -> {
                    ToastUtils.show(getActivity(), R.string.error_commit_load);
                    progress.setVisibility(View.GONE);
                });
    }

    private void addCommitParents(Commit commit) {
        List<Commit> parents = commit.parents();
        if (parents == null || parents.isEmpty()) {
            return;
        }

        List<CommitParentItem> items = new ArrayList<>();
        for (Commit parent : parents) {
            items.add(new CommitParentItem(getActivity(), parent));
        }
        commitSection.update(items);
    }

    private void updateHeader(Commit commit) {
        progress.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        mainSection.setHeader(new CommitHeaderItem(avatars, getActivity(), commit));
        addCommitParents(commit);
    }

    private void updateList(Commit commit, List<GitComment> comments, List<FullCommitFile> files) {
        if (!isAdded()) {
            return;
        }

        this.commit = commit;
        this.comments = comments;
        this.files = files;

        updateHeader(commit);
        mainSection.removeFooter();

        filesSection.setHeader(
                new TextItem(R.layout.commit_file_details_header,
                        R.id.tv_commit_file_summary, CommitUtils.formatStats(commit.files()))
        );
        updateItems(comments, files);
    }

    private void updateItems(List<GitComment> comments, List<FullCommitFile> files) {
        filesSection.update(createFileSections(files));

        List<CommitCommentItem> items = new ArrayList<>();
        for (GitComment comment : comments) {
            items.add(new CommitCommentItem(avatars, commentImageGetter, comment));
        }
        commentSection.update(items);
    }

    private List<Section> createFileSections(List<FullCommitFile> files) {
        List<Section> sections = new ArrayList<>();
        for (FullCommitFile file : files) {
            Section section = new Section(new CommitFileHeaderItem(getActivity(), file.getFile()));
            List<CharSequence> lines = diffStyler.get(file.getFile().filename());
            int number = 0;
            for (CharSequence line : lines) {
                section.add(new CommitFileLineItem(diffStyler, line));
                for (GitComment comment : file.get(number)) {
                    section.add(new CommitCommentItem(avatars, commentImageGetter, comment, true));
                }
                number++;
            }

            sections.add(section);
        }

        return sections;
    }

    private void showFileOptions(CharSequence line, final int position, final GitHubFile file) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(CommitUtils.getName(file));

        final MaterialDialog[] dialogHolder = new MaterialDialog[1];

        View view = getActivity().getLayoutInflater().inflate(
                R.layout.diff_line_dialog, null);

        TextView diff = (TextView) view.findViewById(R.id.tv_diff);
        diff.setText(line);
        diffStyler.updateColors(line, diff);

        ((TextView) view.findViewById(R.id.tv_commit)).setText(getString(R.string.commit_prefix)
                + CommitUtils.abbreviate(commit));

        view.findViewById(R.id.ll_view_area).setOnClickListener(v -> {
            dialogHolder[0].dismiss();
            openFile(file);
        });

        view.findViewById(R.id.ll_comment_area).setOnClickListener(v -> {
            dialogHolder[0].dismiss();
            startActivityForResult(CreateCommentActivity
                            .createIntent(repository, commit.sha(),
                                    file.filename(), position),
                    COMMENT_CREATE);
        });

        builder.customView(view, false)
                .negativeText(R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss());

        MaterialDialog dialog = builder.build();
        dialogHolder[0] = dialog;
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void openFile(GitHubFile file) {
        if (!TextUtils.isEmpty(file.filename())
                && !TextUtils.isEmpty(file.sha())) {
            startActivity(CommitFileViewActivity.createIntent(repository, base,
                    file));
        }
    }

    /**
     * Select previous file by scanning backwards from the current position
     *
     * @param position
     * @param item
     * @param adapter
     */
    private void selectPreviousFile(int position, Object item, GroupAdapter adapter) {
        CharSequence line;
        if (item instanceof CharSequence) {
            line = (CharSequence) item;
        } else {
            line = null;
        }

        int linePosition = 0;
        while (--position >= 0) {
            item = adapter.getItem(position);

            if (item instanceof CommitFileHeaderItem) {
                if (line != null) {
                    showFileOptions(line, linePosition, ((CommitFileHeaderItem) item).getFile());
                }
                break;
            } else if (item instanceof CharSequence) {
                if (line != null) {
                    linePosition++;
                } else {
                    line = (CharSequence) item;
                }
            }
        }
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        int position = adapter.getAdapterPosition(item);

        if (item instanceof CommitParentItem) {
            String sha = ((CommitParentItem) item).getCommit().sha();
            startActivity(CommitViewActivity.createIntent(repository, sha));
        } else if (item instanceof CommitFileHeaderItem) {
            openFile(((CommitFileHeaderItem) item).getFile());
        } else if (item instanceof CharSequence) {
            selectPreviousFile(position, item, adapter);
        } else if (item instanceof CommitCommentItem) {
            if (!TextUtils.isEmpty(((CommitCommentItem) item).getComment().path())) {
                selectPreviousFile(position, item, adapter);
            }
        }
    }
}
