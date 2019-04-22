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
package com.github.pockethub.android.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.github.pockethub.android.GetFilterLabels;
import com.github.pockethub.android.GetFilters;
import com.github.pockethub.android.RequestReader;
import com.github.pockethub.android.RequestWriter;
import com.github.pockethub.android.Users;
import com.github.pockethub.android.core.issue.IssueFilter;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Permissions;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import io.reactivex.Single;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manager cache for an account
 */
public class AccountDataManager {

    private static final String TAG = "AccountDataManager";

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * Format version to bump if serialization format changes and cache should
     * be ignored
     */
    private static final int FORMAT_VERSION = 4;

    @Inject
    protected Context context;

    @Inject
    protected DatabaseCache dbCache;

    @Inject
    protected OrganizationRepositoriesFactory allRepos;

    @Inject
    protected Organizations userAndOrgsResource;

    @Inject
    @Named("cacheDir")
    protected File root;

    @Inject
    public AccountDataManager() {}

    /**
     * @return context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Read data from file
     *
     * @param file
     * @return data
     */
    @SuppressWarnings("unchecked")
    private <V> V read(final File file) {
        long start = System.currentTimeMillis();
        long length = file.length();
        Object data = new RequestReader(file, FORMAT_VERSION).read();
        if (data != null) {
            Log.d(TAG, MessageFormat.format(
                    "Cache hit to {0}, {1} ms to load {2} bytes",
                    file.getName(), (System.currentTimeMillis() - start),
                    length));
        }
        return (V) data;
    }

    /**
     * Write data to file
     *
     * @param file
     * @param data
     * @return this manager
     */
    private AccountDataManager write(File file, Object data) {
        new RequestWriter(file, FORMAT_VERSION).write(data);
        return this;
    }

    /**
     * Query tables for columns
     *
     * @param helper
     * @param tables
     * @param columns
     * @return cursor
     */
    protected Cursor query(SQLiteOpenHelper helper, String tables,
            String[] columns) {
        return query(helper, tables, columns, null, null);
    }

    /**
     * Query tables for columns
     *
     * @param helper
     * @param tables
     * @param columns
     * @param selection
     * @param selectionArgs
     * @return cursor
     */
    protected Cursor query(SQLiteOpenHelper helper, String tables,
            String[] columns, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tables);
        return builder.query(helper.getReadableDatabase(), columns, selection,
                selectionArgs, null, null, null);
    }

    /**
     * Get organizations
     * <p/>
     * This method may perform file and/or network I/O and should never be
     * called on the UI-thread
     *
     * @param forceReload
     * @return list of user and Orgs
     * @throws IOException
     */
    public List<User> getOrgs(boolean forceReload) throws IOException {
        return forceReload ? dbCache.requestAndStore(userAndOrgsResource)
                : dbCache.loadOrRequest(userAndOrgsResource);
    }

    /**
     * Get repositories for given {@link User}
     * <p/>
     * This method may perform network I/O and should never be called on the
     * UI-thread
     *
     * @param user
     * @param forceReload
     *            if true, cached data will not be returned
     * @return list of repositories
     * @throws IOException
     */
    public List<Repository> getRepos(final User user, boolean forceReload)
            throws IOException {
        OrganizationRepositories resource = allRepos.create(user);
        return forceReload ? dbCache.requestAndStore(resource) : dbCache
                .loadOrRequest(resource);
    }

    /**
     * Get bookmarked issue filters
     * <p/>
     * This method may perform network I/O and should never be called on the
     * UI-thread
     *
     * @return non-null but possibly empty collection of issue filters
     */
    public List<IssueFilter> getIssueFilters() {
        List<GetFilters> filters = dbCache.database.getIssue_filterQueries()
                .getFilters()
                .executeAsList();

        List<IssueFilter> issueFilters = new ArrayList<>();

        for (GetFilters f : filters) {
            Users owner = dbCache.database.getOrganizationsQueries().selectUser(f.getOwnerId()).executeAsOne();

            Repository.Builder builder = Repository.builder()
                    .id(f.getRepoId())
                    .name(f.getName())
                    .owner(
                            User.builder()
                                    .id(owner.getId())
                                    .login(owner.getLogin())
                                    .name(owner.getName())
                                    .avatarUrl(owner.getAvatarurl())
                                    .build()
                    )
                    .isPrivate(f.getPrivate())
                    .isFork(f.getFork())
                    .description(f.getDescription())
                    .forksCount(f.getForks())
                    .watchersCount(f.getWatchers())
                    .language(f.getLanguage())
                    .hasIssues(f.getHasIssues())
                    .mirrorUrl(f.getMirrorUrl())
                    .permissions(
                            Permissions.builder()
                                    .admin(f.getPermissions_admin())
                                    .pull(f.getPermissions_pull())
                                    .push(f.getPermissions_push())
                                    .build()
                    );

            if (f.getOrgId() != null) {
                Users org = dbCache.database.getOrganizationsQueries().selectUser(f.getOrgId()).executeAsOne();
                builder.organization(
                        User.builder()
                                .id(org.getId())
                                .login(org.getLogin())
                                .name(org.getName())
                                .avatarUrl(org.getAvatarurl())
                                .build()
                );
            }

            Repository repo = builder.build();
            IssueFilter filter = new IssueFilter(repo, f.getId());
            if (f.getLogin() != null) {
                filter.setAssignee(
                        User.builder()
                                .id(f.getId__())
                                .name(f.getName_())
                                .login(f.getLogin())
                                .avatarUrl(f.getAvatarurl())
                                .build()
                );
            }

            if (f.getMilestone_id() != null) {
                filter.setMilestone(Milestone.builder().id(f.getMilestone_id()).build());
            }

            filter.setDirection(f.getDirection());
            filter.setOpen(f.getOpen());
            filter.setSortType(f.getSort_type());

            List<GetFilterLabels> filterLabels = dbCache.database.getIssue_filterQueries()
                    .getFilterLabels(filter.getId())
                    .executeAsList();

            for (GetFilterLabels filterLabel : filterLabels) {
                filter.addLabel(
                        Label.builder()
                                .name(filterLabel.getName())
                                .color(filterLabel.getColor())
                                .build()
                );
            }

            issueFilters.add(filter);
        }

        return issueFilters;
    }

    /**
     * Add issue filter to store
     * <p/>
     * This method may perform file I/O and should never be called on the
     * UI-thread
     *
     * @param filter
     */
    public Single<IssueFilter> addIssueFilter(final IssueFilter filter) {
        Repository repo = filter.getRepository();
        dbCache.getDatabase().getRepositoriesQueries().replaceRepo(
                repo.id(),
                repo.name(),
                repo.organization() != null ? repo.organization().id() : null,
                repo.owner().id(),
                repo.isPrivate(),
                repo.isFork(),
                repo.description(),
                repo.forksCount(),
                repo.watchersCount(),
                repo.language(),
                repo.hasIssues(),
                repo.mirrorUrl(),
                repo.permissions().admin(),
                repo.permissions().pull(),
                repo.permissions().push()
        );

        for (Label label : filter.getLabels()) {
            dbCache.database.getIssue_filterQueries().insertOrReplaceLabel(
                    repo.id(),
                    label.name(),
                    label.color()
            );
            dbCache.database.getIssue_filterQueries().insertOrReplaceIssueFilterLabel(
                    filter.getId(),
                    repo.id(),
                    label.name()
            );
        }

        if (filter.getMilestone() != null) {
            dbCache.database.getIssue_filterQueries().insertOrReplaceMilestone(
                    repo.id(),
                    filter.getMilestone().title(),
                    filter.getMilestone().state(),
                    filter.getMilestone().id(),
                    filter.getMilestone().number().longValue()
            );
        }

        if (filter.getAssignee() != null) {
            dbCache.database.getOrganizationsQueries().replaceUser(
                    filter.getAssignee().id(),
                    filter.getAssignee().login(),
                    filter.getAssignee().name(),
                    filter.getAssignee().avatarUrl()
            );
        }

        dbCache.database.getIssue_filterQueries().insertOrReplaceIssueFilter(
                filter.getId(),
                repo.id(),
                filter.getMilestone() != null ? filter.getMilestone().id() : null,
                filter.getAssignee() != null ? filter.getAssignee().id() : null,
                filter.isOpen(),
                filter.getDirection(),
                filter.getSortType()
        );
        return Single.just(filter);
    }

    /**
     * Add issue filter from store
     * <p/>
     * This method may perform file I/O and should never be called on the
     * UI-thread
     *
     * @param filter
     */
    public Single<IssueFilter> removeIssueFilter(IssueFilter filter) {
        dbCache.database.getIssue_filterQueries().removeIssueFilter(filter.getId());
        for (Label label : filter.getLabels()) {
            dbCache.database.getIssue_filterQueries().removeIssueFilterLabel(
                    filter.getId(),
                    filter.getRepository().id(),
                    label.name()
            );
        }

        return Single.just(filter);
    }
}
