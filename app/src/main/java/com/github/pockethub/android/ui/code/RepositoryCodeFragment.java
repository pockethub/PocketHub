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
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.code.FullTree;
import com.github.pockethub.android.core.code.FullTree.Entry;
import com.github.pockethub.android.core.code.FullTree.Folder;
import com.github.pockethub.android.core.code.RefreshTreeTask;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.HeaderFooterListAdapter;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.ref.BranchFileViewActivity;
import com.github.pockethub.android.ui.ref.CodeTreeAdapter;
import com.github.pockethub.android.ui.ref.RefDialog;
import com.github.pockethub.android.ui.ref.RefDialogFragment;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;

import java.util.LinkedList;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.RequestCodes.REF_UPDATE;

/**
 * Fragment to display a repository's source code tree
 */
public class RepositoryCodeFragment extends DialogFragment implements
        OnItemClickListener {

    private static final String TAG = "RepositoryCodeFragment";

    private FullTree tree;

    private ListView listView;

    private ProgressBar progressView;

    private TextView branchIconView;

    private TextView branchView;

    private TextView pathView;

    private View pathHeaderView;

    private View branchFooterView;

    private HeaderFooterListAdapter<CodeTreeAdapter> adapter;

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
            }else {
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
            listView.setVisibility(View.GONE);
            branchFooterView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            branchFooterView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshTree(final GitReference reference) {
        showLoading(true);
        new RefreshTreeTask(getActivity(), repository, reference)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(fullTree -> {
                    if (folder == null || folder.parent == null) {
                        setFolder(fullTree, fullTree.root);
                    } else {
                        // Look for current folder in new tree or else reset to root
                        Folder current = folder;
                        LinkedList<Folder> stack = new LinkedList<>();
                        while (current.parent != null) {
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
                    ToastUtils.show(getActivity(), e, R.string.error_code_load);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressView = (ProgressBar) view.findViewById(R.id.pb_loading);
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);

        Activity activity = getActivity();
        adapter = new HeaderFooterListAdapter<>(listView,
                new CodeTreeAdapter(activity));

        branchFooterView = view.findViewById(R.id.rl_branch);
        branchView = (TextView) view.findViewById(R.id.tv_branch);
        branchIconView = (TextView) view.findViewById(R.id.tv_branch_icon);
        branchFooterView.setOnClickListener(v -> switchBranches());

        pathHeaderView = activity.getLayoutInflater().inflate(R.layout.path_item,
                null);
        pathView = (TextView) pathHeaderView.findViewById(R.id.tv_path);
        pathView.setMovementMethod(LinkMovementMethod.getInstance());
        if (pathShowing) {
            adapter.addHeader(pathHeaderView);
        }

        listView.setAdapter(adapter);
    }

    /**
     * Back up the currently viewed folder to its parent
     *
     * @return true if directory changed, false otherwise
     */
    public boolean onBackPressed() {
        if (folder != null && folder.parent != null) {
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

        adapter.getWrappedAdapter().setIndented(folder.entry != null);

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
            pathView.setText(text);
            if (!pathShowing) {
                adapter.addHeader(pathHeaderView);
                pathShowing = true;
            }
        } else if (pathShowing) {
            adapter.removeHeader(pathHeaderView);
            pathShowing = false;
        }

        adapter.getWrappedAdapter().setItems(folder);
        listView.setSelection(0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Entry entry = (Entry) parent.getItemAtPosition(position);
        if (tree == null || entry == null) {
            return;
        }

        if (entry instanceof Folder) {
            setFolder(tree, (Folder) entry);
        } else {
            startActivity(BranchFileViewActivity.createIntent(repository,
                    tree.branch, entry.entry.path(), entry.entry.sha()));
        }
    }
}
