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
package com.github.pockethub.ui.code;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.GitReference;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.core.code.FullTree;
import com.github.pockethub.core.code.FullTree.Entry;
import com.github.pockethub.core.code.FullTree.Folder;
import com.github.pockethub.core.code.RefreshTreeTask;
import com.github.pockethub.core.ref.RefUtils;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.HeaderFooterListAdapter;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.ui.ref.BranchFileViewActivity;
import com.github.pockethub.ui.ref.CodeTreeAdapter;
import com.github.pockethub.ui.ref.RefDialog;
import com.github.pockethub.ui.ref.RefDialogFragment;
import com.github.pockethub.util.ToastUtils;
import com.github.pockethub.util.TypefaceUtils;
import com.google.inject.Inject;

import java.util.LinkedList;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.RequestCodes.REF_UPDATE;

/**
 * Fragment to display a repository's source code tree
 */
public class RepositoryCodeFragment extends DialogFragment implements
        OnItemClickListener {

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

    private Repo repository;

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

        if (tree == null || folder == null)
            refreshTree(null);
        else
            setFolder(tree, folder);
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
                GitReference ref = new GitReference();
                ref.ref = tree.reference.ref;
                refreshTree(ref);
            }else
                refreshTree(null);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showLoading(final boolean loading) {
        ViewUtils.setGone(progressView, !loading);
        ViewUtils.setGone(listView, loading);
        ViewUtils.setGone(branchFooterView, loading);
    }

    private void refreshTree(final GitReference reference) {
        showLoading(true);
        new RefreshTreeTask(repository, reference, getActivity()) {

            @Override
            protected void onSuccess(final FullTree fullTree) throws Exception {
                super.onSuccess(fullTree);

                if (folder == null || folder.parent == null)
                    setFolder(fullTree, fullTree.root);
                else {
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
                        if (refreshed == null)
                            break;
                    }
                    if (refreshed != null)
                        setFolder(fullTree, refreshed);
                    else
                        setFolder(fullTree, fullTree.root);
                }
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                showLoading(false);
                ToastUtils.show(getActivity(), e, R.string.error_code_load);
            }

        }.execute();
    }

    private void switchBranches() {
        if (tree == null)
            return;

        if (dialog == null)
            dialog = new RefDialog((DialogFragmentActivity) getActivity(),
                    REF_UPDATE, repository);
        dialog.show(tree.reference);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

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

        progressView = finder.find(R.id.pb_loading);
        listView = finder.find(android.R.id.list);
        listView.setOnItemClickListener(this);

        Activity activity = getActivity();
        adapter = new HeaderFooterListAdapter<>(listView,
                new CodeTreeAdapter(activity));

        branchFooterView = finder.find(R.id.rl_branch);
        branchView = finder.find(R.id.tv_branch);
        branchIconView = finder.find(R.id.tv_branch_icon);
        branchFooterView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchBranches();
            }
        });

        pathHeaderView = activity.getLayoutInflater().inflate(R.layout.path_item,
                null);
        pathView = (TextView) pathHeaderView.findViewById(R.id.tv_path);
        pathView.setMovementMethod(LinkMovementMethod.getInstance());
        if (pathShowing)
            adapter.addHeader(pathHeaderView);

        TypefaceUtils.setOcticons(branchIconView,
                (TextView) pathHeaderView.findViewById(R.id.tv_folder_icon));
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
        } else
            return false;
    }

    private void setFolder(final FullTree tree, final Folder folder) {
        this.folder = folder;
        this.tree = tree;

        showLoading(false);

        branchView.setText(tree.branch);
        if (RefUtils.isTag(tree.reference))
            branchIconView.setText(R.string.icon_tag);
        else
            branchIconView.setText(R.string.icon_fork);

        adapter.getWrappedAdapter().setIndented(folder.entry != null);

        if (folder.entry != null) {
            int textLightColor = getResources().getColor(R.color.text_light);
            final String[] segments = folder.entry.path.split("/");
            StyledText text = new StyledText();
            for (int i = 0; i < segments.length - 1; i++) {
                final int index = i;
                text.url(segments[i], new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Folder clicked = folder;
                        for (int i = index; i < segments.length - 1; i++) {
                            clicked = clicked.parent;
                            if (clicked == null)
                                return;
                        }
                        setFolder(tree, clicked);
                    }
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
        if (tree == null || entry == null)
            return;

        if (entry instanceof Folder)
            setFolder(tree, (Folder) entry);
        else
            startActivity(BranchFileViewActivity.createIntent(repository,
                    tree.branch, entry.entry.path, entry.entry.sha));
    }
}
