package com.github.mobile.android.ui.gist;

import com.github.mobile.android.ResourcePager;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display a list of Gists
 */
public class StarredGistsFragment extends GistsFragment {

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
}
