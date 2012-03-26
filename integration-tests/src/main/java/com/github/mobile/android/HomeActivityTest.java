package com.github.mobile.android;

import android.test.ActivityInstrumentationTestCase2;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    public void testHomeActivityCanActuallyStart() {
        getActivity(); // a smoke-test for the Guice configuration
    }

}
