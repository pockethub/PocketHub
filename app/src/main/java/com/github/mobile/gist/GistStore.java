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
package com.github.mobile.gist;

import com.github.mobile.ItemStore;

import java.io.IOException;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Store of Gists
 */
public class GistStore extends ItemStore {

    private final ItemReferences<Gist> gists = new ItemReferences<Gist>();

    private final GistService service;

    /**
     * Create gist store
     *
     * @param service
     */
    public GistStore(final GistService service) {
        this.service = service;
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
     * Add gist to store
     *
     * @param gist
     * @return gist
     */
    public Gist addGist(Gist gist) {
        Gist current = getGist(gist.getId());
        if (current != null) {
            current.setComments(gist.getComments());
            current.setDescription(gist.getDescription());
            current.setFiles(gist.getFiles());
            current.setUpdatedAt(gist.getUpdatedAt());
            return current;
        } else {
            gists.put(gist.getId(), gist);
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
        return addGist(service.getGist(id));
    }

    /**
     * Edit gist
     *
     * @param gist
     * @return edited gist
     * @throws IOException
     */
    public Gist editGist(Gist gist) throws IOException {
        return addGist(service.updateGist(gist));
    }
}
