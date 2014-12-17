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
import static com.github.mobile.ui.gist.GistsFragment.GistStarUpdater;
import static com.github.mobile.util.TypefaceUtils.ICON_PERSON;
import static com.github.mobile.util.TypefaceUtils.ICON_STAR;
import static com.github.mobile.util.TypefaceUtils.ICON_TEAM;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.drawable;
import com.github.mobile.R.id;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.ui.TabPagerActivity;
import com.github.mobile.ui.user.HomeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Gist;

/**
 * Activity to display view pagers of different Gist queries
 */
public class GistsActivity extends TabPagerActivity<GistQueriesPagerAdapter>
    implements GistStarUpdater {

    private List<Gist> starredGists;

    private List<Gist> unstarredGists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.gists_title);
        actionBar.setIcon(drawable.action_gist);
        actionBar.setDisplayHomeAsUpEnabled(true);

        configureTabPager();

        starredGists = new ArrayList<Gist>();
        unstarredGists = new ArrayList<Gist>();
    }

    private void randomGist() {
        new RandomGistTask(this).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getSupportMenuInflater().inflate(menu.gists, optionsMenu);

        return super.onCreateOptionsMenu(optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.m_random:
            randomGist();
            return true;
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected GistQueriesPagerAdapter createAdapter() {
        return new GistQueriesPagerAdapter(this);
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
        case 0:
            return ICON_PERSON;
        case 1:
            return ICON_STAR;
        case 2:
            return ICON_TEAM;
        default:
            return super.getIcon(position);
        }
    }

    @Override
    public void onGistsStarsChanged(List<Gist> starred, List<Gist> unstarred) {
        cleanGistCaches(starred, starredGists, unstarredGists);
        cleanGistCaches(unstarred, unstarredGists, starredGists);

        Comparator gistTimeCompare = new Comparator<Gist>() {
            @Override
            public int compare(Gist lhs, Gist rhs) {
                if (lhs.getCreatedAt().getTime() <
                    rhs.getCreatedAt().getTime())
                    return 1;
                else if (lhs.getCreatedAt().getTime() >
                    rhs.getCreatedAt().getTime())
                    return -1;
                else
                    return 0;
            }
        };

        Collections.sort(starredGists, gistTimeCompare);
        Collections.sort(unstarredGists, gistTimeCompare);

    }

    private void cleanGistCaches(List<Gist> newGists,
        List<Gist> oldGists, List<Gist> oppositeGists) {
        for (Gist newGist: newGists) {
            // Gist doesn't have an equals method, so we can't call contains
            boolean foundMatch = false;
            for(Gist cachedGist: oldGists) {
                if (newGist.getId().equals(cachedGist.getId())) {
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch)
                oldGists.add(newGist);

            for(int i = 0; i < oppositeGists.size(); i++) {
                if (oppositeGists.get(i).getId().equals(
                    newGist.getId())) {
                    oppositeGists.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    public List<Gist> getStarredGists() {
        return starredGists;
    }

    @Override
    public List<Gist> getUnstarredGists() {
        return unstarredGists;
    }

    @Override
    public void clearStarsCache() {
        starredGists.clear();
        unstarredGists.clear();
    }
}
