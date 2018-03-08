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
package com.github.pockethub.android.ui.code;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.code.FullTree;
import com.github.pockethub.android.core.code.FullTree.Entry;
import com.github.pockethub.android.core.code.FullTree.Folder;
import com.github.pockethub.android.core.code.RefreshTreeTask;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.DialogResultListener;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.ui.item.code.BlobItem;
import com.github.pockethub.android.ui.item.code.FolderItem;
import com.github.pockethub.android.ui.item.code.PathHeaderItem;
import com.github.pockethub.android.ui.ref.BranchFileViewActivity;
import com.github.pockethub.android.ui.ref.RefDialog;
import com.github.pockethub.android.ui.ref.RefDialogFragment;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.Section;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.RequestCodes.REF_UPDATE;

/**
 * Fragment to display a repository's source code tree
 */
public class RepositoryCodeFragment extends BaseFragment implements OnItemClickListener, DialogResultListener {

    private static final String TAG = "RepositoryCodeFragment";

    @BindView(android.R.id.list)
    protected RecyclerView recyclerView;

    @BindView(R.id.pb_loading)
    protected ProgressBar progressView;

    @BindView(R.id.tv_branch_icon)
    protected TextView branchIconView;

    @BindView(R.id.tv_branch)
    protected TextView branchView;

    @BindView(R.id.rl_branch)
    protected View branchFooterView;

    private GroupAdapter adapter = new GroupAdapter();

    private Section mainSection = new Section();

    private FullTree tree;

    private boolean pathShowing;

    private Folder folder;

    private Repository repository;

    private RefDialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        repository = activity.getIntent().getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter.add(mainSection);
        adapter.setOnItemClickListener(this);

        if (tree == null || folder == null) {
            refreshTree(null);
        } else {
            setFolder(tree, folder);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_refresh:
                if (tree != null) {
                    GitReference ref = GitReference.builder()
                            .ref(tree.reference.ref())
                            .build();
                    refreshTree(ref);
                } else {
                    refreshTree(null);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLoading(final boolean loading) {
        if (loading) {
            progressView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            branchFooterView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            branchFooterView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshTree(final GitReference reference) {
        showLoading(true);
        new RefreshTreeTask(getActivity(), repository, reference)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(fullTree -> {
                    if (folder == null || folder.isRoot()) {
                        setFolder(fullTree, fullTree.root);
                    } else {
                        // Look for current folder in new tree or else reset to root
                        Folder current = folder;
                        LinkedList<Folder> stack = new LinkedList<>();
                        while (!current.isRoot()) {
                            stack.addFirst(current);
                            current = current.parent;
                        }
                        Folder refreshed = fullTree.root;
                        while (!stack.isEmpty()) {
                            refreshed = refreshed.folders
                                    .get(stack.removeFirst().name);
                            if (refreshed == null) {
                                break;
                            }
                        }
                        if (refreshed != null) {
                            setFolder(fullTree, refreshed);
                        } else {
                            setFolder(fullTree, fullTree.root);
                        }
                    }
                }, e -> {
                    Log.d(TAG, "Exception loading tree", e);

                    showLoading(false);
                    ToastUtils.show(getActivity(), R.string.error_code_load);
                });
    }

    private void switchBranches() {
        if (tree == null) {
            return;
        }

        if (dialog == null) {
            dialog = new RefDialog((BaseActivity) getActivity(),
                    REF_UPDATE, repository);
        }
        dialog.show(tree.reference);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
        case REF_UPDATE:
            refreshTree(RefDialogFragment.getSelected(arguments));
            break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_repo_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        branchFooterView.setOnClickListener(v -> switchBranches());

        if (pathShowing) {
            mainSection.setHeader(new PathHeaderItem(""));
        }
    }

    /**
     * Back up the currently viewed folder to its parent
     *
     * @return true if directory changed, false otherwise
     */
    public boolean onBackPressed() {
        if (folder != null && !folder.isRoot()) {
            setFolder(tree, folder.parent);
            return true;
        } else {
            return false;
        }
    }

    private void setFolder(final FullTree tree, final Folder folder) {
        this.folder = folder;
        this.tree = tree;

        showLoading(false);

        branchView.setText(tree.branch);
        if (RefUtils.isTag(tree.reference)) {
            branchIconView.setText(R.string.icon_tag);
        } else {
            branchIconView.setText(R.string.icon_fork);
        }

        if (folder.entry != null) {
            int textLightColor = getResources().getColor(R.color.text_light);
            final String[] segments = folder.entry.path().split("/");
            StyledText text = new StyledText();
            for (int i = 0; i < segments.length - 1; i++) {
                final int index = i;
                text.url(segments[i], v -> {
                    Folder clicked = folder;
                    for (int i1 = index; i1 < segments.length - 1; i1++) {
                        clicked = clicked.parent;
                        if (clicked == null) {
                            return;
                        }
                    }
                    setFolder(tree, clicked);
                }).append(' ').foreground('/', textLightColor).append(' ');
            }
            text.bold(segments[segments.length - 1]);

            if (!pathShowing) {
                mainSection.setHeader(new PathHeaderItem(text));
                pathShowing = true;
            }
        } else if (pathShowing) {
            mainSection.removeHeader();
            pathShowing = false;
        }


        boolean indented = folder.entry != null;

        List items = new ArrayList();

        for (Folder folder1 : folder.folders.values()) {
            items.add(new FolderItem(getActivity(), folder1, indented));
        }

        for (Entry blob : folder.files.values()) {
            items.add(new BlobItem(getActivity(), blob, indented));
        }

        mainSection.update(items);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (tree == null) {
            return;
        }

        if (item instanceof BlobItem) {
            Entry entry = ((BlobItem) item).getData();
            startActivity(BranchFileViewActivity.createIntent(repository,
                    tree.branch, entry.entry.path(), entry.entry.sha()));
        } else if (item instanceof FolderItem) {
            Folder folder = ((FolderItem) item).getData();
            setFolder(tree, folder);
        }
    }
}
