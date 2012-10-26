/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.Intents.EXTRA_GIST;
import static com.github.mobile.Intents.EXTRA_GIST_ID;
import static com.github.mobile.Intents.EXTRA_GIST_IDS;
import static com.github.mobile.Intents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.drawable;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.ui.ConfirmDialogFragment;
import com.github.mobile.ui.FragmentProvider;
import com.github.mobile.ui.PagerActivity;
import com.github.mobile.ui.UrlLauncher;
import com.github.mobile.ui.ViewPager;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;

import java.io.Serializable;
import java.util.List;

import org.eclipse.egit.github.core.Gist;

/**
 * Activity to display a collection of Gists in a pager
 */
public class GistsViewActivity extends PagerActivity {

    private static final int REQUEST_CONFIRM_DELETE = 1;

    /**
     * Create an intent to show a single gist
     *
     * @param gist
     * @return intent
     */
    public static Intent createIntent(Gist gist) {
        return new Builder("gists.VIEW").gist(gist).add(EXTRA_POSITION, 0)
                .toIntent();
    }

    /**
     * Create an intent to show gists with an initial selected Gist
     *
     * @param gists
     * @param position
     * @return intent
     */
    public static Intent createIntent(List<Gist> gists, int position) {
        String[] ids = new String[gists.size()];
        int index = 0;
        for (Gist gist : gists)
            ids[index++] = gist.getId();
        return new Builder("gists.VIEW")
                .add(EXTRA_GIST_IDS, (Serializable) ids)
                .add(EXTRA_POSITION, position).toIntent();
    }

    private ViewPager pager;

    private String[] gists;

    private Gist gist;

    private int initialPosition;

    @Inject
    private GistStore store;

    @Inject
    private AvatarLoader avatars;

    private GistsPagerAdapter adapter;

    private final UrlLauncher urlLauncher = new UrlLauncher(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager);

        gists = getStringArrayExtra(EXTRA_GIST_IDS);
        gist = getSerializableExtra(EXTRA_GIST);
        initialPosition = getIntExtra(EXTRA_POSITION);
        pager = finder.find(id.vp_pages);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Support opening this activity with a single Gist that may be present
        // in the intent but not currently present
        // in the store
        if (gists == null && gist != null) {
            String id = gist.getId();
            if (gist.getCreatedAt() != null) {
                Gist stored = store.getGist(id);
                if (stored == null)
                    store.addGist(gist);
            }
            gists = new String[] { gist.getId() };
        }

        adapter = new GistsPagerAdapter(this, gists);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.scheduleSetItem(initialPosition, this);
        onPageSelected(initialPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, GistsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        case id.m_delete:
            String gistId = gists[pager.getCurrentItem()];
            Bundle args = new Bundle();
            args.putString(EXTRA_GIST_ID, gistId);
            ConfirmDialogFragment.show(this, REQUEST_CONFIRM_DELETE,
                    getString(string.confirm_gist_delete_title),
                    getString(string.confirm_gist_delete_message), args);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (REQUEST_CONFIRM_DELETE == requestCode && RESULT_OK == resultCode) {
            final String gistId = arguments.getString(EXTRA_GIST_ID);
            new DeleteGistTask(this, gistId).start();
            return;
        }

        super.onDialogResult(requestCode, resultCode, arguments);
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        ActionBar actionBar = getSupportActionBar();
        String gistId = gists[position];
        Gist gist = store.getGist(gistId);
        if (gist == null) {
            actionBar.setSubtitle(null);
            actionBar.setLogo(null);
            actionBar.setIcon(drawable.app_icon);
        } else if (gist.getUser() != null) {
            avatars.bind(actionBar, gist.getUser());
            actionBar.setSubtitle(gist.getUser().getLogin());
        } else {
            actionBar.setSubtitle(string.anonymous);
            actionBar.setLogo(null);
            actionBar.setIcon(drawable.app_icon);
        }
        actionBar.setTitle(getString(string.gist_title) + gistId);
    }

    @Override
    public void startActivity(Intent intent) {
        Intent converted = urlLauncher.convert(intent);
        if (converted != null)
            super.startActivity(converted);
        else
            super.startActivity(intent);
    }

    protected FragmentProvider getProvider() {
        return adapter;
    }
}
