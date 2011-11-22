package com.github.mobile.android.gist;

import static com.github.mobile.android.R.layout.gist_list_item;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListAdapter;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.ui.fragments.ListLoadingFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display Gists
 */
public class GistFragment extends ListLoadingFragment<Gist> {

    @Inject
    private GistService service;

    public Loader<List<Gist>> onCreateLoader(int id, Bundle bundle) {
        return new AsyncLoader<List<Gist>>(getActivity()) {
            @Override
            public List<Gist> loadInBackground() {
                try {
                    return service.getGists(service.getClient().getUser());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected ListAdapter adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), gist_list_item),
                new ViewHolderFactory<Gist>() {
                    public ViewHolder<Gist> createViewHolderFor(View view) {
                        return new GistViewHolder(view);
                    }
                });
    }
}
