package com.github.mobile.android;

import android.test.suitebuilder.annotation.MediumTest;
import org.eclipse.egit.github.core.service.UserService;
import roboguice.test.RoboUnitTestCase;

public class GuiceTest extends RoboUnitTestCase<GitHubTestApplication> {

    private static final String TAG = "GHAT";

    @MediumTest
    public void testGuiceConfigurationDoesNotBlowUp() {
        getInjector().getInstance(UserService.class);
    }

}
