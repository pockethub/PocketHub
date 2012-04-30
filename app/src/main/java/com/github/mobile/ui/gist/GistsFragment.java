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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.github.mobile.RequestCodes.GIST_CREATE;
import static com.github.mobile.RequestCodes.GIST_VIEW;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.R.string;
import com.github.mobile.core.gist.GistStore;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.PagedItemFragment;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a list of Gists
 */
public abstract class GistsFragment extends PagedItemFragment<Gist> {

    private static final String TAG = "GistsFragment";

    /**
     * Avatar helper
     */
    @Inject
    protected AvatarLoader avatarHelper;

    /**
     * Gist service
     */
    @Inject
    protected GistService service;

    /**
     * Gist store
     */
    @Inject
    protected GistStore store;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(ViewGistsActivity.createIntent(items, position), GIST_VIEW);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(string.no_gists));
        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == GIST_VIEW || requestCode == GIST_CREATE)
                && (RESULT_OK == resultCode || RESULT_CANCELED == resultCode)) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadFinished(Loader<List<Gist>> loader, List<Gist> items) {
        Exception exception = getException(loader);
        if (exception != null) {
            Log.d(TAG, "Exception loading gists", exception);
            showError(exception, string.error_gists_load);
            showList();
            return;
        }

        super.onLoadFinished(loader, items);
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_gists;
    }

    @Override
    protected ItemListAdapter<Gist, ? extends ItemView> createAdapter(List<Gist> items) {
        return new GistListAdapter(avatarHelper, getActivity().getLayoutInflater(),
                items.toArray(new Gist[items.size()]));
    }
}
