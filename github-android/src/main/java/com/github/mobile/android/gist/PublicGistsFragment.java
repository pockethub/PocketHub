package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_FIRST;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.ThrowableLoader;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.NoSuchPageException;

/**
 * Fragment to display a list of public Gists
 */
public class PublicGistsFragment extends GistsFragment {

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    public Loader<List<Gist>> onCreateLoader(int i, Bundle bundle) {
        return new ThrowableLoader<List<Gist>>(getActivity(), listItems) {
            @Override
            public List<Gist> loadData() throws IOException {
                try {
                    Collection<Gist> publicGists = service.pagePublicGists(PAGE_FIRST, -1).next();
                    List<Gist> gists = new ArrayList<Gist>(publicGists.size());
                    for (Gist gist : publicGists)
                        gists.add(store.addGist(gist));
                    Collections.sort(gists, PublicGistsFragment.this);
                    return gists;
                } catch (NoSuchPageException e) {
                    throw e.getCause();
                }
            }
        };
    }

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class, idWidth, avatarHelper));
    }
}
