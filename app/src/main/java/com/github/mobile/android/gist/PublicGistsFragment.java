package com.github.mobile.android.gist;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.github.mobile.android.RequestCodes.GIST_VIEW;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.ui.PagedListFragment;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ListViewHelper;
import com.google.inject.Inject;
import com.madgag.android.listviews.ViewHoldingListAdapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Fragment to display a list of public Gists
 */
public class PublicGistsFragment extends PagedListFragment<Gist> {

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private GistService service;

    /**
     * Gist id field
     */
    protected TextView gistId;

    @Inject
    private GistStore store;

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
        if (requestCode == GIST_VIEW && (RESULT_OK == resultCode || RESULT_CANCELED == resultCode)) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof BaseAdapter)
                ((BaseAdapter) adapter).notifyDataSetChanged();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadFinished(Loader<List<Gist>> loader, List<Gist> items) {
        idWidth.set(GistViewHolder.computeIdWidth(items, gistId));

        super.onLoadFinished(loader, items);
    }

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, viewInflatorFor(getActivity(), layout.gist_list_item),
                reflectiveFactoryFor(GistViewHolder.class, idWidth, avatarHelper));
    }

    @Override
    protected ResourcePager<Gist> createPager() {
        return new ResourcePager<Gist>() {

            @Override
            protected Object getId(Gist resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return service.pagePublicGists(page, size);
            }

            @Override
            protected Gist register(Gist resource) {
                store.addGist(resource);
                return resource;
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_gists;
    }
}
