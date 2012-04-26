package com.github.mobile.android.gist;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display a list of Gists
 */
public class StarredGistsFragment extends GistsFragment {

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.gist_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(GistViewHolder.class, idWidth,
                avatarHelper));
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
                return service.pageStarredGists(page, size);
            }
        };
    }

    protected int getLoadingMessage() {
        return string.loading_gists;
    }
}
