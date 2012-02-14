package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a list of Gists
 */
public class GistsFragment extends ListLoadingFragment<Gist> implements Comparator<Gist> {

    private OnItemClickListener clickListener;

    @Inject
    private GistService service;

    /**
     * @param clickListener
     * @return this fragment
     */
    public GistsFragment setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(string.no_gists));
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (clickListener != null)
            clickListener.onItemClick(l, v, position, id);
    }

    @Override
    public Loader<List<Gist>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<Gist>>(getActivity()) {
            @Override
            public List<Gist> loadInBackground() {
                try {
                    List<Gist> gists = service.getGists(service.getClient().getUser());
                    Collections.sort(gists, GistsFragment.this);
                    return gists;
                } catch (IOException e) {
                    showError(e, string.error_gists_load);
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class));
    }

    @Override
    public int compare(final Gist g1, final Gist g2) {
        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
    }
}
