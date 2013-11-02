package com.github.mobile.ui.settings;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents;
import com.github.mobile.R;
import com.github.mobile.persistence.AccountDataManager;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.ui.repo.RecentRepositories;
import com.github.mobile.util.AvatarCache;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.PreferenceUtils;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockPreferenceActivity;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Manage the settings of the github app
 */
public class SettingsActivity extends RoboSherlockPreferenceActivity implements ConfirmDialogPreference.OnDialogClosed {

    @Inject
    private AccountDataManager accountDataManager;

    @Inject
    private AvatarLoader avatarLoader;

    User currentUser;

    SharedPreferences sharedPrefs;

    List<String> truePreferences;

    public static Intent intentForSettings(Context context, User currentUser) {
        Intent launchSettings = new Intent(context, SettingsActivity.class);
        launchSettings.putExtra(Intents.EXTRA_USER, currentUser);
        return launchSettings;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        setDialogListeners();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = (User) getIntent().getExtras().getSerializable(
            Intents.EXTRA_USER);
        sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(this);
        truePreferences = new ArrayList<String>(4);
    }

    @Override
    public void onDiaglogClosed(String key, boolean result) {
        if(key.equals(getString(R.string.key_clear_recent_repositories)) &&
            sharedPrefs.getBoolean(key, false))
            reloadRecentRepositories();
        else if (key.equals(getString(R.string.key_clear_avatars)) &&
            sharedPrefs.getBoolean(key, false)) {
            // First, we clear the files, then the cache itself
            avatarLoader.clearAvatarFiles();
            AvatarCache.getInstance().clearCache();
            ToastUtils.show(this, getString(R.string.success_clearing_avatars));
        } else if (key.equals(getString(R.string.key_reload_repositories)) &&
            sharedPrefs.getBoolean(key, false))
            updateRepositories();
        else if (key.equals(getString(R.string.key_reload_organizations)) &&
            sharedPrefs.getBoolean(key, false))
            reloadOrganizations();

        // If the user has confirmed a preference and then selected it a
        // second time and chose cancel, we still want to persist true
        // so that the HomeActivity UI can update accordingly
        if (!truePreferences.contains(key) && result) {
            truePreferences.add(key);
        } else if (truePreferences.contains(key)) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(key, true);
            PreferenceUtils.save(editor);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDialogListeners() {
        ConfirmDialogPreference reloadOrganizations = (ConfirmDialogPreference) findPreference(getString(R.string.key_reload_organizations));
        ConfirmDialogPreference reloadRepositories = (ConfirmDialogPreference) findPreference(getString(R.string.key_reload_repositories));
        ConfirmDialogPreference clearAvatars = (ConfirmDialogPreference) findPreference(getString(R.string.key_clear_avatars));
        ConfirmDialogPreference clearRecentRepos = (ConfirmDialogPreference) findPreference(getString(R.string.key_clear_recent_repositories));

        reloadOrganizations.setOnDialogClosedListener(this);
        reloadRepositories.setOnDialogClosedListener(this);
        clearAvatars.setOnDialogClosedListener(this);
        clearRecentRepos.setOnDialogClosedListener(this);
    }

    private void reloadRecentRepositories() {
        new ProgressDialogTask<Void>(this) {
            @Override
            protected Void run(Account account) throws IOException {
                List<User> orgs = accountDataManager.getOrgs(false);
                for (User org: orgs) {
                    RecentRepositories cache = new
                        RecentRepositories(SettingsActivity.this, org);
                    cache.clearAll();
                    cache.saveAsync();
                }
                return null;
            }

            @Override
            public void execute() {
                showIndeterminate(getString(R.string
                    .updating_recent_repositories));

                super.execute();
            }

            @Override
            protected void onSuccess(Void result) throws Exception {
                super.onSuccess(result);
                ToastUtils.show(SettingsActivity.this, R.string.
                    success_clearing_recent_repositories);
            }
        }.execute();
    }

    private void reloadOrganizations() {
        new ProgressDialogTask<Void>(this) {
            @Override
            protected Void run(Account account) throws IOException {
                accountDataManager.getOrgs(true);
                return null;
            }

            @Override
            public void execute() {
                showIndeterminate(getString(R.string.updating_organizations));

                super.execute();
            }

            @Override
            protected void onSuccess(Void result) throws Exception {
                super.onSuccess(result);
                ToastUtils.show(SettingsActivity.this, R.string
                    .success_updating_organizations);
            }
        }.execute();
    }

    private void updateRepositories() {
        new ProgressDialogTask<Void>(this) {
            @Override
            protected Void run(Account account) throws IOException {
                // We really just want to force a reload, not actually do
                // anything with the information
                accountDataManager.getRepos(currentUser, true);
                return null;
            }

            @Override
            public void execute() {
                showIndeterminate(getString(R.string.updating_repositories));
                super.execute();
            }

            @Override
            protected void onSuccess(Void result) throws Exception {
                super.onSuccess(result);
                ToastUtils.show(SettingsActivity.this, R.string
                    .success_updating_repositories);
            }
        }.execute();
    }
}