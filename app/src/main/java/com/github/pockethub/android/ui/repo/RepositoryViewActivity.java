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
package com.github.pockethub.android.ui.repo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.repo.RepositoryUtils;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.ui.TabPagerActivity;
import com.github.pockethub.android.ui.user.UriLauncherActivity;
import com.github.pockethub.android.ui.user.UserViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.activity.StarringService;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;
import com.meisolsson.githubsdk.service.repositories.RepositoryForkService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.google.inject.Inject;


import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY;
import static com.github.pockethub.android.ResultCodes.RESOURCE_CHANGED;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_CODE;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_COMMIT;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_ISSUE_OPEN;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_NEWS;

/**
 * Activity to view a repository
 */
public class RepositoryViewActivity extends TabPagerActivity<RepositoryPagerAdapter> {
    public static final String TAG = "RepositoryViewActivity";

    /**
     * Create intent for this activity
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Builder("repo.VIEW").repo(repository).toIntent();
    }

    private Repository repository;

    @Inject
    private AvatarLoader avatars;

    private ProgressBar loadingBar;

    private boolean isStarred;

    private boolean starredStatusChecked;

    private boolean hasReadme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = getParcelableExtra(EXTRA_REPOSITORY);

        loadingBar = finder.find(R.id.progress_bar);

        User owner = repository.owner();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.name());
        actionBar.setSubtitle(owner.login());
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (owner.avatarUrl() != null && RepositoryUtils.isComplete(repository))
            checkReadme();
        else {
            avatars.bind(getSupportActionBar(), owner);
            ViewUtils.setGone(loadingBar, false);
            setGone(true);
            ServiceGenerator.createService(this, RepositoryService.class)
                    .getRepository(repository.owner().login(), repository.name())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.<Repository>bindToLifecycle())
                    .subscribe(new ObserverAdapter<Repository>() {
                        @Override
                        public void onNext(Repository repo) {
                            repository = repo;
                            checkReadme();
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.show(RepositoryViewActivity.this, R.string.error_repo_load);
                            ViewUtils.setGone(loadingBar, true);
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getMenuInflater().inflate(R.menu.activity_repository, optionsMenu);
        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem followItem = menu.findItem(R.id.m_star);

        followItem.setVisible(starredStatusChecked);
        followItem.setTitle(isStarred ? R.string.unstar : R.string.star);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (adapter == null || pager.getCurrentItem() != adapter.getItemCode() || !adapter.onBackPressed())
            super.onBackPressed();
    }

    private void checkReadme() {
        ServiceGenerator.createService(this, RepositoryContentService.class)
                .hasReadme(repository.owner().login(), repository.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Response>bindToLifecycle())
                .subscribe(new ObserverAdapter<Response>() {
                    @Override
                    public void onNext(Response response) {
                        hasReadme = response.code() == 200;
                        configurePager();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hasReadme = false;
                        configurePager();
                    }
                });
    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), repository.owner());
        configureTabPager();
        ViewUtils.setGone(loadingBar, true);
        setGone(false);
        checkStarredRepositoryStatus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_star:
                starRepository();
                return true;
            case R.id.m_fork:
                forkRepository();
                return true;
            case R.id.m_contributors:
                startActivity(RepositoryContributorsActivity.createIntent(repository));
                return true;
            case R.id.m_share:
                shareRepository();
                return true;
            case R.id.m_delete:
                deleteRepository();
                return true;
            case R.id.m_refresh:
                checkStarredRepositoryStatus();
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                finish();
                Intent intent = UserViewActivity.createIntent(repository.owner());
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        adapter.onDialogResult(pager.getCurrentItem(), requestCode, resultCode, arguments);
    }

    @Override
    protected RepositoryPagerAdapter createAdapter() {
        return new RepositoryPagerAdapter(this, repository.hasIssues(), hasReadme);
    }

    @Override
    protected int getContentView() {
        return R.layout.tabbed_progress_pager;
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
        StarringService service = ServiceGenerator.createService(this, StarringService.class);

        Observable<Response<Boolean>> starObservable;
        if (isStarred) {
            starObservable = service.unstarRepository(repository.owner().login(), repository.name());
        } else {
            starObservable = service.starRepository(repository.owner().login(), repository.name());
        }

        starObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Response<Boolean>>bindToLifecycle())
                .subscribe(new ObserverAdapter<Response<Boolean>>() {
                    @Override
                    public void onNext(Response<Boolean> aBoolean) {
                        isStarred = !isStarred;
                        setResult(RESOURCE_CHANGED);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show(RepositoryViewActivity.this, isStarred ? R.string.error_unstarring_repository : R.string.error_starring_repository);
                    }
                });
    }

    private void checkStarredRepositoryStatus() {
        starredStatusChecked = false;
        ServiceGenerator.createService(this, StarringService.class)
                .checkIfRepositoryIsStarred(repository.owner().login(), repository.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Response<Boolean>>bindToLifecycle())
                .subscribe(new ObserverAdapter<Response<Boolean>>() {
                    @Override
                    public void onNext(Response<Boolean> response) {
                        isStarred = response.code() == 204;
                        starredStatusChecked = true;
                        invalidateOptionsMenu();
                    }
                });
    }

    private void shareRepository() {
        String repoUrl = repository.htmlUrl();
        if (TextUtils.isEmpty(repoUrl))
            repoUrl = "https://github.com/" + InfoUtils.createRepoId(repository);
        Intent sharingIntent = ShareUtils.create(InfoUtils.createRepoId(repository), repoUrl);
        startActivity(sharingIntent);
    }

    private void forkRepository() {
        ServiceGenerator.createService(this, RepositoryForkService.class)
                .createFork(repository.owner().login(), repository.name())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Repository>bindToLifecycle())
                .subscribe(new ObserverAdapter<Repository>() {
                    @Override
                    public void onNext(Repository repo) {
                        if (repo != null) {
                            UriLauncherActivity.launchUri(RepositoryViewActivity.this, Uri.parse(repo.htmlUrl()));
                        } else {
                            ToastUtils.show(RepositoryViewActivity.this, R.string.error_forking_repository);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show(RepositoryViewActivity.this, R.string.error_forking_repository);
                    }
                });
    }

    private void deleteRepository() {
        new MaterialDialog.Builder(this)
                .title(R.string.are_you_sure)
                .content(R.string.unexpected_bad_things)
                .positiveText(R.string.not_sure)
                .negativeText(R.string.delete_cap)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();

                        ServiceGenerator.createService(dialog.getContext(), RepositoryService.class)
                                .deleteRepository(repository.owner().login(), repository.name())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(RepositoryViewActivity.this.<Response<Boolean>>bindToLifecycle())
                                .subscribe(new ObserverAdapter<Response<Boolean>>() {
                                    @Override
                                    public void onNext(Response<Boolean> response) {
                                        onBackPressed();
                                        ToastUtils.show(RepositoryViewActivity.this, R.string.delete_successful);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        ToastUtils.show(RepositoryViewActivity.this, R.string.error_deleting_repository);
                                    }
                                });
                    }
                })
                .show();
    }
}
