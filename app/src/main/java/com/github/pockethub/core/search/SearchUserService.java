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
package com.github.pockethub.core.search;

import org.eclipse.egit.github.core.IResourceProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;
import static org.eclipse.egit.github.core.client.IGitHubConstants.PARAM_START_PAGE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LEGACY;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_SEARCH;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_USER;

public class SearchUserService extends UserService {

    private static class UserContainer implements
            IResourceProvider<SearchUser> {

        private List<SearchUser> users;

        /**
         * @see org.eclipse.egit.github.core.IResourceProvider#getResources()
         */
        @Override
        public List<SearchUser> getResources() {
            return users;
        }
    }

    /**
     * Create search user service
     */
    public SearchUserService() {
        super();
    }

    /**
     * Create search user service
     *
     * @param client
     */
    public SearchUserService(GitHubClient client) {
        super(client);
    }

    /**
     * Search for users matching query.
     *
     * @param query
     * @return list of users
     * @throws IOException
     */
    public List<SearchUser> searchUsers(final String query)
            throws IOException {
        return searchUsers(query, -1);
    }

    /**
     * Search for users matching and query.
     *
     * @param query
     * @param startPage
     * @return list of users
     * @throws IOException
     */
    public List<SearchUser> searchUsers(final String query,
            final int startPage) throws IOException {
        if (query == null)
            throw new IllegalArgumentException("Query cannot be null"); //$NON-NLS-1$
        if (query.length() == 0)
            throw new IllegalArgumentException("Query cannot be empty"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_LEGACY + SEGMENT_USER
                + SEGMENT_SEARCH);
        final String encodedQuery = URLEncoder.encode(query, CHARSET_UTF8)
                .replace("+", "%20") //$NON-NLS-1$ //$NON-NLS-2$
                .replace(".", "%2E"); //$NON-NLS-1$ //$NON-NLS-2$
        uri.append('/').append(encodedQuery);

        PagedRequest<SearchUser> request = createPagedRequest();

        Map<String, String> params = new HashMap<>(2, 1);
        if (startPage > 0)
            params.put(PARAM_START_PAGE, Integer.toString(startPage));
        if (!params.isEmpty())
            request.setParams(params);

        request.setUri(uri);
        request.setType(UserContainer.class);
        return getAll(request);
    }

    /**
     * Search for users matching search parameters.
     *
     * @param params
     * @return list of users
     * @throws IOException
     */
    public List<SearchUser> searchUsers(
            final Map<String, String> params) throws IOException {
        return searchUsers(params, -1);
    }

    /**
     * Search for users matching search parameters.
     *
     * @param queryParams
     * @param startPage
     * @return list of users
     * @throws IOException
     */
    public List<SearchUser> searchUsers(
            final Map<String, String> queryParams, final int startPage)
            throws IOException {
        if (queryParams == null)
            throw new IllegalArgumentException("Params cannot be null"); //$NON-NLS-1$
        if (queryParams.isEmpty())
            throw new IllegalArgumentException("Params cannot be empty"); //$NON-NLS-1$

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> param : queryParams.entrySet())
            query.append(param.getKey()).append(':').append(param.getValue())
                .append(' ');
        return searchUsers(query.toString(), startPage);
    }
}
