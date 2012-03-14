package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.AsyncLoader;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Gist;

/**
 * Fragment to display a list of Gists
 */
public class StarredGistsFragment extends GistsFragment {

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public Loader<List<Gist>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncLoader<List<Gist>>(getActivity()) {
            @Override
            public List<Gist> loadInBackground() {
                try {
                    List<Gist> starred = service.getStarredGists();
                    List<Gist> gists = new ArrayList<Gist>(starred.size());
                    for (Gist gist : starred)
                        gists.add(store.addGist(gist));
                    Collections.sort(gists, StarredGistsFragment.this);
                    return gists;
                } catch (IOException e) {
                    showError(e, string.error_gists_load);
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class, GistViewHolder.computeMaxDigits(items), avatarHelper));
    }
}
