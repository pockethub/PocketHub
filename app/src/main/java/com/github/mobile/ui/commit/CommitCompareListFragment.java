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
import static com.github.mobile.Intents.EXTRA_HEAD;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import android.accounts.Account;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.commit.CommitCompareTask;
import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.ui.DialogFragment;
import com.github.mobile.ui.HeaderFooterListAdapter;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ToastUtils;
import com.github.mobile.util.ViewUtils;
import com.google.inject.Inject;
import com.viewpagerindicator.R.layout;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryCommitCompare;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Fragment to display a list of commits being compared
 */
public class CommitCompareListFragment extends DialogFragment implements
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

    @InjectExtra(EXTRA_HEAD)
    private String head;

    @Inject
    private AvatarLoader avatars;

    private HeaderFooterListAdapter<CommitFileListAdapter> adapter;

    private RepositoryCommitCompare compare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        diffStyler = new DiffStyler(getResources());
        compareCommits();
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu,
            final MenuInflater inflater) {
        inflater.inflate(menu.refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isUsable())
            return false;

        switch (item.getItemId()) {
        case id.m_refresh:
            compareCommits();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void compareCommits() {
        new CommitCompareTask(getActivity(), repository, base, head) {

            @Override
            protected RepositoryCommitCompare run(Account account)
                    throws Exception {
                RepositoryCommitCompare compare = super.run(account);

                List<CommitFile> files = compare.getFiles();
                diffStyler.setFiles(files);
                if (files != null)
                    Collections.sort(files, new CommitFileComparator());
                return compare;
            }

            @Override
            protected void onSuccess(RepositoryCommitCompare compare)
                    throws Exception {
                super.onSuccess(compare);

                updateList(compare);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ToastUtils.show(getActivity(), e, string.error_commits_load);
            }

        }.execute();
    }

    private void updateList(RepositoryCommitCompare compare) {
        if (!isUsable())
            return;

        this.compare = compare;

        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        adapter.clearHeaders();
        adapter.getWrappedAdapter().clear();

        List<RepositoryCommit> commits = compare.getCommits();
        if (commits != null && !commits.isEmpty()) {
            View commitHeader = inflater.inflate(layout.commit_details_header,
                    null);
            ((TextView) commitHeader.findViewById(id.tv_commit_summary))
                    .setText(MessageFormat.format(
                            getString(string.comparing_commits), commits.size()));
            adapter.addHeader(commitHeader, null, false);
            adapter.addHeader(inflater.inflate(layout.list_divider, null));
            CommitListAdapter commitAdapter = new CommitListAdapter(
                    layout.commit_item, inflater, commits, avatars);
            for (int i = 0; i < commits.size(); i++) {
                RepositoryCommit commit = commits.get(i);
                View view = commitAdapter.getView(i, null, null);
                adapter.addHeader(view, commit, true);
                adapter.addHeader(inflater.inflate(layout.list_divider, null));
            }
        }

        CommitFileListAdapter rootAdapter = adapter.getWrappedAdapter();
        rootAdapter.clear();
        List<CommitFile> files = compare.getFiles();
        if (files != null && !files.isEmpty()) {
            addFileStatHeader(files, inflater);
            for (CommitFile file : files)
                rootAdapter.addItem(file);
        }
    }

    private void addFileStatHeader(List<CommitFile> files,
            LayoutInflater inflater) {
        View fileHeader = inflater.inflate(
                layout.commit_compare_file_details_header, null);
        ((TextView) fileHeader.findViewById(id.tv_commit_file_summary))
                .setText(CommitUtils.formatStats(files));
        adapter.addHeader(fileHeader);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        list.setOnItemClickListener(this);

        adapter = new HeaderFooterListAdapter<CommitFileListAdapter>(list,
                new CommitFileListAdapter(inflater, diffStyler, null, null));
        adapter.addFooter(inflater.inflate(layout.footer_separator, null));
        list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.commit_diff_list, container);
    }

    private void openCommit(final RepositoryCommit commit) {
        if (compare != null) {
            int commitPosition = 0;
            Collection<RepositoryCommit> commits = compare.getCommits();
            for (RepositoryCommit candidate : commits)
                if (commit == candidate)
                    break;
                else
                    commitPosition++;
            if (commitPosition < commits.size())
                startActivity(CommitViewActivity.createIntent(repository,
                        commitPosition, commits));
        } else
            startActivity(CommitViewActivity.createIntent(repository,
                    commit.getSha()));
    }

    private void openFile(final CommitFile file) {
        startActivity(CommitFileViewActivity.createIntent(repository, head,
                file));
    }

    private void openLine(AdapterView<?> parent, int position) {
        Object item = null;
        while (--position >= 0) {
            item = parent.getItemAtPosition(position);
            if (item instanceof CommitFile) {
                startActivity(CommitFileViewActivity.createIntent(repository,
                        head, (CommitFile) item));
                return;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof RepositoryCommit)
            openCommit((RepositoryCommit) item);
        else if (item instanceof CommitFile)
            openFile((CommitFile) item);
        else if (item instanceof CharSequence)
            openLine(parent, position);
    }
}
