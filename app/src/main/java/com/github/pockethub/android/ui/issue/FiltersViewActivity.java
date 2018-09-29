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
package com.github.pockethub.android.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.issue.IssueFilter;
import com.github.pockethub.android.persistence.AccountDataManager;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.MainActivity;
import io.reactivex.disposables.CompositeDisposable;

import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.android.ui.issue.FilterListFragment.ARG_FILTER;
import static com.github.pockethub.android.ui.issue.FilterListFragment.REQUEST_DELETE;

/**
 * Activity to display a list of saved {@link IssueFilter} objects
 */
public class FiltersViewActivity extends BaseActivity {

    private CompositeDisposable disposables;

    /**
     * Create intent to browse issue filters
     *
     * @return intent
     */
    public static Intent createIntent() {
        return new Builder("repo.issues.filters.VIEW").toIntent();
    }

    @Inject
    protected AccountDataManager cache;

    private FilterListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.issues_filter_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.bookmarks);
        actionBar.setIcon(R.drawable.ic_bookmark_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        fragment = (FilterListFragment) getSupportFragmentManager()
            .findFragmentById(R.id.list);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (requestCode == REQUEST_DELETE && resultCode == RESULT_OK) {
            IssueFilter filter = arguments.getParcelable(ARG_FILTER);
            disposables.add(
                    cache.removeIssueFilter(filter)
                            .subscribe(response -> {
                                if (fragment != null) {
                                    fragment.listFetcher.forceRefresh();
                                }
                            })
            );
            return;
        }

        super.onDialogResult(requestCode, resultCode, arguments);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
