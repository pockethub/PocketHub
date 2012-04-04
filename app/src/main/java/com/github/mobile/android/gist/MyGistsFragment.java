package com.github.mobile.android.gist;

import static com.github.mobile.android.ResultCodes.GIST_DELETE;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.ThrowableLoader;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.io.IOException;
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
                return storeAndSort(service.getGists(service.getClient().getUser()));
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE && GIST_DELETE == resultCode) {
            refresh();
            return;
        }
        if (requestCode == REQUEST_VIEW && GIST_DELETE == resultCode) {
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
