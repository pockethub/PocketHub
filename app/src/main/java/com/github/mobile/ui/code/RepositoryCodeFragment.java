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

import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R.string;
import com.github.mobile.core.code.FullTree;
import com.github.mobile.core.code.FullTree.Entry;
import com.github.mobile.core.code.FullTree.Folder;
import com.github.mobile.core.code.RefreshTreeTask;
import com.github.mobile.ui.DialogFragment;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.viewpagerindicator.R.id;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a repository's source code tree
 */
public class RepositoryCodeFragment extends DialogFragment implements
        OnItemClickListener {

    private FullTree tree;

    private ListView listView;

    private ProgressBar progressView;

    private TextView pathView;

    private HeaderFooterListAdapter<CodeTreeAdapter> adapter;

    private Folder folder;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refreshTree();
    }

    private void refreshTree() {
        new RefreshTreeTask(repository, getActivity()) {

            @Override
            protected void onSuccess(final FullTree fullTree) throws Exception {
                super.onSuccess(fullTree);

                tree = fullTree;
                ViewUtils.setGone(progressView, true);
                ViewUtils.setGone(listView, false);
                setFolder(fullTree.root);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ViewUtils.setGone(progressView, true);
                ViewUtils.setGone(listView, false);
                Toaster.showLong(getActivity(), string.error_code_load);
            }

        }.execute();
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

        adapter = new HeaderFooterListAdapter<CodeTreeAdapter>(listView,
                new CodeTreeAdapter(getActivity()));
        View pathHeader = getActivity().getLayoutInflater().inflate(
                layout.path_item, null);
        pathView = (TextView) pathHeader.findViewById(id.tv_path);
        adapter.addHeader(pathHeader);
        listView.setAdapter(adapter);
    }

    /**
     * Back up the currently viewed folder to its parent
     *
     * @return true if directory changed, false otherwise
     */
    public boolean onBackPressed() {
        if (folder != null && folder.parent != null) {
            setFolder(folder.parent);
            return true;
        } else
            return false;
    }

    private void setFolder(final Folder folder) {
        this.folder = folder;
        if (folder.entry != null)
            pathView.setText(folder.entry.getPath());
        else
            pathView.setText(tree.branch);
        adapter.getWrappedAdapter().setItems(folder);
        listView.setSelection(0);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Entry entry = (Entry) parent.getItemAtPosition(position);
        if (entry instanceof Folder)
            setFolder((Folder) entry);
        else
            startActivity(BranchFileViewActivity.createIntent(repository,
                    tree.branch, entry.name, entry.entry.getSha()));
    }
}
