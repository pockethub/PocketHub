package com.github.mobile.android.ui.gist;

import com.github.mobile.android.ResourcePager;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display a list of public Gists
 */
public class PublicGistsFragment extends GistsFragment {

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
}
