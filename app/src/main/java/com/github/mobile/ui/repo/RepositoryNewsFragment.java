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
package com.github.mobile.ui.repo;

import static com.github.mobile.Intents.EXTRA_REPOSITORY;

import com.github.mobile.core.ResourcePager;
import com.github.mobile.ui.NewsFragment;
import com.github.mobile.ui.user.EventPager;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;

import roboguice.inject.InjectExtra;

/**
 * Fragment to display a news feed for a specific repository
 */
public class RepositoryNewsFragment extends NewsFragment {

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repo;

    @Override
    protected ResourcePager<Event> createPager() {
        return new EventPager() {

            public PageIterator<Event> createIterator(int page, int size) {
                return service.pageEvents(repo, page, size);
            }
        };
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    @Override
    protected void viewRepository(Repository repository) {
        if (repo.getId() != repository.getId())
            super.viewRepository(repository);
    }
}
