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
package com.github.mobile.ui.gist;

import android.os.Bundle;

import com.github.mobile.core.ResourcePager;
import com.github.mobile.core.gist.GistPager;

import java.util.List;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;

/**
 * Fragment to display a list of Gists
 */
public class StarredGistsFragment extends GistsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Stop gap to auto-refresh as necessary.
        // TODO: implement logic so that only starred gists are shown
        if (gistStarUpdater.getStarredGists().size() != 0 ||
            gistStarUpdater.getUnstarredGists().size() != 0) {
            mergeLocalChanges(gistStarUpdater.getStarredGists(),
                gistStarUpdater.getUnstarredGists());
            getListAdapter().getWrappedAdapter().setItems(items);
            gistStarUpdater.clearStarsCache();
            forceRefresh();
        }
    }

    @Override
    protected ResourcePager<Gist> createPager() {
        return new GistPager(store) {

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return service.pageStarredGists(page, size);
            }
        };
    }

    private void mergeLocalChanges(List<Gist> starredGists, List<Gist>
        unstarredGists) {
        int starredPos = 0;
        int unstarredPos = 0;
        int itemsPos = 0;

        while (itemsPos < items.size() && !(starredPos < starredGists.size() &&
            unstarredPos < unstarredGists.size())) {
            Gist curStar = null, curUnstar = null, curItem;
            if(starredPos < starredGists.size())
                curStar = starredGists.get(starredPos);
            if(unstarredPos < unstarredGists.size())
                curUnstar = unstarredGists.get(unstarredPos);
            curItem = items.get(itemsPos);

            // If this is a duplicate, just carry on
            if (curStar != null && curStar.getId().equals(curItem.getId())) {
                starredPos++;
                continue;
            }

            // If this is earlier than the current spot in the list, add it
            if (curStar != null && curStar.getCreatedAt().getTime() >=
                curItem.getCreatedAt().getTime()) {
                items.add(itemsPos, starredGists.get(starredPos));
                itemsPos++;
                starredPos++;
                continue;
            }

            // If this is on the list but has been unstarred, remove it
            if (curUnstar != null && curUnstar.getId().equals(curItem.getId())) {
                items.remove(itemsPos);
                unstarredPos++;
                continue;
            }

            itemsPos++;

            // If the unstarred list is lagging behind the items list then we
            // want to advance the unstarred list up to the items list
            if (curUnstar != null && curItem.getCreatedAt().getTime() <
                curUnstar.getCreatedAt().getTime()) {
                while (unstarredPos < unstarredGists.size()) {
                    curUnstar = unstarredGists.get(unstarredPos);
                    // We want to advance the unstarred location up to the
                    // first one ahead of the items list
                    if (curItem.getCreatedAt().getTime() <
                        curUnstar.getCreatedAt().getTime())
                        break;
                    unstarredPos++;
                }
            }
        }
    }
}
