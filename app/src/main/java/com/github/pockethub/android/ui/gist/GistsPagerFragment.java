/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pockethub.android.ui.gist;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.gists.PublicGistsClient;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.PageIterator;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.TabPagerFragment;
import com.github.pockethub.android.util.ToastUtils;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.GIST_VIEW;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_PERSON;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_STAR;
import static com.github.pockethub.android.util.TypefaceUtils.ICON_TEAM;

public class GistsPagerFragment extends TabPagerFragment<GistQueriesPagerAdapter> {

    private static final String TAG = "GistsPagerFragment";
    @Inject
    private GistStore store;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureTabPager();
    }

    private void randomGist() {
        Observable<Gist> observable = Observable.create(new Observable.OnSubscribe<Gist>() {
            @Override
            public void call(Subscriber<? super Gist> subscriber) {
                PageIterator<Gist> pages = new PageIterator<>(new PageIterator.GitHubRequest<List<Gist>>() {
                    @Override
                    public GithubListClient<List<Gist>> execute(int page) {
                        return new PublicGistsClient(1);
                    }
                }, 1);
                pages.next();
                int randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));

                Collection<Gist> gists = pages.getRequest().execute(randomPage).observable().toBlocking().first().first;

                // Make at least two tries since page numbers are volatile
                if (gists.isEmpty()) {
                    randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));
                    gists = pages.getRequest().execute(randomPage).observable().toBlocking().first().first;
                }

                if (gists.isEmpty())
                    throw new IllegalArgumentException(getContext().getString(
                            R.string.no_gists_found));

                subscriber.onNext(store.addGist(gists.iterator().next()));
            }
        });

        showProgressIndeterminate(R.string.random_gist);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity)getActivity()).<Gist>bindToLifecycle())
                .subscribe(new ObserverAdapter<Gist>() {

                    @Override
                    public void onNext(Gist gist) {
                        getActivity().startActivityForResult(
                                GistsViewActivity.createIntent(gist), GIST_VIEW);
                        dismissProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Exception opening random Gist", e);
                        ToastUtils.show((Activity) getContext(), e.getMessage());
                        dismissProgress();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_gists, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_random:
                randomGist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected GistQueriesPagerAdapter createAdapter() {
        return new GistQueriesPagerAdapter(this);
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
            case 0:
                return ICON_PERSON;
            case 1:
                return ICON_STAR;
            case 2:
                return ICON_TEAM;
            default:
                return super.getIcon(position);
        }
    }
}
