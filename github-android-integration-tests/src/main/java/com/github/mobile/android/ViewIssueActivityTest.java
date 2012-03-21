package com.github.mobile.android;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED;
import static android.nfc.NfcAdapter.EXTRA_NDEF_MESSAGES;
import static com.github.mobile.android.test.TestUserAccountUtil.ensureValidGitHubAccountAvailable;
import static com.github.mobile.android.util.Beam.createJsonNdefMessage;
import static com.github.mobile.android.util.Beam.isBeamApiAvailable;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import android.content.Intent;
import android.os.Parcelable;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import com.github.mobile.android.issue.ViewIssueActivity;
import com.github.mobile.android.util.Beam;
import com.jayway.android.robotium.solo.Solo;

public class ViewIssueActivityTest extends ActivityInstrumentationTestCase2<ViewIssueActivity> {


    private Solo solo;
    private static final String TAG = "ViewIssueActivityTest";

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
        if (!solo.getAllOpenedActivities().isEmpty()) {
            Log.d(TAG, "tearDown will finishOpenedActivities()");
            solo.finishOpenedActivities(); // this method hangs if no activities have been started in the test
        }
    }

    @MediumTest
    public void testActivityCanBeStartedByOpenIntent() {
        Intent intent = new Intent("com.github.mobile.android.repo.issue.VIEW");
        intent.putExtra(EXTRA_REPOSITORY_OWNER, "github");
        intent.putExtra(EXTRA_REPOSITORY_NAME, "gauges-android");
        intent.putExtra(EXTRA_ISSUE_NUMBER, 6);

        startActivityWith(intent);
        solo.waitForText("websocket");
        assertThat(solo.searchText("Maven dependencies"), is(true));
    }

    /**
     * This test _simulates_ the phone receiving a Beam message, as actually
     * sending a real Beam message to the device would require physical arrangement.
     */
    @MediumTest
    public void testActivityCanBeStartedByBeamIntent() {
        if (!isBeamApiAvailable()) {
            Log.i(TAG, "Skipping Beam test, device doesn't support Beam API");
            return;
        }

        Intent intent = new Intent(ACTION_NDEF_DISCOVERED);
        intent.setType(Beam.mimeTypeFor("repo.issue"));
        String jsonPayload = "{'number':25, 'html_url':'https://github.com/rtyley/agit/issues/25'}";
        intent.putExtra(EXTRA_NDEF_MESSAGES, new Parcelable[] { createJsonNdefMessage("repo.issue", jsonPayload) });
        startActivityWith(intent);

        solo.waitForText("PeriodSync");
        assertThat(solo.searchText("Agit PeriodSync fails to install"), is(true));
    }

    private void startActivityWith(Intent intent) {
        intent.setFlags(intent.getFlags() | FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
    }

}
