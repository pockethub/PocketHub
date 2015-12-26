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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import com.github.pockethub.ui.MainActivity;
import com.github.pockethub.ui.TabPagerFragment;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.pockethub.util.TypefaceUtils.ICON_ADD;
import static com.github.pockethub.util.TypefaceUtils.ICON_BROADCAST;
import static com.github.pockethub.util.TypefaceUtils.ICON_FOLLOW;
import static com.github.pockethub.util.TypefaceUtils.ICON_WATCH;

/**
 * Dashboard activity for issues
 */
public class IssueDashboardPagerFragment extends
    TabPagerFragment<IssueDashboardPagerAdapter> {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureTabPager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected IssueDashboardPagerAdapter createAdapter() {
        return new IssueDashboardPagerAdapter(this);
    }

    @Override
    protected String getIcon(int position) {
        switch (position) {
            case 0:
                return ICON_WATCH;
            case 1:
                return ICON_FOLLOW;
            case 2:
                return ICON_ADD;
            case 3:
                return ICON_BROADCAST;
            default:
                return super.getIcon(position);
        }
    }
}
