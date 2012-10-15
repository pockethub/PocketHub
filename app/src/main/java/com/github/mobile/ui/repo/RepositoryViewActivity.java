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
import static com.github.mobile.util.TypefaceUtils.ICON_CODE;
import static com.github.mobile.util.TypefaceUtils.ICON_COMMIT;
import static com.github.mobile.util.TypefaceUtils.ICON_ISSUE_OPEN;
import static com.github.mobile.util.TypefaceUtils.ICON_NEWS;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.repo.RefreshRepositoryTask;
import com.github.mobile.core.repo.RepositoryUtils;
import com.github.mobile.core.repo.StarRepositoryTask;
import com.github.mobile.core.repo.StarredRepositoryTask;
import com.github.mobile.core.repo.UnstarRepositoryTask;
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

    private boolean isStarring;

    private boolean starringStatusChecked;

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
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getSupportMenuInflater().inflate(menu.repository_star, optionsMenu);

        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem followItem = menu.findItem(id.m_star);

        followItem.setVisible(starringStatusChecked);
        followItem.setTitle(isStarring ? string.unstar : string.star);

        return super.onPrepareOptionsMenu(menu);
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
        configureTabPager();
        ViewUtils.setGone(loadingBar, true);
        setGone(false);
        checkStarringRepositoryStatus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_star:
            starRepository();
            return true;
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
        return new RepositoryPagerAdapter(this, repository.isHasIssues());
    }

    @Override
    protected int getContentView() {
        return layout.tabbed_progress_pager;
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
        case 0:
            return ICON_NEWS;
        case 1:
            return ICON_CODE;
        case 2:
            return ICON_COMMIT;
        case 3:
            return ICON_ISSUE_OPEN;
        default:
            return super.getIcon(position);
        }
    }

    private void starRepository() {
        if (isStarring)
            new UnstarRepositoryTask(this, repository) {

                @Override
                protected void onSuccess(Void v) throws Exception {
                    super.onSuccess(v);

                    isStarring = !isStarring;
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    super.onException(e);

                    ToastUtils.show(RepositoryViewActivity.this,
                            string.error_unstarring_repository);
                }
            }.start();
        else
            new StarRepositoryTask(this, repository) {

                @Override
                protected void onSuccess(Void v) throws Exception {
                    super.onSuccess(v);

                    isStarring = !isStarring;
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    super.onException(e);

                    ToastUtils.show(RepositoryViewActivity.this,
                            string.error_starring_repository);
                }
            }.start();
    }

    private void checkStarringRepositoryStatus() {
        starringStatusChecked = false;
        new StarredRepositoryTask(this, repository) {

            @Override
            protected void onSuccess(Boolean watching) throws Exception {
                super.onSuccess(watching);

                isStarring = watching;
                starringStatusChecked = true;
                invalidateOptionsMenu();
            }
        }.execute();
    }
}