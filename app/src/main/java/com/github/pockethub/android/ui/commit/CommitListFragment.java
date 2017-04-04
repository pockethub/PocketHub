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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Commit;
import com.meisolsson.githubsdk.model.Repository;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ThrowableLoader;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.ResourcePager;
import com.github.pockethub.android.core.commit.CommitPager;
import com.github.pockethub.android.core.commit.CommitStore;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.DialogResultListener;
import com.github.pockethub.android.ui.ItemListFragment;
import com.github.pockethub.android.ui.PagedItemFragment;
import com.github.pockethub.android.ui.ref.RefDialog;
import com.github.pockethub.android.ui.ref.RefDialogFragment;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.google.inject.Inject;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.RequestCodes.COMMIT_VIEW;
import static com.github.pockethub.android.RequestCodes.REF_UPDATE;

/**
 * Fragment to display a list of repository commits
 */
public class CommitListFragment extends PagedItemFragment<Commit>
        implements DialogResultListener {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    @Inject
    private CommitStore store;

    private Repository repository;

    private RefDialog dialog;

    private TextView branchIconView;

    private TextView branchView;

    private View branchFooterView;

    private String ref;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = (Activity) context;
        repository = activity.getIntent().getParcelableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_commits);
    }

    @Override
    public Loader<List<Commit>> onCreateLoader(int id, Bundle bundle) {
        final ThrowableLoader<List<Commit>> parentLoader = (ThrowableLoader<List<Commit>>) super
                .onCreateLoader(id, bundle);
        return new ThrowableLoader<List<Commit>>(getActivity(), items) {

            @Override
            public List<Commit> loadData() throws Exception {
                if (TextUtils.isEmpty(ref)) {
                    String defaultBranch = repository.defaultBranch();
                    if (TextUtils.isEmpty(defaultBranch)) {
                        defaultBranch = ServiceGenerator.createService(getActivity(), RepositoryService.class)
                                .getRepository(repository.owner().login(), repository.name())
                                .blockingGet()
                                .body()
                                .defaultBranch();

                        if (TextUtils.isEmpty(defaultBranch)) {
                            defaultBranch = "master";
                        }
                    }
                    ref = defaultBranch;
                }

                return parentLoader.loadData();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Commit>> loader, List<Commit> items) {
        super.onLoadFinished(loader, items);

        if (ref != null) {
            updateRefLabel();
        }
    }

    @Override
    protected ResourcePager<Commit> createPager() {
        return new CommitPager(repository, store) {

            private String last;

            @Override
            protected Commit register(Commit resource) {
                // Store first parent of last commit registered for next page
                // lookup
                List<Commit> parents = resource.parents();
                if (parents != null && !parents.isEmpty()) {
                    last = parents.get(0).sha();
                } else {
                    last = null;
                }

                return super.register(resource);
            }

            @Override
            public PageIterator<Commit> createIterator(int page, int size) {

                return new PageIterator<>(page1 -> {
                    RepositoryCommitService service = ServiceGenerator.createService(getActivity(),
                            RepositoryCommitService.class);

                    if (page1 > 1 || ref == null) {
                        return service.getCommits(repository.owner().login(), repository.name(), last, page1);
                    } else {
                        return service.getCommits(repository.owner().login(), repository.name(), ref, page1);
                    }
                }, page);
            }

            @Override
            public ResourcePager<Commit> clear() {
                last = null;
                return super.clear();
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_commits;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_commits_load;
    }

    @Override
    protected SingleTypeAdapter<Commit> createAdapter(
            List<Commit> items) {
        return new CommitListAdapter(R.layout.commit_item, getActivity()
                .getLayoutInflater(), items, avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof Commit) {
            startActivityForResult(CommitViewActivity.createIntent(repository,
                    position, items), COMMIT_VIEW);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMMIT_VIEW) {
            notifyDataSetChanged();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
        case REF_UPDATE:
            setRef(RefDialogFragment.getSelected(arguments));
            break;
        }
    }

    private void updateRefLabel() {
        branchView.setText(RefUtils.getName(ref));
        if (RefUtils.isTag(ref)) {
            branchIconView.setText(R.string.icon_tag);
        } else {
            branchIconView.setText(R.string.icon_fork);
        }
    }

    private void setRef(final GitReference ref) {
        this.ref = ref.ref();
        updateRefLabel();
        refreshWithProgress();
    }

    private void switchRefs() {
        if (ref == null) {
            return;
        }

        if (dialog == null) {
            dialog = new RefDialog((BaseActivity) getActivity(),
                    REF_UPDATE, repository);
        }
        GitReference reference = GitReference.builder()
                .ref(ref)
                .build();
        dialog.show(reference);
    }

    @Override
    public ItemListFragment<Commit> setListShown(boolean shown,
            boolean animate) {
        branchFooterView.setVisibility(shown ? View.VISIBLE : View.GONE);
        return super.setListShown(shown, animate);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        branchFooterView = view.findViewById(R.id.rl_branch);
        branchView = (TextView) view.findViewById(R.id.tv_branch);
        branchIconView = (TextView) view.findViewById(R.id.tv_branch_icon);
        branchFooterView.setOnClickListener(v -> switchRefs());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commit_list, container, false);
    }
}
