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
package com.github.pockethub.android.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.ui.MainActivity;
import com.github.pockethub.android.ui.TabPagerActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.service.users.UserFollowerService;
import com.meisolsson.githubsdk.service.users.UserService;
import com.google.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.android.Intents.EXTRA_USER;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_FOLLOW;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_NEWS;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_PUBLIC;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_WATCH;


/**
 * Activity to view a user's various pages
 */
public class UserViewActivity extends TabPagerActivity<UserPagerAdapter>
    implements OrganizationSelectionProvider {

    /**
     * Create intent for this activity
     *
     * @param user
     * @return intent
     */
    public static Intent createIntent(User user) {
        return new Builder("user.VIEW").user(user).toIntent();
    }

    @Inject
    private AvatarLoader avatars;

    private User user;

    private ProgressBar loadingBar;

    private boolean isFollowing;

    private boolean followingStatusChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = getIntent().getParcelableExtra(EXTRA_USER);
        loadingBar = (ProgressBar) findViewById(R.id.pb_loading);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(user.login());

        if (!TextUtils.isEmpty(user.avatarUrl())) {
            configurePager();
        } else {
            loadingBar.setVisibility(View.VISIBLE);
            setGone(true);
            ServiceGenerator.createService(this, UserService.class)
                    .getUser(user.login())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.bindToLifecycle())
                    .subscribe(response -> {
                        user = response.body();
                        configurePager();
                    }, e -> {
                        ToastUtils.show(this, R.string.error_person_load);
                        loadingBar.setVisibility(View.GONE);
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getMenuInflater().inflate(R.menu.activity_user_follow, optionsMenu);

        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem followItem = menu.findItem(R.id.m_follow);
        boolean isCurrentUser = user.login().equals(AccountUtils.getLogin(this));

        followItem.setVisible(followingStatusChecked && !isCurrentUser);
        followItem.setTitle(isFollowing ? R.string.unfollow : R.string.follow);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_follow:
                followUser();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), user);
        configureTabPager();
        loadingBar.setVisibility(View.GONE);
        setGone(false);
        checkFollowingUserStatus();
    }

    @Override
    public User addListener(OrganizationSelectionListener listener) {
        return user;
    }

    @Override
    public OrganizationSelectionProvider removeListener(
        OrganizationSelectionListener listener) {
        return this;
    }

    @Override
    protected UserPagerAdapter createAdapter() {
        return new UserPagerAdapter(this);
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
                return ICON_PUBLIC;
            case 2:
                return ICON_WATCH;
            case 3:
                return ICON_FOLLOW;
            default:
                return super.getIcon(position);
        }
    }

    private void followUser() {
        UserFollowerService service = ServiceGenerator.createService(this, UserFollowerService.class);

        Single<Response<Boolean>> followSingle;
        if (isFollowing) {
            followSingle = service.unfollowUser(user.login());
        } else{
            followSingle = service.followUser(user.login());
        }

        followSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(aBoolean -> isFollowing = !isFollowing,
                        e -> ToastUtils.show(this, isFollowing ? R.string.error_unfollowing_person : R.string.error_following_person));
    }

    private void checkFollowingUserStatus() {
        followingStatusChecked = false;
        ServiceGenerator.createService(this, UserFollowerService.class)
                .isFollowing(user.login())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> {
                    isFollowing = response.code() == 204;
                    followingStatusChecked = true;
                    invalidateOptionsMenu();
                });
    }
}
