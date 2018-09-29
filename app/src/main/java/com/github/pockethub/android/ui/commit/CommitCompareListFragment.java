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
import android.view.*;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.commit.CommitUtils;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.ui.item.TextItem;
import com.github.pockethub.android.ui.item.commit.CommitFileHeaderItem;
import com.github.pockethub.android.ui.item.commit.CommitFileLineItem;
import com.github.pockethub.android.ui.item.commit.CommitItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.CommitCompare;
import com.meisolsson.githubsdk.model.GitHubFile;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.Section;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.pockethub.android.Intents.*;

/**
 * Fragment to display a list of commits being compared
 */
public class CommitCompareListFragment extends BaseFragment implements OnItemClickListener {

    @BindView(R.id.list)
    protected RecyclerView list;

    @BindView(R.id.pb_loading)
    protected ProgressBar progress;

    private DiffStyler diffStyler;

    private Repository repository;

    private String base;

    private String head;

    @Inject
    protected AvatarLoader avatars;

    private GroupAdapter adapter = new GroupAdapter();

    private Section mainSection = new Section();

    private Section commitsSection = new Section();

    private Section filesSection = new Section();

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
        mainSection.add(commitsSection);
        mainSection.add(filesSection);
        adapter.add(mainSection);

        adapter.setOnItemClickListener(this);

        diffStyler = new DiffStyler(getResources());
        compareCommits();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commit_diff_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(final Menu optionsMenu,
            final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_refresh, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (!isAdded()) {
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
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(response -> {
                    CommitCompare compareCommit = response.body();
                    List<GitHubFile> files = compareCommit.files();
                    diffStyler.setFiles(files);
                    Collections.sort(files, new CommitFileComparator());
                    updateList(compareCommit);
                }, error -> ToastUtils.show(getActivity(), R.string.error_commits_load));
    }

    private void updateList(CommitCompare compare) {
        if (!isAdded()) {
            return;
        }

        this.compare = compare;

        progress.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        List<Commit> commits = compare.commits();
        if (!commits.isEmpty()) {
            String comparingCommits = getString(R.string.comparing_commits);
            String text = MessageFormat.format(comparingCommits, commits.size());
            commitsSection.setHeader(
                    new TextItem(R.layout.commit_details_header, R.id.tv_commit_summary, text)
            );

            List<CommitItem> items = new ArrayList<>();
            for (Commit commit : commits) {
                items.add(new CommitItem(avatars, commit));
            }

            commitsSection.update(items);
        }

        List<GitHubFile> files = compare.files();
        if (!files.isEmpty()) {
            filesSection.setHeader(
                    new TextItem(R.layout.commit_compare_file_details_header,
                            R.id.tv_commit_file_summary, CommitUtils.formatStats(files))
            );
            filesSection.update(createFileSections(files));
        }
    }

    private List<Section> createFileSections(List<GitHubFile> files) {
        List<Section> sections = new ArrayList<>();

        for (GitHubFile file : files) {
            Section section = new Section(new CommitFileHeaderItem(getActivity(), file));
            List<CharSequence> lines = diffStyler.get(file.filename());
            for (CharSequence line : lines) {
                section.add(new CommitFileLineItem(diffStyler, line));
            }

            sections.add(section);
        }

        return sections;
    }

    private void openCommit(final Commit commit) {
        if (compare != null) {
            int commitPosition = 0;
            List<Commit> commits = compare.commits();
            for (Commit candidate : commits) {
                if (commit == candidate) {
                    break;
                } else {
                    commitPosition++;
                }
            }
            if (commitPosition < commits.size()) {
                String[] ids = new String[commits.size()];
                for (int i = 0; i < commits.size(); i++) {
                    ids[i] = commits.get(i).sha();
                }
                startActivity(CommitViewActivity.createIntent(repository, commitPosition, ids));
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

    private void openLine(GroupAdapter adapter, int position) {
        Object item;
        while (--position >= 0) {
            item = adapter.getItem(position);
            if (item instanceof CommitFileHeaderItem) {
                openFile(((CommitFileHeaderItem) item).getFile());
                return;
            }
        }
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof CommitItem) {
            openCommit(((CommitItem) item).getCommit());
        } else if (item instanceof CommitFileHeaderItem) {
            openFile(((CommitFileHeaderItem) item).getFile());
        } else if (item instanceof CommitFileLineItem) {
            int position = adapter.getAdapterPosition(item);
            openLine(adapter, position);
        }
    }
}
