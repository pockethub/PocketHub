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
package com.github.mobile.issue;

import static com.google.common.collect.Lists.newArrayList;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.async.AuthenticatedUserLoader;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.ListLoadingFragment;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * List fragment displaying a list of {@link IssueFilter} items
 */
public class FilterListFragment extends ListLoadingFragment<IssueFilter> {

    @Inject
    private AccountDataManager cache;

    @Inject
    private AvatarUtils avatarHelper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListViewUtils.configure(getActivity(), getListView(), true);
        setEmptyText(getString(string.no_filters));
    }

    @Override
    public Loader<List<IssueFilter>> onCreateLoader(int id, Bundle args) {
        return new AuthenticatedUserLoader<List<IssueFilter>>(getActivity()) {

            public List<IssueFilter> load() {
                List<IssueFilter> filters = newArrayList(cache.getIssueFilters());
                Collections.sort(filters, new Comparator<IssueFilter>() {

                    public int compare(IssueFilter lhs, IssueFilter rhs) {
                        int compare = lhs.getRepository().generateId()
                                .compareToIgnoreCase(rhs.getRepository().generateId());
                        if (compare == 0)
                            compare = lhs.toDisplay().toString().compareToIgnoreCase(rhs.toDisplay().toString());
                        return compare;
                    }
                });
                return filters;
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<IssueFilter> adapterFor(List<IssueFilter> items) {
        return new ViewHoldingListAdapter<IssueFilter>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.issue_filter_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                IssueFilterViewHolder.class, avatarHelper));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        IssueFilter filter = (IssueFilter) l.getItemAtPosition(position);
        startActivity(IssueBrowseActivity.createIntent(filter));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
