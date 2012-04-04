package com.github.mobile.android.core.gist;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

/**
 * Gist model with comments and starred status
 */
public class FullGist extends ArrayList<Comment> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -5966699489498437000L;

    private final Gist gist;

    private final boolean starred;

    /**
     * Create gist with comments
     *
     * @param gist
     * @param starred
     * @param comments
     */
    public FullGist(final Gist gist, final boolean starred, final List<Comment> comments) {
        super(comments);
        this.starred = starred;
        this.gist = gist;
    }

    /**
     * Create empty gist
     */
    public FullGist() {
        this.gist = null;
        this.starred = false;
    }

    /**
     * @return starred
     */
    public boolean isStarred() {
        return starred;
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
        return newArrayList(gist.getFiles().values());
    }
}
