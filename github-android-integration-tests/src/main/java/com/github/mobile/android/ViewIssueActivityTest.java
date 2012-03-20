package com.github.mobile.android;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.github.mobile.android.test.TestUserAccountUtil.ensureValidGitHubAccountAvailable;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.TextView;

import com.github.mobile.android.issue.ViewIssueActivity;
import com.github.mobile.android.test.TestUserAccountUtil;
import com.jayway.android.robotium.solo.Solo;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import junit.framework.Assert;

public class ViewIssueActivityTest extends ActivityInstrumentationTestCase2<ViewIssueActivity> {

    private Solo solo;

    public ViewIssueActivityTest() {
        super(ViewIssueActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        ensureValidGitHubAccountAvailable(getInstrumentation());
        solo = new Solo(getInstrumentation());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    @MediumTest
    public void testActivityCanBeStartedByOpenIntent() {
        Intent intent = new Intent("com.github.mobile.android.repo.issue.VIEW");
        intent.putExtra(EXTRA_REPOSITORY_OWNER , "github");
        intent.putExtra(EXTRA_REPOSITORY_NAME , "gauges-android");
        intent.putExtra(EXTRA_ISSUE_NUMBER , 6);

        startActivityWith(intent);
        solo.waitForText("websocket");
        assertThat(solo.searchText("Maven dependencies"), is(true));
    }

    private void startActivityWith(Intent intent) {
        intent.setFlags(intent.getFlags() | FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
    }
}
