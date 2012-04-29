/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.user;

import android.app.Activity;
import android.os.Bundle;

import com.github.mobile.HomeActivity;
import com.github.mobile.HomeActivity.OrgSelectionListener;
import com.github.mobile.R.string;
import com.github.mobile.core.ResourcePager;
import com.github.mobile.ui.NewsFragment;
import com.github.mobile.util.ListViewUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;

/**
 * Fragment to display a news feed for a given user/org
 */
public class UserNewsFragment extends NewsFragment implements OrgSelectionListener {

    private User org;

    @Inject
    private EventService service;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListViewUtils.configure(getActivity(), getListView(), true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        org = ((HomeActivity) activity).registerOrgSelectionListener(this);
    }

    @Override
    public void onOrgSelected(User org) {
        int previousOrgId = this.org != null ? this.org.getId() : -1;
        this.org = org;
        // Only hard refresh if view already created and org is changing
        if (getView() != null && previousOrgId != org.getId())
            hideOldContentAndRefresh();
    }

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageUserReceivedEvents(org.getLogin(), false, page, size);
            }

            protected Event register(Event resource) {
                return NewsEventViewHolder.isValid(resource) ? resource : null;
            }

        };
    }

    @Override
    protected int getLoadingMessage() {
        return string.loading_news;
    }
}
