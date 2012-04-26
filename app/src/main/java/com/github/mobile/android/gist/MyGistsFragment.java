package com.github.mobile.android.gist;

import static android.app.Activity.RESULT_OK;
import static com.github.mobile.android.RequestCodes.GIST_CREATE;
import static com.github.mobile.android.RequestCodes.GIST_VIEW;
import android.content.Intent;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.authenticator.GitHubAccount;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display a list of Gists
 */
public class MyGistsFragment extends GistsFragment {

    @Inject
    private Provider<GitHubAccount> accountProvider;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == GIST_CREATE || requestCode == GIST_VIEW) && RESULT_OK == resultCode) {
            refresh();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected ResourcePager<Gist> createPager() {
        return new ResourcePager<Gist>() {

            @Override
            protected Object getId(Gist resource) {
                return resource.getId();
            }

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return service.pageGists(accountProvider.get().username, page, size);
            }
        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_gists;
    }

    @Override
    protected ViewHoldingListAdapter<Gist> adapterFor(List<Gist> items) {
        return new ViewHoldingListAdapter<Gist>(items, ViewInflator.viewInflatorFor(getActivity(),
                layout.gist_list_item), ReflectiveHolderFactory.reflectiveFactoryFor(GistViewHolder.class, idWidth));
    }
}
