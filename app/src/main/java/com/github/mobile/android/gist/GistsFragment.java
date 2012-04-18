package com.github.mobile.android.gist;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.github.mobile.android.RequestCodes.GIST_CREATE;
import static com.github.mobile.android.RequestCodes.GIST_VIEW;
import static java.util.Collections.sort;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.ListLoadingFragment;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a list of Gists
 */
public abstract class GistsFragment extends ListLoadingFragment<Gist> implements Comparator<Gist> {

    private static final String TAG = "GistsFragment";

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

    /**
     * Gist id field
     */
    protected TextView gistId;

    /**
     * Width of id column of in Gist list
     */
    protected final AtomicReference<Integer> idWidth = new AtomicReference<Integer>();

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivityForResult(ViewGistsActivity.createIntent(listItems, position), GIST_VIEW);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_gists));
        ListViewHelper.configure(getActivity(), getListView(), true);
        gistId = (TextView) getLayoutInflater(savedInstanceState).inflate(layout.gist_list_item, null).findViewById(
                id.tv_gist_id);
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
        idWidth.set(GistViewHolder.computeIdWidth(items, gistId));

        super.onLoadFinished(loader, items);
    }

    @Override
    public int compare(final Gist g1, final Gist g2) {
        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
    }

    /**
     * Store the given collections of Gists and return the same collection but sorted using {@link #compare(Gist, Gist)}
     *
     * @param gists
     * @return non-null but possibly empty list of sorted gists
     */
    protected List<Gist> storeAndSort(Collection<Gist> gists) {
        List<Gist> gistList = new ArrayList<Gist>(gists.size());
        for (Gist gist : gists)
            gistList.add(store.addGist(gist));
        sort(gistList, this);
        return gistList;
    }
}
