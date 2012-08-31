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
package com.github.mobile.ui.repo;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.Intents.EXTRA_REPOSITORY;
import static com.github.mobile.ui.repo.RepositoryPagerAdapter.ITEM_CODE;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.repo.RefreshRepositoryTask;
import com.github.mobile.core.repo.RepositoryUtils;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.ui.user.HomeActivity;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to view a repository
 */
public class RepositoryViewActivity extends
        TabPagerActivity<RepositoryPagerAdapter> {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Builder("repo.VIEW").repo(repository).toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @Inject
    private AvatarLoader avatars;

    @InjectView(id.pb_loading)
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User owner = repository.getOwner();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.getName());
        actionBar.setSubtitle(owner.getLogin());
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (owner.getAvatarUrl() != null
                && RepositoryUtils.isComplete(repository))
            configurePager();
        else {
            createTabPager();
            avatars.bind(getSupportActionBar(), owner);
            ViewUtils.setGone(loadingBar, false);
            setGone(true);
            new RefreshRepositoryTask(this, repository) {

                @Override
                protected void onSuccess(Repository fullRepository)
                        throws Exception {
                    super.onSuccess(fullRepository);

                    repository = fullRepository;
                    configurePager();
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    super.onException(e);

                    ToastUtils.show(RepositoryViewActivity.this,
                            string.error_repo_load);
                    ViewUtils.setGone(loadingBar, true);
                }
            }.execute();
        }
    }

    @Override
    public boolean onSearchRequested() {
        if (pager.getCurrentItem() == 1) {
            Bundle args = new Bundle();
            args.putSerializable(EXTRA_REPOSITORY, repository);
            startSearch(null, false, args, false);
            return true;
        } else
            return false;
    }

    @Override
    public void onBackPressed() {
        if (adapter == null || pager.getCurrentItem() != ITEM_CODE
                || !adapter.onBackPressed())
            super.onBackPressed();
    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), repository.getOwner());
        createTabPager();
        ViewUtils.setGone(loadingBar, true);
        setGone(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode,
                arguments);
    }

    @Override
    protected RepositoryPagerAdapter createAdapter() {
        return new RepositoryPagerAdapter(getSupportFragmentManager(),
                getResources(), repository.isHasIssues());
    }

    @Override
    protected int getContentView() {
        return layout.tabbed_progress_pager;
    }
}
