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

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.github.mobile.RequestReader;
import com.github.mobile.RequestWriter;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;

/**
 * Model class for the repositories recently selected under an organization
 */
public class RecentRepositories implements Comparator<IRepositoryIdProvider>, Serializable {

    /**
     * Number of repositories retained per organization
     */
    public static final int MAX_SIZE = 5;

    private static final long serialVersionUID = 580345177644233739L;

    private static final int VERSION = 1;

    private static File getFile(final Context context, final User organization) {
        return new File(context.getFilesDir(), "recent-repos-" + organization.getId() + ".ser");
    }

    private LinkedHashSet<String> ids;

    private final File file;

    /**
     * Create a recent repositories cache for the given organization
     *
     * @param context
     * @param organization
     */
    public RecentRepositories(final Context context, final User organization) {
        file = getFile(context, organization);

        LinkedHashSet<String> loaded = new RequestReader(file, VERSION).read();
        if (loaded == null)
            loaded = new LinkedHashSet<String>();

        trim();
    }

    private void load() {
        LinkedHashSet<String> loaded = new RequestReader(file, VERSION).read();
        if (loaded == null)
            loaded = new LinkedHashSet<String>();
        ids = loaded;
    }

    private void trim() {
        if (ids == null)
            return;

        Iterator<String> iterator = ids.iterator();
        while (iterator.hasNext() && ids.size() > MAX_SIZE) {
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Add repository to recent list
     *
     * @param repo
     * @return this recent list
     */
    public RecentRepositories add(final IRepositoryIdProvider repo) {
        return repo != null ? add(repo.generateId()) : this;
    }

    /**
     * Add id to recent list
     *
     * @param repoId
     * @return this recent list
     */
    public RecentRepositories add(final String repoId) {
        if (TextUtils.isEmpty(repoId))
            return this;

        if (ids == null)
            load();
        ids.remove(repoId);
        ids.add(repoId);
        trim();
        return this;
    }

    /**
     * Persist recent list asynchronously on a background thread
     *
     * @return this recent list
     */
    public RecentRepositories saveAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                save();
                return null;
            }
        }.execute();
        return this;
    }

    /**
     * Persist recent list
     *
     * @return this recent list
     */
    public RecentRepositories save() {
        new RequestWriter(file, VERSION).write(ids);
        return this;
    }

    /**
     * Is the given repository in the recent list?
     *
     * @param repository
     * @return true if in recent list, false otherwise
     */
    public boolean contains(IRepositoryIdProvider repository) {
        return repository != null && contains(repository.generateId());
    }

    /**
     * Is the given repository id in the recent list
     *
     * @param repositoryId
     * @return true if in recent list, false otherwise
     */
    public boolean contains(String repositoryId) {
        if (TextUtils.isEmpty(repositoryId))
            return false;
        if (ids == null)
            load();
        return ids.contains(repositoryId);
    }

    @Override
    public int compare(final IRepositoryIdProvider lhs, final IRepositoryIdProvider rhs) {
        String lId = lhs.generateId();
        String rId = rhs.generateId();

        boolean lRecent = contains(lId);
        boolean rRecent = contains(rId);
        if (lRecent && !rRecent)
            return -1;
        if (!lRecent && rRecent)
            return 1;

        return CASE_INSENSITIVE_ORDER.compare(lId, rId);
    }
}
