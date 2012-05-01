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
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.util.AvatarLoader;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to view a user's various pages
 */
public class UserViewActivity extends RoboSherlockFragmentActivity implements OrganizationSelectionProvider {

    /**
     * Create intent for this activity
     *
     * @param user
     * @return intent
     */
    public static Intent createIntent(User user) {
        return new Builder("user.VIEW").user(user).toIntent();
    }

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Inject
    private AvatarLoader avatarHelper;

    @InjectExtra(EXTRA_USER)
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(user.getLogin());
        avatarHelper.bind(actionBar, user);

        pager.setAdapter(new UserPagerAdapter(getSupportFragmentManager(), getResources()));
        indicator.setViewPager(pager);
    }

    @Override
    public User addListener(OrganizationSelectionListener listener) {
        return user;
    }

    @Override
    public OrganizationSelectionProvider removeListener(OrganizationSelectionListener listener) {
        return this;
    }
}
