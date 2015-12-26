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
package com.github.pockethub.ui.issue;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.pockethub.R;
import com.github.pockethub.ui.FragmentStatePagerAdapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.github.pockethub.ui.issue.DashboardIssueFragment.ARG_FILTER;
import static org.eclipse.egit.github.core.service.IssueService.DIRECTION_DESCENDING;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_DIRECTION;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_FILTER;
import static org.eclipse.egit.github.core.service.IssueService.FIELD_SORT;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_ASSIGNED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_CREATED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_MENTIONED;
import static org.eclipse.egit.github.core.service.IssueService.FILTER_SUBSCRIBED;
import static org.eclipse.egit.github.core.service.IssueService.SORT_UPDATED;

/**
 * Pager adapter for the issues dashboard
 */
public class IssueDashboardPagerAdapter extends FragmentStatePagerAdapter {

    private final Resources resources;

    /**
     * Create pager adapter
     *
     * @param fragment
     */
    public IssueDashboardPagerAdapter(final Fragment fragment) {
        super(fragment);

        resources = fragment.getResources();
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(final int position) {
        String filter = null;
        switch (position) {
            case 0:
                filter = FILTER_SUBSCRIBED;
                break;
            case 1:
                filter = FILTER_ASSIGNED;
                break;
            case 2:
                filter = FILTER_CREATED;
                break;
            case 3:
                filter = FILTER_MENTIONED;
                break;
            default:
                return null;
        }
        final Map<String, String> filterData = new HashMap<>();
        filterData.put(FIELD_FILTER, filter);
        filterData.put(FIELD_SORT, SORT_UPDATED);
        filterData.put(FIELD_DIRECTION, DIRECTION_DESCENDING);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_FILTER, (Serializable) filterData);
        DashboardIssueFragment fragment = new DashboardIssueFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.tab_watched);
            case 1:
                return resources.getString(R.string.tab_assigned);
            case 2:
                return resources.getString(R.string.tab_created);
            case 3:
                return resources.getString(R.string.tab_mentioned);
            default:
                return null;
        }
    }
}
