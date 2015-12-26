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
package com.github.pockethub.ui.issue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.github.pockethub.Intents.Builder;
import com.github.pockethub.R;
import com.github.pockethub.RequestFuture;
import com.github.pockethub.core.issue.IssueFilter;
import com.github.pockethub.persistence.AccountDataManager;
import com.github.pockethub.ui.ConfirmDialogFragment;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.MainActivity;
import com.google.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Activity to display a list of saved {@link com.github.pockethub.core.issue.IssueFilter} objects
 */
public class FiltersViewFragment extends DialogFragment implements
    OnItemLongClickListener {

    /**
     * Create intent to browse issue filters
     *
     * @return intent
     */
    public static Intent createIntent() {
        return new Builder("repo.issues.filters.VIEW").toIntent();
    }

    private static final String ARG_FILTER = "filter";

    private static final int REQUEST_DELETE = 1;

    @Inject
    private AccountDataManager cache;

    private FilterListFragment fragment;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.issues_filter_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragment = (FilterListFragment) getChildFragmentManager().findFragmentById(android.R.id.list);
        fragment.getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (requestCode == REQUEST_DELETE && resultCode == Activity.RESULT_OK) {
            IssueFilter filter = arguments.getParcelable(ARG_FILTER);
            cache.removeIssueFilter(filter, new RequestFuture<IssueFilter>() {

                @Override
                public void success(IssueFilter response) {
                    if (fragment != null)
                        fragment.refresh();
                }
            });
            return;
        }

        super.onDialogResult(requestCode, resultCode, arguments);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
        int position, long id) {
        IssueFilter filter = (IssueFilter) parent.getItemAtPosition(position);
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        ConfirmDialogFragment.show(getActivity(), REQUEST_DELETE,
            getString(R.string.confirm_bookmark_delete_title),
            getString(R.string.confirm_bookmark_delete_message), args);
        return true;
    }
}
