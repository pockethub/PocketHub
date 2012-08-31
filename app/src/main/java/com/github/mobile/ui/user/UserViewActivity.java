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
package com.github.mobile.ui.user;

import static com.github.mobile.Intents.EXTRA_USER;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.user.RefreshUserTask;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

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

    @InjectExtra(EXTRA_USER)
    private User user;

    @InjectView(id.pb_loading)
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(user.getLogin());

        if (user.getAvatarUrl() != null)
            configurePager();
        else {
            createTabPager();
            ViewUtils.setGone(loadingBar, false);
            ViewUtils.setGone(host, true);
            ViewUtils.setGone(pager, true);
            new RefreshUserTask(this, user.getLogin()) {

                @Override
                protected void onSuccess(User fullUser) throws Exception {
                    super.onSuccess(fullUser);

                    user = fullUser;
                    configurePager();
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    super.onException(e);

                    ToastUtils.show(UserViewActivity.this,
                            string.error_person_load);
                    ViewUtils.setGone(loadingBar, true);
                }
            }.execute();
        }
    }

    private void configurePager() {
        avatars.bind(getSupportActionBar(), user);
        createTabPager();
        ViewUtils.setGone(loadingBar, true);
        ViewUtils.setGone(host, false);
        ViewUtils.setGone(pager, false);
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
        return new UserPagerAdapter(getSupportFragmentManager(), getResources());
    }

    @Override
    protected int getContentView() {
        return layout.tabbed_progress_pager;
    }
}
