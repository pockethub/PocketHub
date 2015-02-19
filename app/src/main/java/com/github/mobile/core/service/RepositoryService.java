/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package com.github.mobile.core.service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.CHARSET_UTF8;
import static org.eclipse.egit.github.core.client.IGitHubConstants.PARAM_LANGUAGE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.PARAM_START_PAGE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_BRANCHES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_CONTRIBUTORS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_FORKS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_HOOKS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LANGUAGES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_LEGACY;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ORGS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOSITORIES;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_SEARCH;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_TAGS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_TEST;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_USER;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_USERS;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_FIRST;
import static org.eclipse.egit.github.core.client.PagedRequest.PAGE_SIZE;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.IResourceProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.GitHubService;

/**
 * Repository service class.
 *
 * @see <a href="http://developer.github.com/v3/repos">GitHub repository API
 *      documentation</a>
 * @see <a href="http://developer.github.com/v3/repos/forks">GitHub forks API
 *      documentation</a>
 */
public class RepositoryService extends GitHubService {

    /**
     * FIELD_NAME
     */
    public static final String FIELD_NAME = "name"; //$NON-NLS-1$

    /**
     * FIELD_DESCRIPTION
     */
    public static final String FIELD_DESCRIPTION = "description"; //$NON-NLS-1$

    /**
     * FIELD_HOMEPAGE
     */
    public static final String FIELD_HOMEPAGE = "homepage"; //$NON-NLS-1$

    /**
     * FIELD_PUBLIC
     */
    public static final String FIELD_PUBLIC = "public"; //$NON-NLS-1$

    /**
     * Type filter key
     */
    public static final String FILTER_TYPE = "type"; //$NON-NLS-1$

    /**
     * Public repository filter type
     */
    public static final String TYPE_PUBLIC = "public"; //$NON-NLS-1$

    /**
     * Private repository filter type
     */
    public static final String TYPE_PRIVATE = "private"; //$NON-NLS-1$

    /**
     * Member repository filter type
     */
    public static final String TYPE_MEMBER = "member"; //$NON-NLS-1$

    /**
     * All repositories filter type
     */
    public static final String TYPE_ALL = "all"; //$NON-NLS-1$

    private static class RepositoryContainer implements
            IResourceProvider<SearchRepository> {

        private List<SearchRepository> repositories;

        /**
         * @see org.eclipse.egit.github.core.IResourceProvider#getResources()
         */
        public List<SearchRepository> getResources() {
            return repositories;
        }
    }

    /**
     * Create repository service
     */
    public RepositoryService() {
        super();
    }

    /**
     * Create repository service
     *
     * @param client
     *            cannot be null
     */
    public RepositoryService(GitHubClient client) {
        super(client);
    }

    /**
     * Get repositories for the currently authenticated user
     *
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepositories() throws IOException {
        return getRepositories((Map<String, String>) null);
    }

    /**
     * Get repositories for the currently authenticated user
     *
     * @param filterData
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepositories(Map<String, String> filterData)
            throws IOException {
        return getAll(pageRepositories(filterData));
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories() {
        return pageRepositories(PAGE_SIZE);
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(int size) {
        return pageRepositories(PAGE_FIRST, size);
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @param start
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(int start, int size) {
        return pageRepositories((Map<String, String>) null, start, size);
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @param filterData
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(
            Map<String, String> filterData) {
        return pageRepositories(filterData, PAGE_SIZE);
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @param filterData
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(
            Map<String, String> filterData, int size) {
        return pageRepositories(filterData, PAGE_FIRST, size);
    }

    /**
     * Page repositories for currently authenticated user
     *
     * @param filterData
     * @param start
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(
            Map<String, String> filterData, int start, int size) {
        PagedRequest<Repository> request = createPagedRequest(start, size);
        request.setUri(SEGMENT_USER + SEGMENT_REPOS);
        request.setParams(filterData);
        request.setType(new TypeToken<List<Repository>>() {
        }.getType());
        return createPageIterator(request);
    }

    /**
     * Page all repositories
     *
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageAllRepositories() {
        return pageAllRepositories(-1);
    }

    /**
     * Page all repositories
     *
     * @param since
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageAllRepositories(final long since) {
        PagedRequest<Repository> request = createPagedRequest();
        request.setUri(SEGMENT_REPOSITORIES);
        if (since > 0)
            request.setParams(Collections.singletonMap("since",
                    Long.toString(since)));
        request.setType(new TypeToken<List<Repository>>() {
        }.getType());
        return createPageIterator(request);
    }

    /**
     * Get repositories for the given user
     *
     * @param user
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepositories(String user) throws IOException {
        return getAll(pageRepositories(user));
    }

    /**
     * Page repositories for given user
     *
     * @param user
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(String user) {
        return pageRepositories(user, PAGE_SIZE);
    }

    /**
     * Page repositories for given user
     *
     * @param user
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageRepositories(String user, int size) {
        return pageRepositories(user, PAGE_FIRST, size);
    }

    /**
     * Page repositories for given user
     *
     * @param user
     * @param start
     * @param size
     * @return iterator over repository page
     */
    public PageIterator<Repository> pageRepositories(String user, int start,
            int size) {
        if (user == null)
            throw new IllegalArgumentException("User cannot be null"); //$NON-NLS-1$
        if (user.length() == 0)
            throw new IllegalArgumentException("User cannot be empty"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_USERS);
        uri.append('/').append(user);
        uri.append(SEGMENT_REPOS);
        PagedRequest<Repository> request = createPagedRequest(start, size);
        request.setUri(uri);
        request.setType(new TypeToken<List<Repository>>() {
        }.getType());
        return createPageIterator(request);
    }

    /**
     * Get organization repositories for the given organization
     *
     * @param organization
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getOrgRepositories(String organization)
            throws IOException {
        return getOrgRepositories(organization, null);
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization) {
        return pageOrgRepositories(organization, PAGE_SIZE);
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization,
            int size) {
        return pageOrgRepositories(organization, PAGE_FIRST, size);
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @param start
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization,
            int start, int size) {
        return pageOrgRepositories(organization, null, start, size);
    }

    /**
     * Get organization repositories for the given organization
     *
     * @param organization
     * @param filterData
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getOrgRepositories(String organization,
            Map<String, String> filterData) throws IOException {
        return getAll(pageOrgRepositories(organization, filterData));
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @param filterData
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization,
            Map<String, String> filterData) {
        return pageOrgRepositories(organization, filterData, PAGE_SIZE);
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @param filterData
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization,
            Map<String, String> filterData, int size) {
        return pageOrgRepositories(organization, filterData, PAGE_FIRST, size);
    }

    /**
     * Page repositories for the given organization
     *
     * @param organization
     * @param filterData
     * @param start
     * @param size
     * @return iterator over pages of repositories
     */
    public PageIterator<Repository> pageOrgRepositories(String organization,
            Map<String, String> filterData, int start, int size) {
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null"); //$NON-NLS-1$
        if (organization.length() == 0)
            throw new IllegalArgumentException("Organization cannot be empty"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_ORGS);
        uri.append('/').append(organization);
        uri.append(SEGMENT_REPOS);
        PagedRequest<Repository> request = createPagedRequest(start, size);
        request.setParams(filterData);
        request.setUri(uri);
        request.setType(new TypeToken<List<Repository>>() {
        }.getType());
        return createPageIterator(request);
    }

    /**
     * Search for repositories matching query.
     *
     * @param query
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(final String query)
            throws IOException {
        return searchRepositories(query, -1);
    }

    /**
     * Search for repositories matching query.
     *
     * @param query
     * @param startPage
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(final String query,
            final int startPage) throws IOException {
        return searchRepositories(query, null, startPage);
    }

    /**
     * Search for repositories matching language and query.
     *
     * @param query
     * @param language
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(final String query,
            final String language) throws IOException {
        return searchRepositories(query, language, -1);
    }

    /**
     * Search for repositories matching language and query.
     *
     * @param query
     * @param language
     * @param startPage
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(final String query,
            final String language, final int startPage) throws IOException {
        if (query == null)
            throw new IllegalArgumentException("Query cannot be null"); //$NON-NLS-1$
        if (query.length() == 0)
            throw new IllegalArgumentException("Query cannot be empty"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_LEGACY + SEGMENT_REPOS
                + SEGMENT_SEARCH);
        final String encodedQuery = URLEncoder.encode(query, CHARSET_UTF8)
                .replace("+", "%20") //$NON-NLS-1$ //$NON-NLS-2$
                .replace(".", "%2E"); //$NON-NLS-1$ //$NON-NLS-2$
        uri.append('/').append(encodedQuery);

        PagedRequest<SearchRepository> request = createPagedRequest();

        Map<String, String> params = new HashMap<String, String>(2, 1);
        if (language != null && language.length() > 0)
            params.put(PARAM_LANGUAGE, language);
        if (startPage > 0)
            params.put(PARAM_START_PAGE, Integer.toString(startPage));
        if (!params.isEmpty())
            request.setParams(params);

        request.setUri(uri);
        request.setType(RepositoryContainer.class);
        return getAll(request);
    }

    /**
     * Search for repositories matching search parameters.
     *
     * @param params
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(
            final Map<String, String> params) throws IOException {
        return searchRepositories(params, -1);
    }

    /**
     * Search for repositories matching search parameters.
     *
     * @param queryParams
     * @param startPage
     * @return list of repositories
     * @throws IOException
     */
    public List<SearchRepository> searchRepositories(
            final Map<String, String> queryParams, final int startPage)
            throws IOException {
        if (queryParams == null)
            throw new IllegalArgumentException("Params cannot be null"); //$NON-NLS-1$
        if (queryParams.isEmpty())
            throw new IllegalArgumentException("Params cannot be empty"); //$NON-NLS-1$

        StringBuilder query = new StringBuilder();
        for (Entry<String, String> param : queryParams.entrySet())
            query.append(param.getKey()).append(':').append(param.getValue())
                    .append(' ');
        return searchRepositories(query.toString(), startPage);
    }

    /**
     * Create a new repository
     *
     * @param repository
     * @return created repository
     * @throws IOException
     */
    public Repository createRepository(Repository repository)
            throws IOException {
        if (repository == null)
            throw new IllegalArgumentException("Repository cannot be null"); //$NON-NLS-1$

        return client.post(SEGMENT_USER + SEGMENT_REPOS, repository,
                Repository.class);
    }

    /**
     * Create a new repository
     *
     * @param organization
     * @param repository
     * @return created repository
     * @throws IOException
     */
    public Repository createRepository(String organization,
            Repository repository) throws IOException {
        if (organization == null)
            throw new IllegalArgumentException("Organization cannot be null"); //$NON-NLS-1$
        if (organization.length() == 0)
            throw new IllegalArgumentException("Organization cannot be empty"); //$NON-NLS-1$
        if (repository == null)
            throw new IllegalArgumentException("Repository cannot be null"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_ORGS);
        uri.append('/').append(organization);
        uri.append(SEGMENT_REPOS);
        return client.post(uri.toString(), repository, Repository.class);
    }

    /**
     * Get repository
     *
     * @param owner
     * @param name
     * @return repository
     * @throws IOException
     */
    public Repository getRepository(final String owner, final String name)
            throws IOException {
        return getRepository(RepositoryId.create(owner, name));
    }

    /**
     * Get repository
     *
     * @param provider
     * @return repository
     * @throws IOException
     */
    public Repository getRepository(final IRepositoryIdProvider provider)
            throws IOException {
        final String id = getId(provider);
        GitHubRequest request = createRequest();
        request.setUri(SEGMENT_REPOS + '/' + id);
        request.setType(Repository.class);
        return (Repository) client.get(request).getBody();
    }

    /**
     * Create paged request for iterating over repositories forks
     *
     * @param repository
     * @param start
     * @param size
     * @return paged request
     */
    protected PagedRequest<Repository> createPagedForkRequest(
            IRepositoryIdProvider repository, int start, int size) {
        final String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_FORKS);
        PagedRequest<Repository> request = createPagedRequest(start, size);
        request.setUri(uri);
        request.setType(new TypeToken<List<Repository>>() {
        }.getType());
        return request;
    }

    /**
     * Get all the forks of the given repository
     *
     * @param repository
     * @return non-null but possibly empty list of repository
     * @throws IOException
     */
    public List<Repository> getForks(IRepositoryIdProvider repository)
            throws IOException {
        return getAll(pageForks(repository));
    }

    /**
     * Page forks of given repository
     *
     * @param repository
     * @return iterator over repositories
     */
    public PageIterator<Repository> pageForks(IRepositoryIdProvider repository) {
        return pageForks(repository, PAGE_SIZE);
    }

    /**
     * Page forks of given repository
     *
     * @param repository
     * @param size
     * @return iterator over repositories
     */
    public PageIterator<Repository> pageForks(IRepositoryIdProvider repository,
            int size) {
        return pageForks(repository, PAGE_FIRST, size);
    }

    /**
     * Page forks of given repository
     *
     * @param repository
     * @param start
     * @param size
     * @return iterator over repositories
     */
    public PageIterator<Repository> pageForks(IRepositoryIdProvider repository,
            int start, int size) {
        PagedRequest<Repository> request = createPagedForkRequest(repository,
                start, size);
        return createPageIterator(request);
    }

    /**
     * Edit given repository
     *
     * @param repository
     * @return edited repository
     * @throws IOException
     */
    public Repository editRepository(Repository repository) throws IOException {
        if (repository == null)
            throw new IllegalArgumentException("Repository cannot be null"); //$NON-NLS-1$

        final String repoId = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(repoId);
        return client.post(uri.toString(), repository, Repository.class);
    }

    /**
     * Edit given fields in repository
     * <p>
     * Only values in the given fields map will be updated on the repository
     *
     * @param owner
     * @param name
     * @param fields
     * @return edited repository
     * @throws IOException
     */
    public Repository editRepository(String owner, String name,
            Map<String, Object> fields) throws IOException {
        verifyRepository(owner, name);
        if (fields == null)
            throw new IllegalArgumentException("Fields cannot be null"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(owner).append('/').append(name);
        return client.post(uri.toString(), fields, Repository.class);
    }

    /**
     * Edit given fields in repository
     * <p>
     * Only values in the given fields map will be updated on the repository
     *
     * @param provider
     * @param fields
     * @return edited repository
     * @throws IOException
     */
    public Repository editRepository(IRepositoryIdProvider provider,
            Map<String, Object> fields) throws IOException {
        String id = getId(provider);
        if (fields == null)
            throw new IllegalArgumentException("Fields cannot be null"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        return client.post(uri.toString(), fields, Repository.class);
    }

    /**
     * Fork given repository into new repository under the currently
     * authenticated user.
     *
     * @param repository
     * @return forked repository
     * @throws IOException
     */
    public Repository forkRepository(IRepositoryIdProvider repository)
            throws IOException {
        return forkRepository(repository, null);
    }

    /**
     * Fork given repository into new repository.
     *
     * The new repository will be under the given organization if non-null, else
     * it will be under the currently authenticated user.
     *
     * @param repository
     * @param organization
     * @return forked repository
     * @throws IOException
     */
    public Repository forkRepository(IRepositoryIdProvider repository,
            String organization) throws IOException {
        final String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_FORKS);
        if (organization != null)
            uri.append("?org=").append(organization);
        return client.post(uri.toString(), null, Repository.class);
    }

    /**
     * Delete given repository from currently
     * authenticated user's list of repositories.
     *
     * @param repository
     * @throws IOException
     */
    public void deleteRepository(IRepositoryIdProvider repository)
            throws IOException {
        deleteRepository(repository, null);
    }

    /**
     * Delete given repository.
     *
     * Everything under given repository will be deleted
     *
     * @param repository
     * @param organization
     * @throws IOException
     */
    public void deleteRepository(IRepositoryIdProvider repository,
            String organization) throws IOException {
        final String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        client.delete(uri.toString());
    }

    /**
     * Get languages used in given repository
     *
     * @param repository
     * @return map of language names mapped to line counts
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked" })
    public Map<String, Long> getLanguages(IRepositoryIdProvider repository)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_LANGUAGES);
        GitHubRequest request = createRequest();
        request.setUri(uri);
        request.setType(new TypeToken<Map<String, Long>>() {
        }.getType());
        return (Map<String, Long>) client.get(request).getBody();
    }

    /**
     * Get branches in given repository
     *
     * @param repository
     * @return list of branches
     * @throws IOException
     */
    public List<RepositoryBranch> getBranches(IRepositoryIdProvider repository)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_BRANCHES);
        PagedRequest<RepositoryBranch> request = createPagedRequest();
        request.setUri(uri);
        request.setType(new TypeToken<List<RepositoryBranch>>() {
        }.getType());
        return getAll(request);
    }

    /**
     * Get tags in given repository
     *
     * @param repository
     * @return list of tags
     * @throws IOException
     */
    public List<RepositoryTag> getTags(IRepositoryIdProvider repository)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_TAGS);
        PagedRequest<RepositoryTag> request = createPagedRequest();
        request.setUri(uri);
        request.setType(new TypeToken<List<RepositoryTag>>() {
        }.getType());
        return getAll(request);
    }

    /**
     * Get contributors to repository
     *
     * @param repository
     * @param includeAnonymous
     * @return list of contributors
     * @throws IOException
     */
    public List<Contributor> getContributors(IRepositoryIdProvider repository,
            boolean includeAnonymous) throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_CONTRIBUTORS);
        PagedRequest<Contributor> request = createPagedRequest();
        request.setUri(uri);
        if (includeAnonymous)
            request.setParams(Collections.singletonMap("anon", "1")); //$NON-NLS-1$ //$NON-NLS-2$
        request.setType(new TypeToken<List<Contributor>>() {
        }.getType());
        return getAll(request);
    }

    /**
     * Get hooks for given repository
     *
     * @param repository
     * @return list of hooks
     * @throws IOException
     */
    public List<RepositoryHook> getHooks(IRepositoryIdProvider repository)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        PagedRequest<RepositoryHook> request = createPagedRequest();
        request.setUri(uri);
        request.setType(new TypeToken<List<RepositoryHook>>() {
        }.getType());
        return getAll(request);
    }

    /**
     * Get hook from repository with given id
     *
     * @param repository
     * @param hookId
     * @return repository hook
     * @throws IOException
     */
    public RepositoryHook getHook(IRepositoryIdProvider repository, int hookId)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        uri.append('/').append(hookId);
        GitHubRequest request = createRequest();
        request.setType(RepositoryHook.class);
        request.setUri(uri);
        return (RepositoryHook) client.get(request).getBody();
    }

    /**
     * Create hook in repository
     *
     * @param repository
     * @param hook
     * @return created repository hook
     * @throws IOException
     */
    public RepositoryHook createHook(IRepositoryIdProvider repository,
            RepositoryHook hook) throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        return client.post(uri.toString(), hook, RepositoryHook.class);
    }

    /**
     * Edit hook in repository
     *
     * @param repository
     * @param hook
     * @return edited hook
     * @throws IOException
     */
    public RepositoryHook editHook(IRepositoryIdProvider repository,
            RepositoryHook hook) throws IOException {
        String id = getId(repository);
        if (hook == null)
            throw new IllegalArgumentException("Hook cannot be null"); //$NON-NLS-1$

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        uri.append('/').append(hook.getId());
        return client.post(uri.toString(), hook, RepositoryHook.class);
    }

    /**
     * Delete hook from repository
     *
     * @param repository
     * @param hookId
     * @throws IOException
     */
    public void deleteHook(IRepositoryIdProvider repository, int hookId)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        uri.append('/').append(hookId);
        client.delete(uri.toString());
    }

    /**
     * Test hook in repository. This will trigger the hook to run for the latest
     * push to the repository.
     *
     * @param repository
     * @param hookId
     * @throws IOException
     */
    public void testHook(IRepositoryIdProvider repository, int hookId)
            throws IOException {
        String id = getId(repository);
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_HOOKS);
        uri.append('/').append(hookId);
        uri.append(SEGMENT_TEST);
        client.post(uri.toString());
    }
}
