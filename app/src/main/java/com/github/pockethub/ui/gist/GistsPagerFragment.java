/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pockethub.ui.gist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.pockethub.R;
import com.github.pockethub.ui.TabPagerFragment;

import static com.github.pockethub.util.TypefaceUtils.ICON_PERSON;
import static com.github.pockethub.util.TypefaceUtils.ICON_STAR;
import static com.github.pockethub.util.TypefaceUtils.ICON_TEAM;

public class GistsPagerFragment extends TabPagerFragment<GistQueriesPagerAdapter> {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureTabPager();
    }

    private void randomGist() {
        new RandomGistTask(this.getActivity()).start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_gists, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_random:
                randomGist();
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
}
