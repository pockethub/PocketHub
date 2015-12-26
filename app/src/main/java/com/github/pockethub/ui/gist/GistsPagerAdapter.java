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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.github.pockethub.ui.FragmentStatePagerAdapter;

import static com.github.pockethub.Intents.EXTRA_GIST_ID;

/**
 * Adapter to page through an array of Gists
 */
public class GistsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] ids;

    private final SparseArray<GistFragment> fragments = new SparseArray<>();

    /**
     * @param activity
     * @param gistIds
     */
    public GistsPagerAdapter(AppCompatActivity activity, String[] gistIds) {
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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        fragments.remove(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        if (fragment instanceof GistFragment)
            fragments.put(position, (GistFragment) fragment);
        return fragment;
    }

    /**
     * Deliver dialog result to fragment at given position
     *
     * @param position
     * @param requestCode
     * @param resultCode
     * @param arguments
     * @return this adapter
     */
    public GistsPagerAdapter onDialogResult(int position, int requestCode,
            int resultCode, Bundle arguments) {
        GistFragment fragment = fragments.get(position);
        if (fragment != null)
            fragment.onDialogResult(requestCode, resultCode, arguments);
        return this;
    }
}
