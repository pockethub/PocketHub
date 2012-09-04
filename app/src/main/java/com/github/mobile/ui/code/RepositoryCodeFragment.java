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
package com.github.mobile.ui.code;

import static android.app.Activity.RESULT_OK;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.RequestCodes.REF_UPDATE;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.R.color;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.code.FullTree;
import com.github.mobile.core.code.FullTree.Entry;
import com.github.mobile.core.code.FullTree.Folder;
import com.github.mobile.core.code.RefreshTreeTask;
import com.github.mobile.core.ref.RefUtils;
import com.github.mobile.ui.DialogFragment;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.ui.ref.BranchFileViewActivity;
import com.github.mobile.ui.ref.CodeTreeAdapter;
import com.github.mobile.ui.ref.RefDialog;
import com.github.mobile.ui.ref.RefDialogFragment;
import com.github.mobile.util.ToastUtils;
import com.github.mobile.util.TypefaceUtils;
import com.google.inject.Inject;

import java.util.LinkedList;

import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.DataService;

import roboguice.inject.InjectExtra;

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

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @Inject
    private DataService service;

    private RefDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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
        inflater.inflate(menu.refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_refresh:
            if (tree != null)
                refreshTree(new Reference().setRef(tree.reference.getRef()));
            else
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

    private void refreshTree(final Reference reference) {
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
                    LinkedList<Folder> stack = new LinkedList<Folder>();
                    while (current != null && current.parent != null) {
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
                ToastUtils.show(getActivity(), e, string.error_code_load);
            }

        }.execute();
    }

    private void switchBranches() {
        if (tree == null)
            return;

        if (dialog == null)
            dialog = new RefDialog((DialogFragmentActivity) getActivity(),
                    REF_UPDATE, repository, service);
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
        return inflater.inflate(layout.repo_code, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewFinder finder = new ViewFinder(view);
        progressView = finder.find(id.pb_loading);
        listView = finder.find(android.R.id.list);
        listView.setOnItemClickListener(this);

        Activity activity = getActivity();
        adapter = new HeaderFooterListAdapter<CodeTreeAdapter>(listView,
                new CodeTreeAdapter(activity));

        branchFooterView = finder.find(id.rl_branch);
        branchView = finder.find(id.tv_branch);
        branchIconView = finder.find(id.tv_branch_icon);
        branchFooterView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                switchBranches();
            }
        });

        pathHeaderView = activity.getLayoutInflater().inflate(layout.path_item,
                null);
        pathView = (TextView) pathHeaderView.findViewById(id.tv_path);
        pathView.setMovementMethod(LinkMovementMethod.getInstance());

        TypefaceUtils.setOcticons(branchIconView,
                (TextView) pathHeaderView.findViewById(id.tv_folder_icon));
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
            branchIconView.setText(string.icon_tag);
        else
            branchIconView.setText(string.icon_fork);

        if (folder.entry != null) {
            int textLightColor = getResources().getColor(color.text_light);
            final String[] segments = folder.entry.getPath().split("/");
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
            text.append(segments[segments.length - 1]);
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
        if (entry instanceof Folder)
            setFolder(tree, (Folder) entry);
        else
            startActivity(BranchFileViewActivity.createIntent(repository,
                    tree.branch, entry.name, entry.entry.getSha()));
    }
}
