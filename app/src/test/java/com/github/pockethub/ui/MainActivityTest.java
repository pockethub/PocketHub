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

package com.github.pockethub.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.alorma.github.sdk.bean.dto.response.Organization;
import com.github.pockethub.BuildConfig;
import com.github.pockethub.R;
import com.github.pockethub.ui.gist.GistsPagerFragment;
import com.github.pockethub.ui.issue.FilterListFragment;
import com.github.pockethub.ui.issue.IssueDashboardPagerFragment;
import com.github.pockethub.ui.user.HomePagerFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    private MockMainActivity mockMainActivity;
    static Fragment fragment;
    static AccountManager mockManager;
    private ArgumentCaptor<Account> argumentCaptor;
    private Account[] accounts;

    @Before
    public void setup() {
        mockMainActivity = Robolectric.buildActivity(MockMainActivity.class).create().get();
        List<Organization> org = new ArrayList<>();
        org.add(new Organization());
        Account firstGitHubAccount = new Account("GitHubAccount", "com.github");
        Account secondGitHubAccount = new Account("GitHubAccount2", "com.github");
        accounts = new Account[]{firstGitHubAccount, secondGitHubAccount};
        mockManager = mock(AccountManager.class);
        when(mockManager.getAccountsByType(RuntimeEnvironment.application.getString(R.string.account_type))).thenReturn(accounts);
        mockMainActivity.onLoadFinished(null, org);
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
        mockMainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_home, "HomeTitle"));

        String expectedString = RuntimeEnvironment.application.getString(R.string.app_name);
        assertFragmentInstanceAndSupportActionBarTitle(HomePagerFragment.class, expectedString);
    }

    @Test
    public void testNavigationDrawerClickListenerPos2_ShouldReplaceGistsPagerFragmentToContainer() {
        mockMainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_gists, "GistTitle"));

        assertFragmentInstanceAndSupportActionBarTitle(GistsPagerFragment.class, "GistTitle");
    }

    @Test
    public void testNavigationDrawerClickListenerPos3_ShouldReplaceIssueDashboardPagerFragmentToContainer() {
        mockMainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_issue_dashboard, "IssueDashboard"));

        assertFragmentInstanceAndSupportActionBarTitle(IssueDashboardPagerFragment.class, "IssueDashboard");
    }

    @Test
    public void testNavigationDrawerClickListenerPos4_ShouldReplaceFilterListFragmentToContainer() {
        mockMainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_bookmarks, "Bookmarks"));

        assertFragmentInstanceAndSupportActionBarTitle(FilterListFragment.class, "Bookmarks");
    }

    @Test
    public void testNavigationDrawerClickListenerPos5_ShouldLogoutUser() {
        mockMainActivity.onNavigationItemSelected(getMockMenuItem(R.id.navigation_log_out, "Logout"));

        verify(mockManager, times(2)).removeAccount(argumentCaptor.capture(), (AccountManagerCallback<Boolean>) anyObject(), (Handler) anyObject());
        List<Account> values = argumentCaptor.getAllValues();
        assertThat(values.get(0), is(equalTo(accounts[0])));
        assertThat(values.get(1), is(equalTo(accounts[1])));
    }

    private void assertFragmentInstanceAndSupportActionBarTitle(Class expectedInstance, String expectedSupportActionBarTitle) {
        assertThat(fragment, is(instanceOf(expectedInstance)));
        assertThat(mockMainActivity.getSupportActionBar().getTitle().toString(), is(equalTo(expectedSupportActionBarTitle)));
    }

    public static class MockMainActivity extends MainActivity {

        @Override
        void switchFragment(Fragment frag, Organization org) {
            super.switchFragment(frag, org);
            fragment = frag;
        }

        @Override
        AccountManager getAccountManager() {
            return mockManager;
        }
    }
}
