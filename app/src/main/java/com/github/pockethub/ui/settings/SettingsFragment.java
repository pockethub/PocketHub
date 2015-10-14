package com.github.pockethub.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.github.pockethub.BuildConfig;
import com.github.pockethub.R;

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
	}
}
