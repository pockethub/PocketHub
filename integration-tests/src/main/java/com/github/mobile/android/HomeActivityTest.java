package com.github.mobile.android;

import static com.github.mobile.android.R.id.tv_org_name;
import static com.github.mobile.android.R.id.tv_repo_name;
import static com.github.mobile.android.repo.RecentReposHelper.REPO_NAME;
import static com.github.mobile.android.test.TestUserAccountUtil.ensureValidGitHubAccountAvailable;
import static com.github.mobile.android.util.AccountHelper.demandCurrentAccount;
import static com.github.rtyley.android.screenshot.celebrity.Screenshots.poseForScreenshot;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.ui.user.UserPagerAdapter;
import com.google.common.base.Joiner;
import com.jayway.android.robotium.solo.Solo;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private static final String TAG = "HomeActivityTest";

    private Solo solo;
    private Account account;
    private OrganizationService organizationService;
    private RepositoryService repositoryService;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        ensureValidGitHubAccountAvailable(getInstrumentation());
        solo = new Solo(getInstrumentation());

        AccountManager accountManager = AccountManager.get(getInstrumentation().getTargetContext());
        account = demandCurrentAccount(accountManager, null);
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setCredentials(account.name, accountManager.getPassword(account));

        organizationService = new OrganizationService(gitHubClient);
        repositoryService = new RepositoryService(gitHubClient);
    }

    @Override
    protected void tearDown() throws Exception {
        if (!solo.getAllOpenedActivities().isEmpty()) {
            Log.d(TAG, "tearDown will finishOpenedActivities()");
            solo.finishOpenedActivities(); // this method hangs if no activities have been started in the test
        }
    }

    @MediumTest
    public void testReposListedCorrectlyWhenUsingTheOrgSwitcher() throws Exception {
        HomeActivity activity = getActivity();

        List<User> orgs = organizationService.getOrganizations();

        poseForScreenshot();
        solo.sleep(6000);
        poseForScreenshot();
        solo.sleep(1000);
        poseForScreenshot();

        ViewPager viewPager = (ViewPager) solo.getView(ViewPager.class, 0);
        setViewPagerToIndex(viewPager, titlesOn(viewPager).indexOf("Repos"));

        solo.sleep(5000);
        poseForScreenshot();

        Log.d(TAG, "Got " + orgs.size() + " orgs for account " + account.name);
        for (int i = 0; i < orgs.size(); ++i) {
            setSelectedNavigationItem(activity, i + 1);
            solo.sleep(1000);
            poseForScreenshot();

            String orgName = textIn(activity.findViewById(tv_org_name));
            String repoIdsInOrg = loadRepoIdsFor(orgName, repositoryService);

            solo.sleep(1000);
            poseForScreenshot();

            String displayedRepoId = textIn(viewPager.findViewById(tv_repo_name)); // first repo shown in list
            Log.d(TAG, "org text = " + orgName + " repo name = " + displayedRepoId);
            poseForScreenshot();
            assertThat(repoIdsInOrg, containsString(displayedRepoId));
        }
    }

    private String loadRepoIdsFor(String orgName, RepositoryService repositoryService) throws IOException {
        List<Repository> orgRepositories = repositoryService.getOrgRepositories(orgName);
        return Joiner.on(",").join(transform(orgRepositories, REPO_NAME));
    }

    private String textIn(View textView) {
        return ((TextView) textView).getText().toString();
    }

    private List<String> titlesOn(ViewPager viewPager) {
        List<String> titles = newArrayList();
        UserPagerAdapter titleProvider = (UserPagerAdapter) viewPager.getAdapter();
        for (int i = 0; i < viewPager.getChildCount(); ++i) {
            titles.add(titleProvider.getTitle(i));
        }
        Log.d(TAG, "Found titles " + titles);
        return titles;
    }

    private void setViewPagerToIndex(final ViewPager viewPager, final int index) {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                viewPager.setCurrentItem(index);
            }
        });
    }

    private void setSelectedNavigationItem(final HomeActivity activity, final int index) {
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                activity.getSupportActionBar().setSelectedNavigationItem(index);
            }
        });
    }
}
