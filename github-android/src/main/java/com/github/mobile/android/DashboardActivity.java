package com.github.mobile.android;


import android.os.Bundle;
import android.util.Log;
import roboguice.activity.RoboActivity;

import static com.github.mobile.android.authenticator.AccountAuthenticatorService.addAccount;

public class DashboardActivity extends RoboActivity {
    private static final String TAG = "DA";

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
    }

}
