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
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a list of Gists
 */
public class GistsFragment extends ListLoadingFragment<Gist> {

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
        setEmptyText("No Gists");
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
                List<Gist> gists;
                try {
                    gists = service.getGists(service.getClient().getUser());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Collections.sort(gists, new Comparator<Gist>() {

                    public int compare(Gist g1, Gist g2) {
                        return g2.getCreatedAt().compareTo(g1.getCreatedAt());
                    }
                });
                return gists;
            }
        };
    }

    @Override
    protected ListAdapter adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class));
    }
}
