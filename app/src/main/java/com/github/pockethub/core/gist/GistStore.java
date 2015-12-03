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
package com.github.pockethub.core.gist;

import android.content.Context;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GistFile;
import com.alorma.github.sdk.services.gists.EditGistClient;
import com.alorma.github.sdk.services.gists.GetGistDetailClient;
import com.github.pockethub.core.ItemStore;
import com.github.pockethub.util.RequestUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Store of Gists
 */
public class GistStore extends ItemStore {

    private final ItemReferences<Gist> gists = new ItemReferences<>();

    private Context context;

    /**
     * Create gist store
     *
     * @param context
     */
    public GistStore(final Context context) {
        this.context = context;
    }

    /**
     * Get gist
     *
     * @param id
     * @return gist or null if not in store
     */
    public Gist getGist(String id) {
        return gists.get(id);
    }

    /**
     * Sort files in {@link Gist}
     *
     * @param gist
     * @return sorted files
     */
    protected Map<String, GistFile> sortFiles(final Gist gist) {
        Map<String, GistFile> files = gist.files;
        if (files == null || files.size() < 2)
            return files;

        Map<String, GistFile> sorted = new TreeMap<>(
                CASE_INSENSITIVE_ORDER);
        sorted.putAll(files);
        return sorted;
    }

    /**
     * Add gist to store
     *
     * @param gist
     * @return gist
     */
    public Gist addGist(Gist gist) {
        Gist current = getGist(gist.id);
        if (current != null) {
            current.comments = gist.comments;
            current.description = gist.description;
            current.files = sortFiles(gist);
            current.updated_at = gist.updated_at;
            return current;
        } else {
            gist.files = sortFiles(gist);
            gists.put(gist.id, gist);
            return gist;
        }
    }

    /**
     * Refresh gist
     *
     * @param id
     * @return refreshed gist
     * @throws IOException
     */
    public Gist refreshGist(String id) throws IOException {
        return addGist(new GetGistDetailClient(context, id).executeSync());
    }

    /**
     * Edit gist
     *
     * @param gist
     * @return edited gist
     * @throws IOException
     */
    public Gist editGist(Gist gist) throws IOException {
        return addGist(new EditGistClient(context, gist.id, RequestUtils.editGist(gist)).executeSync());
    }
}
