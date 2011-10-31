package com.github.mobile.android;

import android.test.ActivityInstrumentationTestCase2;

public class DashboardActivityTest extends ActivityInstrumentationTestCase2<DashboardActivity> {

	public DashboardActivityTest() {
		super("com.github.mobile.android.app", DashboardActivity.class);
	}

	public void testDashboardActivityCanActuallyStart() {
		getActivity(); // a smoke-test for the Guice configuration
	}

}
