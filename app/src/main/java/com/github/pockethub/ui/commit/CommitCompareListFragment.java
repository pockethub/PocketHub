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
package com.github.pockethub.ui.commit;

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

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.alorma.github.sdk.bean.dto.response.CompareCommit;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.repo.CompareCommitsClient;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.core.commit.CommitUtils;
import com.github.pockethub.rx.ObserverAdapter;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.HeaderFooterListAdapter;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.Intents.EXTRA_BASE;
import static com.github.pockethub.Intents.EXTRA_HEAD;
import static com.github.pockethub.Intents.EXTRA_REPOSITORY;

/**
 * Fragment to display a list of commits being compared
 */
public class CommitCompareListFragment extends DialogFragment implements
        OnItemClickListener {

    private DiffStyler diffStyler;

    private ListView list;

    private ProgressBar progress;

    private Repo repository;

    private String base;

    private String head;

    @Inject
    private AvatarLoader avatars;

    private HeaderFooterListAdapter<CommitFileListAdapter> adapter;

    private CompareCommit compare;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        repository = activity.getIntent().getParcelableExtra(EXTRA_REPOSITORY);
        base = getStringExtra(EXTRA_BASE);
        head = getStringExtra(EXTRA_HEAD);
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
        if (!isUsable())
            return false;

        switch (item.getItemId()) {
        case R.id.m_refresh:
            compareCommits();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void compareCommits() {
        new CompareCommitsClient(InfoUtils.createRepoInfo(repository), base, head)
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<CompareCommit>bindToLifecycle())
                .subscribe(new ObserverAdapter<CompareCommit>() {
                    @Override
                    public void onNext(CompareCommit compareCommit) {
                        List<CommitFile> files = compareCommit.files;
                        diffStyler.setFiles(files);
                        if (files != null)
                            Collections.sort(files, new CommitFileComparator());
                        updateList(compareCommit);
                    }

                    @Override
                    public void onError(Throwable error) {
                        ToastUtils.show(getActivity(), error, R.string.error_commits_load);
                    }
                });
    }

    private void updateList(CompareCommit compare) {
        if (!isUsable())
            return;

        this.compare = compare;

        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        adapter.clearHeaders();
        adapter.getWrappedAdapter().clear();

        List<Commit> commits = compare.commits;
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
        List<CommitFile> files = compare.files;
        if (files != null && !files.isEmpty()) {
            addFileStatHeader(files, inflater);
            for (CommitFile file : files)
                rootAdapter.addItem(file);
        }
    }

    private void addFileStatHeader(List<CommitFile> files,
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

        list = finder.find(android.R.id.list);
        progress = finder.find(R.id.pb_loading);

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
            Collection<Commit> commits = compare.commits;
            for (Commit candidate : commits)
                if (commit == candidate)
                    break;
                else
                    commitPosition++;
            if (commitPosition < commits.size())
                startActivity(CommitViewActivity.createIntent(repository,
                        commitPosition, commits));
        } else
            startActivity(CommitViewActivity.createIntent(repository,
                    commit.sha));
    }

    private void openFile(final CommitFile file) {
        if (!TextUtils.isEmpty(file.filename)
                && !TextUtils.isEmpty(file.sha))
            startActivity(CommitFileViewActivity.createIntent(repository, head, file));
    }

    private void openLine(AdapterView<?> parent, int position) {
        Object item = null;
        while (--position >= 0) {
            item = parent.getItemAtPosition(position);
            if (item instanceof CommitFile) {
                openFile((CommitFile) item);
                return;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Object item = parent.getItemAtPosition(position);
        if (item instanceof Commit)
            openCommit((Commit) item);
        else if (item instanceof CommitFile)
            openFile((CommitFile) item);
        else if (item instanceof CharSequence)
            openLine(parent, position);
    }
}
