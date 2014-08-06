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

import static android.app.Activity.RESULT_OK;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.RequestCodes.COMMIT_VIEW;
import static com.github.mobile.RequestCodes.REF_UPDATE;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ThrowableLoader;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.commit.CommitPager;
import com.github.mobile.core.commit.CommitStore;
import com.github.mobile.core.ref.RefUtils;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.DialogResultListener;
import com.github.mobile.ui.ItemListFragment;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.ui.ref.RefDialog;
import com.github.mobile.ui.ref.RefDialogFragment;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TypefaceUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Fragment to display a list of repository commits
 */
public class CommitListFragment extends PagedItemFragment<RepositoryCommit>
        implements DialogResultListener {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    @Inject
    private CommitService service;

    @Inject
    private CommitStore store;

    private Repository repository;

    private RefDialog dialog;

    private TextView branchIconView;

    private TextView branchView;

    private View branchFooterView;

    @Inject
    private DataService dataService;

    @Inject
    private RepositoryService repoService;

    private String ref;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        repository = getSerializableExtra(EXTRA_REPOSITORY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(string.no_commits);
    }

    @Override
    public Loader<List<RepositoryCommit>> onCreateLoader(int id, Bundle bundle) {
        final ThrowableLoader<List<RepositoryCommit>> parentLoader = (ThrowableLoader<List<RepositoryCommit>>) super
                .onCreateLoader(id, bundle);
        return new ThrowableLoader<List<RepositoryCommit>>(getActivity(), items) {

            @Override
            public List<RepositoryCommit> loadData() throws Exception {
                if (TextUtils.isEmpty(ref)) {
                    String defaultBranch = repository.getMasterBranch();
                    if (TextUtils.isEmpty(defaultBranch)) {
                        defaultBranch = repoService.getRepository(repository)
                                .getMasterBranch();
                        if (TextUtils.isEmpty(defaultBranch))
                            defaultBranch = "master";
                    }
                    ref = defaultBranch;
                }

                return parentLoader.loadData();
            }
        };
    }

    public void onLoadFinished(Loader<List<RepositoryCommit>> loader,
            List<RepositoryCommit> items) {
        super.onLoadFinished(loader, items);

        if (ref != null)
            updateRefLabel();
    }

    @Override
    protected ResourcePager<RepositoryCommit> createPager() {
        return new CommitPager(repository, store) {

            private String last;

            @Override
            protected RepositoryCommit register(RepositoryCommit resource) {
                // Store first parent of last commit registered for next page
                // lookup
                List<Commit> parents = resource.getParents();
                if (parents != null && !parents.isEmpty())
                    last = parents.get(0).getSha();
                else
                    last = null;

                return super.register(resource);
            }

            @Override
            public PageIterator<RepositoryCommit> createIterator(int page,
                    int size) {
                if (page > 1 || ref == null)
                    return service.pageCommits(repository, last, null, size);
                else
                    return service.pageCommits(repository, ref, null, size);
            }

            @Override
            public ResourcePager<RepositoryCommit> clear() {
                last = null;
                return super.clear();
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_commits;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return string.error_commits_load;
    }

    @Override
    protected SingleTypeAdapter<RepositoryCommit> createAdapter(
            List<RepositoryCommit> items) {
        return new CommitListAdapter(layout.commit_item, getActivity()
                .getLayoutInflater(), items, avatars);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object item = l.getItemAtPosition(position);
        if (item instanceof RepositoryCommit)
            startActivityForResult(CommitViewActivity.createIntent(repository,
                    position, items), COMMIT_VIEW);
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
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case REF_UPDATE:
            setRef(RefDialogFragment.getSelected(arguments));
            break;
        }
    }

    private void updateRefLabel() {
        branchView.setText(RefUtils.getName(ref));
        if (RefUtils.isTag(ref))
            branchIconView.setText(string.icon_tag);
        else
            branchIconView.setText(string.icon_fork);
    }

    private void setRef(final Reference ref) {
        this.ref = ref.getRef();
        updateRefLabel();
        refreshWithProgress();
    }

    private void switchRefs() {
        if (ref == null)
            return;

        if (dialog == null)
            dialog = new RefDialog((DialogFragmentActivity) getActivity(),
                    REF_UPDATE, repository, dataService);
        dialog.show(new Reference().setRef(ref));
    }

    @Override
    public ItemListFragment<RepositoryCommit> setListShown(boolean shown,
            boolean animate) {
        ViewUtils.setGone(branchFooterView, !shown);
        return super.setListShown(shown, animate);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        branchFooterView = finder.find(id.rl_branch);
        branchView = finder.find(id.tv_branch);
        branchIconView = finder.find(id.tv_branch_icon);
        TypefaceUtils.setOcticons(branchIconView);
        branchFooterView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchRefs();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(layout.commit_list, null);
    }
}
