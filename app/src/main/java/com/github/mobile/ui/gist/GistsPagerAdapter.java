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

import static com.github.mobile.Intents.EXTRA_GIST_ID;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.mobile.ui.FragmentStatePagerAdapter;

/**
 * Adapter to page through an array of Gists
 */
public class GistsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] ids;

    /**
     * @param activity
     * @param gistIds
     */
    public GistsPagerAdapter(SherlockFragmentActivity activity, String[] gistIds) {
        super(activity);

        this.ids = gistIds;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new GistFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_GIST_ID, ids[position]);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return ids.length;
    }
}
