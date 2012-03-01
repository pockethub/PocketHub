package com.github.mobile.android.gist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

/**
 * Gist model with comments
 */
public class FullGist extends ArrayList<Comment> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -5966699489498437000L;

    private final Gist gist;

    /**
     * Create gist with comments
     *
     * @param gist
     * @param comments
     */
    public FullGist(final Gist gist, final List<Comment> comments) {
        super(comments);
        this.gist = gist;
    }

    /**
     * Create empty gist
     */
    public FullGist() {
        this.gist = null;
    }

    /**
     * @return gist
     */
    public Gist getGist() {
        return gist;
    }

    /**
     * Get files in gist
     *
     * @return list of files
     */
    public List<GistFile> getFiles() {
        return new ArrayList<GistFile>(gist.getFiles().values());
    }
}
