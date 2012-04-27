package com.github.mobile.android.ui.gist;

import static android.app.Activity.RESULT_OK;
import static com.github.mobile.android.RequestCodes.GIST_CREATE;
import static com.github.mobile.android.RequestCodes.GIST_VIEW;
import android.content.Intent;

import com.github.mobile.android.ResourcePager;
import com.github.mobile.android.authenticator.GitHubAccount;
import com.github.mobile.android.ui.ItemListAdapter;
import com.github.mobile.android.ui.ItemView;
import com.google.inject.Inject;
import com.google.inject.Provider;

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

    @Override
    protected ResourcePager<Gist> createPager() {
        return new GistPager() {

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return service.pageGists(accountProvider.get().username, page, size);
            }
        };
    }

    @Override
    protected ItemListAdapter<Gist, ? extends ItemView> createAdapter(List<Gist> items) {
        return new GistListAdapter(null, getActivity().getLayoutInflater(), items.toArray(new Gist[items.size()]));
    }
}
