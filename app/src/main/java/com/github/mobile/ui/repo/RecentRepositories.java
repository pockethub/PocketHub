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

import com.github.mobile.RequestReader;
import com.github.mobile.RequestWriter;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Model class for the repositories recently selected under an organization
 */
public class RecentRepositories implements Comparator<Repository>, Serializable {

    /**
     * Number of repositories retained per organization
     */
    public static final int MAX_SIZE = 5;

    private static final long serialVersionUID = 580345177644233739L;

    private static final int VERSION = 2;

    private static File getFile(final Context context, final User organization) {
        return new File(context.getFilesDir(), "recent-repos-" + organization.getId() + ".ser");
    }

    private LinkedHashSet<Long> ids;

    private final File file;

    private int id;

    /**
     * Create a recent repositories cache for the given organization
     *
     * @param context
     * @param organization
     */
    public RecentRepositories(final Context context, final User organization) {
        file = getFile(context, organization);
        id = organization.getId();
    }

    private void load() {
        LinkedHashSet<Long> loaded = new RequestReader(file, VERSION).read();
        if (loaded == null)
            loaded = new LinkedHashSet<Long>();
        ids = loaded;
        trim();
    }

    private void trim() {
        Iterator<Long> iterator = ids.iterator();
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
    public RecentRepositories add(final Repository repo) {
        return repo != null ? add(repo.getId()) : this;
    }

    /**
     * Add id to recent list
     *
     * @param id
     * @return this recent list
     */
    public RecentRepositories add(final Long id) {
        if (ids == null)
            load();
        ids.remove(id);
        ids.add(id);
        trim();
        return this;
    }

    /**
     * Persist recent list asynchronously on a background thread
     *
     * @return this recent list
     */
    public RecentRepositories saveAsync() {
        if (ids != null)
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
        final LinkedHashSet<Long> save = ids;
        if (save != null)
            new RequestWriter(file, VERSION).write(save);
        return this;
    }

    /**
     * Is the given repository in the recent list?
     *
     * @param repository
     * @return true if in recent list, false otherwise
     */
    public boolean contains(Repository repository) {
        return repository != null && contains(repository.getId());
    }

    /**
     * Is the given repository id in the recent list
     *
     * @param id
     * @return true if in recent list, false otherwise
     */
    public boolean contains(long id) {
        if (ids == null)
            load();
        return ids.contains(id);
    }

    @Override
    public int compare(final Repository lhs, final Repository rhs) {
        final boolean lRecent = contains(lhs);
        final boolean rRecent = contains(rhs);
        if (lRecent && !rRecent)
            return -1;
        if (!lRecent && rRecent)
            return 1;

        final int order = CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
        if (order == 0)
            if (id == lhs.getOwner().getId())
                return -1;
            else if (id == rhs.getOwner().getId())
                return 1;
            else
                return CASE_INSENSITIVE_ORDER.compare(lhs.getOwner().getLogin(), rhs.getOwner().getLogin());
        else
            return order;
    }
}
