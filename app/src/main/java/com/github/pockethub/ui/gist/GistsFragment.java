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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.core.gist.GistStore;
import com.github.pockethub.ui.PagedItemFragment;
import com.github.pockethub.util.AvatarLoader;
import com.google.inject.Inject;

import java.util.List;

import static com.github.pockethub.RequestCodes.GIST_CREATE;
import static com.github.pockethub.RequestCodes.GIST_VIEW;

/**
 * Fragment to display a list of Gists
 */
public abstract class GistsFragment extends PagedItemFragment<Gist> {

    /**
     * Avatar loader
     */
    @Inject
    protected AvatarLoader avatars;

    /**
     * Gist store
     */
    @Inject
    protected GistStore store;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(GistsViewActivity.createIntent(items, position),
                GIST_VIEW);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_gists);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
        case R.id.m_create:
            startActivityForResult(new Intent(getActivity(),
                    CreateGistActivity.class), GIST_CREATE);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GIST_VIEW || requestCode == GIST_CREATE) {
            notifyDataSetChanged();
            forceRefresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_gists_load;
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_gists;
    }

    @Override
    protected SingleTypeAdapter<Gist> createAdapter(List<Gist> items) {
        return new GistListAdapter(avatars, getActivity(), items);
    }
}
