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
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.TabPagerFragment;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.google.inject.Inject;

import java.util.Collection;

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureTabPager();
    }

    private void randomGist() {
        Single<Gist> single = Single.create(emitter -> {
            GistService service = ServiceGenerator.createService(getActivity(), GistService.class);

            Page<Gist> p = service.getPublicGists(1).blockingGet().body();
            int randomPage = 1 + (int) (Math.random() * ((p.last() - 1) + 1));

            Collection<Gist> gists = service.getPublicGists(randomPage)
                    .blockingGet()
                    .body()
                    .items();

            // Make at least two tries since page numbers are volatile
            if (gists.isEmpty()) {
                randomPage = 1 + (int) (Math.random() * ((p.last() - 1) + 1));
                gists = service.getPublicGists(randomPage).blockingGet().body().items();
            }

            if (gists.isEmpty()) {
                throw new IllegalArgumentException(getContext().getString(
                        R.string.no_gists_found));
            }

            emitter.onSuccess(store.addGist(gists.iterator().next()));
        });

        showProgressIndeterminate(R.string.random_gist);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity)getActivity()).bindToLifecycle())
                .subscribe(gist -> {
                    getActivity().startActivityForResult(
                            GistsViewActivity.createIntent(gist), GIST_VIEW);
                    dismissProgress();
                }, e -> {
                    Log.d(TAG, "Exception opening random Gist", e);
                    ToastUtils.show((Activity) getContext(), e.getMessage());
                    dismissProgress();
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
