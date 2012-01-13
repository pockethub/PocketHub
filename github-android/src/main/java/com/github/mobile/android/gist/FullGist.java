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
public class FullGist implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -5966699489498437000L;

    private final Gist gist;

    private final List<Comment> comments;

    /**
     * Create gist with comments
     *
     * @param gist
     * @param comments
     */
    public FullGist(final Gist gist, final List<Comment> comments) {
        this.gist = gist;
        this.comments = comments;
    }

    /**
     * @return gist
     */
    public Gist getGist() {
        return gist;
    }

    /**
     * @return comments
     */
    public List<Comment> getComments() {
        return comments;
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
