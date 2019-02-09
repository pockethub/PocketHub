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

package com.github.pockethub.android.ui;

import android.accounts.Account;
import android.os.Build;
import android.view.MenuItem;
import com.github.pockethub.android.AccountManagerShadow;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.gist.GistsPagerFragment;
import com.github.pockethub.android.ui.issue.FilterListFragment;
import com.github.pockethub.android.ui.issue.IssueDashboardPagerFragment;
import com.github.pockethub.android.ui.user.HomePagerFragment;
import com.meisolsson.githubsdk.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = AccountManagerShadow.class)
public class MainActivityTest {

    private MainActivity mainActivity;
    private ArgumentCaptor<Account> argumentCaptor;

    @Before
    public void setup() {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();

        List<User> org = new ArrayList<>();
        org.add(User.builder().build());

        mainActivity.onOrgsLoaded(org);
        argumentCaptor = ArgumentCaptor.forClass(Account.class);
    }

    private MenuItem getMockMenuItem(int id, String title) {
        MenuItem mockedMenuItem = mock(MenuItem.class);
        when(mockedMenuItem.getItemId()).thenReturn(id);
        when(mockedMenuItem.getTitle()).thenReturn(title);
        return mockedMenuItem;
    }

    @Test
    public void testNavigationDrawerClickListenerPos1_ShouldReplaceHomePagerFragmentToContainer() {
        mainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_home, "HomeTitle"));

        String expectedString = RuntimeEnvironment.application.getString(R.string.app_name);
        assertFragmentInstanceAndSupportActionBarTitle(HomePagerFragment.class, expectedString);
    }

    @Test
    public void testNavigationDrawerClickListenerPos2_ShouldReplaceGistsPagerFragmentToContainer() {
        mainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_gists, "GistTitle"));

        assertFragmentInstanceAndSupportActionBarTitle(GistsPagerFragment.class, "GistTitle");
    }

    @Test
    public void testNavigationDrawerClickListenerPos3_ShouldReplaceIssueDashboardPagerFragmentToContainer() {
        mainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_issue_dashboard, "IssueDashboard"));

        assertFragmentInstanceAndSupportActionBarTitle(IssueDashboardPagerFragment.class, "IssueDashboard");
    }

    @Test
    public void testNavigationDrawerClickListenerPos4_ShouldReplaceFilterListFragmentToContainer() {
        mainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_bookmarks, "Bookmarks"));

        assertFragmentInstanceAndSupportActionBarTitle(FilterListFragment.class, "Bookmarks");
    }

    @Test
    public void testNavigationDrawerClickListenerPos5_ShouldLogoutUser() {
        mainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_log_out, "Logout"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            verify(AccountManagerShadow.mockManager, times(2)).removeAccount(argumentCaptor.capture(), eq(mainActivity),
                    any(), any());
        } else {
            verify(AccountManagerShadow.mockManager, times(2)).removeAccount(argumentCaptor.capture(),
                    any(), any());
        }

        List<Account> values = argumentCaptor.getAllValues();
        assertThat(values.get(0), is(equalTo(AccountManagerShadow.accounts[0])));
        assertThat(values.get(1), is(equalTo(AccountManagerShadow.accounts[1])));
    }

    private void assertFragmentInstanceAndSupportActionBarTitle(Class expectedInstance, String expectedSupportActionBarTitle) {
        assertThat(mainActivity.getCurrentFragment(), is(instanceOf(expectedInstance)));
        assertThat(mainActivity.getSupportActionBar().getTitle().toString(), is(equalTo(expectedSupportActionBarTitle)));
    }

}
