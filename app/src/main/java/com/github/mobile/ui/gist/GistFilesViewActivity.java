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
import static com.github.mobile.Intents.EXTRA_GIST_ID;
import static com.github.mobile.Intents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.Intents.Builder;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.util.AvatarLoader;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;

/**
 * Activity to page through the content of all the files in a Gist
 */
public class GistFilesViewActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to show files with an initial selected file
     *
     * @param gist
     * @param position
     * @return intent
     */
    public static Intent createIntent(Gist gist, int position) {
        return new Builder("gist.files.VIEW").gist(gist.getId())
                .add(EXTRA_POSITION, position).toIntent();
    }

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    private Gist gist;

    @Inject
    private GistStore store;

    @Inject
    private AvatarLoader avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);

        gist = store.getGist(gistId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(string.gist) + ' ' + gistId);
        User author = gist.getUser();
        if (author != null) {
            actionBar.setSubtitle(author.getLogin());
            avatarHelper.bind(actionBar, author);
        } else
            actionBar.setSubtitle(string.anonymous);

        ViewPager pager = (ViewPager) findViewById(id.vp_pages);
        pager.setAdapter(new GistFilesPagerAdapter(getSupportFragmentManager(),
                gist));
        ((TitlePageIndicator) findViewById(id.tpi_header)).setViewPager(pager);

        pager.setCurrentItem(initialPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = GistsViewActivity.createIntent(gist);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
