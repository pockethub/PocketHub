package com.github.mobile.android.gist;

import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.ThrowableLoader;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Gist;

/**
 * Fragment to display a list of Gists
 */
public class MyGistsFragment extends GistsFragment {

    @Override
    public Loader<List<Gist>> onCreateLoader(int i, Bundle bundle) {
        return new ThrowableLoader<List<Gist>>(getActivity(), listItems) {
            @Override
            public List<Gist> loadData() throws IOException {
                List<Gist> userGists = service.getGists(service.getClient().getUser());
                List<Gist> gists = new ArrayList<Gist>(userGists.size());
                for (Gist gist : userGists)
                    gists.add(store.addGist(gist));
                Collections.sort(gists, MyGistsFragment.this);
                return gists;
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE && ShareGistActivity.RESULT_CREATED == resultCode) {
            refresh();
            return;
        }
        if (requestCode == REQUEST_VIEW && ViewGistActivity.RESULT_DELETED == resultCode) {
            refresh();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class, idWidth));
    }
}
