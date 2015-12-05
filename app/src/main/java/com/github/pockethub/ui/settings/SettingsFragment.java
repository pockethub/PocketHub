package com.github.pockethub.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.pockethub.BuildConfig;
import com.github.pockethub.R;
import com.github.pockethub.ui.issue.EditIssueActivity;

/**
 * Created by larsgrefer on 14.10.15.
 */
public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		Preference version = findPreference("pref_version");
		version.setSummary(BuildConfig.VERSION_NAME + "\n" + BuildConfig.BUILD_TYPE);

		Preference build = findPreference("pref_build");
		build.setSummary(BuildConfig.GIT_SHA + "\n" + BuildConfig.BUILD_TIME);

		Preference bugreport = findPreference("pref_bugreport");
		Intent intent = EditIssueActivity.createIntent(null, "pockethub", "PocketHub", null);
		bugreport.setIntent(intent);
	}
}
