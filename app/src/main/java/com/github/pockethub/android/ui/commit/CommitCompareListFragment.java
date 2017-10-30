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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.HeaderFooterListAdapter;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.CommitCompare;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.google.inject.Inject;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.Intents.EXTRA_BASE;
import static com.github.pockethub.android.Intents.EXTRA_HEAD;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of commits being compared
 */
public class CommitCompareListFragment extends DialogFragment implements
        OnItemClickListener {

    private DiffStyler diffStyler;

    private ListView list;

    private ProgressBar progress;

    private Repository repository;

    private String base;

    private String head;

    @Inject
    private AvatarLoader avatars;

    private HeaderFooterListAdapter<CommitFileListAdapter> adapter;

    private CommitCompare compare;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        repository = activity.getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        base = getStringExtra(EXTRA_BASE).substring(0, 7);
        head = getStringExtra(EXTRA_HEAD).substring(0, 7);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        diffStyler = new DiffStyler(getResources());
        compareCommits();
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu,
            final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isUsable()) {
            return false;
        }

        switch (item.getItemId()) {
        case R.id.m_refresh:
            compareCommits();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void compareCommits() {
        ServiceGenerator.createService(getActivity(), RepositoryCommitService.class)
                .compareCommits(repository.owner().login(), repository.name(), base, head)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> {
                    CommitCompare compareCommit = response.body();
                    List<GitHubFile> files = compareCommit.files();
                    diffStyler.setFiles(files);
                    Collections.sort(files, new CommitFileComparator());
                    updateList(compareCommit);
                }, error -> ToastUtils.show(getActivity(), error, R.string.error_commits_load));
    }

    private void updateList(CommitCompare compare) {
        if (!isUsable()) {
            return;
        }

        this.compare = compare;

        progress.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        adapter.clearHeaders();
        adapter.getWrappedAdapter().clear();

        List<Commit> commits = compare.commits();
        if (!commits.isEmpty()) {
            View commitHeader = inflater.inflate(R.layout.commit_details_header,
                    null);
            ((TextView) commitHeader.findViewById(R.id.tv_commit_summary))
                    .setText(MessageFormat.format(
                            getString(R.string.comparing_commits), commits.size()));
            adapter.addHeader(commitHeader);
            adapter.addHeader(inflater.inflate(R.layout.list_divider, null));
            CommitListAdapter commitAdapter = new CommitListAdapter(
                    R.layout.commit_item, inflater, commits, avatars);
            for (int i = 0; i < commits.size(); i++) {
                Commit commit = commits.get(i);
                View view = commitAdapter.getView(i, null, null);
                adapter.addHeader(view, commit, true);
                adapter.addHeader(inflater.inflate(R.layout.list_divider, null));
            }
        }

        CommitFileListAdapter rootAdapter = adapter.getWrappedAdapter();
        rootAdapter.clear();
        List<GitHubFile> files = compare.files();
        if (!files.isEmpty()) {
            addFileStatHeader(files, inflater);
            for (GitHubFile file : files) {
                rootAdapter.addItem(file);
            }
        }
    }

    private void addFileStatHeader(List<GitHubFile> files,
            LayoutInflater inflater) {
        View fileHeader = inflater.inflate(
                R.layout.commit_compare_file_details_header, null);
        ((TextView) fileHeader.findViewById(R.id.tv_commit_file_summary))
                .setText(CommitUtils.formatStats(files));
        adapter.addHeader(fileHeader);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = (ListView) view.findViewById(android.R.id.list);
        progress = (ProgressBar) view.findViewById(R.id.pb_loading);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        list.setOnItemClickListener(this);

        adapter = new HeaderFooterListAdapter<>(list,
                new CommitFileListAdapter(inflater, diffStyler, null, null));
        adapter.addFooter(inflater.inflate(R.layout.footer_separator, null));
        list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commit_diff_list, container);
    }

    private void openCommit(final Commit commit) {
        if (compare != null) {
            int commitPosition = 0;
            Collection<Commit> commits = compare.commits();
            for (Commit candidate : commits) {
                if (commit == candidate) {
                    break;
                } else {
                    commitPosition++;
                }
            }
            if (commitPosition < commits.size()) {
                startActivity(CommitViewActivity.createIntent(repository, commitPosition, commits));
            }
        } else {
            startActivity(CommitViewActivity.createIntent(repository,
                    commit.sha()));
        }
    }

    private void openFile(final GitHubFile file) {
        if (!TextUtils.isEmpty(file.filename())
                && !TextUtils.isEmpty(file.sha())) {
            startActivity(CommitFileViewActivity.createIntent(repository, head, file));
        }
    }

    private void openLine(AdapterView<?> parent, int position) {
        Object item;
        while (--position >= 0) {
            item = parent.getItemAtPosition(position);
            if (item instanceof GitHubFile) {
                openFile((GitHubFile) item);
                return;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof Commit) {
            openCommit((Commit) item);
        } else if (item instanceof GitHubFile) {
            openFile((GitHubFile) item);
        } else if (item instanceof CharSequence) {
            openLine(parent, position);
        }
    }
}
