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

import static com.github.mobile.util.GitHubIntents.EXTRA_GIST_FILE;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.TitleProvider;

import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

/**
 * Pager adapter for all the files in a given gist
 */
public class GistFilesPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

    private final GistFile[] files;

    /**
     * @param fm
     * @param gist
     */
    public GistFilesPagerAdapter(FragmentManager fm, Gist gist) {
        super(fm);
        Map<String, GistFile> gistFiles = gist.getFiles();
        if (gistFiles != null && !gistFiles.isEmpty())
            files = gistFiles.values().toArray(new GistFile[gistFiles.size()]);
        else
            files = new GistFile[0];
    }

    @Override
    public String getTitle(final int position) {
        return files[position].getFilename();
    }

    @Override
    public Fragment getItem(final int position) {
        GistFile file = files[position];
        Fragment fragment = new GistFileFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_GIST_FILE, file);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return files.length;
    }
}
