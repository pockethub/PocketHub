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
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.core.commit.FullCommit;
import com.github.mobile.core.commit.FullCommitFile;
import com.github.mobile.core.commit.RefreshCommitTask;
import com.github.mobile.ui.DialogFragment;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.ViewUtils;
import com.google.inject.Inject;
import com.viewpagerindicator.R.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Fragment to display commit details with diff output
 */
public class CommitDiffListFragment extends DialogFragment implements
        OnItemClickListener {

    private DiffStyler diffStyler;

    @InjectView(android.R.id.list)
    private ListView list;

    @InjectView(id.pb_loading)
    private ProgressBar progress;

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @InjectExtra(EXTRA_BASE)
    private String base;

    @Inject
    private AvatarLoader avatars;

    private TextView commitMessage;

    private ImageView authorAvatar;

    private TextView authorName;

    private TextView authorDate;

    private HeaderFooterListAdapter<CommitFileListAdapter> adapter;

    private HttpImageGetter commentImageGetter;

    private List<View> parentViews = new ArrayList<View>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        commentImageGetter = new HttpImageGetter(getActivity());

        refreshCommit();
    }

    private void refreshCommit() {
        new RefreshCommitTask(getActivity(), repository, base,
                commentImageGetter) {

            @Override
            protected FullCommit run() throws Exception {
                FullCommit full = super.run();

                List<CommitFile> files = full.getCommit().getFiles();
                diffStyler.setFiles(files);
                if (files != null)
                    Collections.sort(files, new CommitFileComparator());
                return full;
            }

            @Override
            protected void onSuccess(FullCommit commit) throws Exception {
                super.onSuccess(commit);

                updateList(commit);
            }

        }.execute();
    }

    private void updateList(FullCommit fullCommit) {
        if (!isUsable())
            return;

        RepositoryCommit commit = fullCommit.getCommit();
        LayoutInflater inflater = getActivity().getLayoutInflater();

        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);

        Commit rawCommit = commit.getCommit();
        commitMessage.setText(rawCommit.getMessage());
        User author = commit.getAuthor();
        CommitUser commitAuthor = rawCommit.getAuthor();
        avatars.bind(authorAvatar, author);
        if (author != null)
            authorName.setText(author.getLogin());
        else
            authorName.setText(commitAuthor.getName());
        authorDate.setText(new StyledText().append(commitAuthor.getDate()));

        for (View view : parentViews)
            adapter.removeHeader(view);
        parentViews.clear();

        List<Commit> parents = commit.getParents();
        if (parents != null && !parents.isEmpty()) {
            for (Commit parent : parents) {
                View parentView = inflater.inflate(layout.commit_parent_item,
                        null);
                ((TextView) parentView.findViewById(id.tv_commit_id))
                        .setText(CommitUtils.abbreviate(parent));
                adapter.addHeader(parentView, parent, true);
            }
        }

        CommitFileListAdapter rootAdapter = adapter.getWrappedAdapter();
        for (FullCommitFile file : fullCommit.getFiles())
            rootAdapter.addItem(file);
        for (CommitComment comment : fullCommit)
            rootAdapter.addComment(comment);

        adapter.addFooter(inflater.inflate(layout.footer_separator, null));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        diffStyler = new DiffStyler(getResources());

        list.setOnItemClickListener(this);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        adapter = new HeaderFooterListAdapter<CommitFileListAdapter>(list,
                new CommitFileListAdapter(inflater, diffStyler, avatars,
                        new HttpImageGetter(getActivity())));
        list.setAdapter(adapter);

        View header = inflater.inflate(layout.commit_header, null);
        commitMessage = (TextView) header.findViewById(id.tv_commit_message);
        authorAvatar = (ImageView) header.findViewById(id.iv_avatar);
        authorName = (TextView) header.findViewById(id.tv_commit_author);
        authorDate = (TextView) header.findViewById(id.tv_commit_date);

        adapter.addHeader(header, null, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.commit_diff_list, container);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof Commit)
            startActivity(CommitViewActivity.createIntent(repository,
                    ((Commit) item).getSha()));
    }
}
