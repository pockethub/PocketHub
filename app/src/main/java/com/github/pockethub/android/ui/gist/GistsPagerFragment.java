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

import com.github.pockethub.android.R;
import com.github.pockethub.android.core.gist.GistStore;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.TabPagerFragment;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.google.inject.Inject;

import java.util.Random;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.github.pockethub.android.RequestCodes.GIST_VIEW;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_PERSON;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_STAR;
import static com.github.pockethub.android.ui.view.OcticonTextView.ICON_TEAM;

public class GistsPagerFragment extends TabPagerFragment<GistQueriesPagerAdapter> {

    private static final String TAG = "GistsPagerFragment";
    @Inject
    private GistStore store;
    private Random rand;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rand = new Random();
        configureTabPager();
    }

    private void randomGist() {

        GistService service = ServiceGenerator.createService(getActivity(), GistService.class);

        service.getPublicGists(1)
                .flatMap(response -> {
                    Page<Gist> firstPage = response.body();
                    int randomPage = (int) (Math.random() * (firstPage.last() - 1));
                    randomPage = Math.max(1, randomPage);

                    return service.getPublicGists(randomPage);
                })
                .flatMap(response -> {
                    Page<Gist> gistPage = response.body();
                    if (gistPage.items().isEmpty()) {
                        int randomPage = (int) (Math.random() * (gistPage.last() - 1));
                        randomPage = Math.max(1, randomPage);

                        return service.getPublicGists(randomPage);
                    }

                    return Single.just(response);
                })
                .map(response -> {
                    Page<Gist> gistPage = response.body();
                    if (response.isSuccessful()) {
                        int size = gistPage.items().size();
                        if (size > 0) {
                            return store.addGist(gistPage.items().get(rand.nextInt(size)));
                        } else {
                            throw new IllegalArgumentException(getContext().getString(
                                    R.string.no_gists_found));
                        }
                    } else {
                        ToastUtils.show(getActivity(), R.string.error_gist_load);
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity)getActivity()).bindToLifecycle())
                .compose(RxProgress.bindToLifecycle(getActivity(), R.string.random_gist))
                .subscribe(gist -> getActivity().startActivityForResult(
                        GistsViewActivity.createIntent(gist), GIST_VIEW), e -> {
                    Log.d(TAG, "Exception opening random Gist", e);
                    ToastUtils.show((Activity) getContext(), e.getMessage());
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
