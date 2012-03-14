package com.github.mobile.android.gist;

import com.github.mobile.android.ItemStore;

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
