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
package com.github.pockethub.android.core.gist;

import android.content.Context;

import com.github.pockethub.android.core.ItemStore;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GistFile;
import com.meisolsson.githubsdk.model.request.gist.CreateGist;
import com.meisolsson.githubsdk.service.gists.GistService;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Single;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

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
        Map<String, GistFile> files = gist.files();
        if (files == null || files.size() < 2) {
            return files;
        }

        Map<String, GistFile> sorted = new TreeMap<>(CASE_INSENSITIVE_ORDER);
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
        Gist current = getGist(gist.id());
        if (current != null && current.equals(gist)) {
            return current;
        }

        gist = gist.toBuilder()
                .files(sortFiles(gist))
                .build();
        gists.put(gist.id(), gist);
        return gist;
    }

    /**
     * Refresh gist.
     *
     * @param id The id of the Gist to update
     * @return refreshed gist
     */
    public Single<Gist> refreshGist(String id) {
        return ServiceGenerator.createService(context, GistService.class).getGist(id)
                .map(response -> addGist(response.body()));
    }

    /**
     * Edit gist.
     *
     * @param gist The Gist to edit
     * @return edited gist
     */
    public Single<Gist> editGist(Gist gist) {
        CreateGist edit = CreateGist.builder()
                .files(gist.files())
                .description(gist.description())
                .isPublic(gist.isPublic())
                .build();

        return ServiceGenerator.createService(context, GistService.class).editGist(edit)
                .map(response -> addGist(response.body()));
    }
}
