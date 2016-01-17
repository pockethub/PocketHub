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
package com.github.pockethub.ui.gist;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.services.client.GithubListClient;
import com.alorma.github.sdk.services.gists.UserStarredGistsClient;
import com.github.pockethub.core.PageIterator;
import com.github.pockethub.core.ResourcePager;
import com.github.pockethub.core.gist.GistPager;

import java.util.List;

/**
 * Fragment to display a list of Gists
 */
public class StarredGistsFragment extends GistsFragment {

    @Override
    protected ResourcePager<Gist> createPager() {
        return new GistPager(store) {

            @Override
            public PageIterator<Gist> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<List<Gist>>() {
                    @Override
                    public GithubListClient<List<Gist>> execute(int page) {
                        return new UserStarredGistsClient(page);
                    }
                }, page);
            }
        };
    }
}
