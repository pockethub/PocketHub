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
package com.github.mobile.core.commit;

import android.text.TextUtils;
import android.widget.ImageView;

import com.github.mobile.util.AvatarLoader;

import java.util.Date;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;

/**
 * Utilities for working with commits
 */
public class CommitUtils {

    private static final int LENGTH = 10;

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    public static String abbreviate(final RepositoryCommit commit) {
        return commit != null ? abbreviate(commit.getSha()) : null;
    }

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    public static String abbreviate(final Commit commit) {
        return commit != null ? abbreviate(commit.getSha()) : null;
    }

    /**
     * Abbreviate sha to default length if longer
     *
     * @param sha
     * @return abbreviated sha
     */
    public static String abbreviate(final String sha) {
        if (!TextUtils.isEmpty(sha) && sha.length() > LENGTH)
            return sha.substring(0, LENGTH);
        else
            return sha;
    }

    /**
     * Is the given commit SHA-1 valid?
     *
     * @param sha
     * @return true if valid, false otherwise
     */
    public static boolean isValidCommit(final String sha) {
        if (!TextUtils.isEmpty(sha))
            return sha.matches("[a-fA-F0-9]+");
        else
            return false;
    }

    /**
     * Get author of commit
     * <p>
     * This checks both the {@link RepositoryCommit} and the underlying
     * {@link Commit} to retrieve a name
     *
     * @param commit
     * @return author name or null if missing
     */
    public static String getAuthor(final RepositoryCommit commit) {
        User author = commit.getAuthor();
        if (author != null)
            return author.getLogin();

        Commit rawCommit = commit.getCommit();
        if (rawCommit == null)
            return null;

        CommitUser commitAuthor = rawCommit.getAuthor();
        return commitAuthor != null ? commitAuthor.getName() : null;
    }

    /**
     * Get committer of commit
     * <p>
     * This checks both the {@link RepositoryCommit} and the underlying
     * {@link Commit} to retrieve a name
     *
     * @param commit
     * @return committer name or null if missing
     */
    public static String getCommitter(final RepositoryCommit commit) {
        User committer = commit.getCommitter();
        if (committer != null)
            return committer.getLogin();

        Commit rawCommit = commit.getCommit();
        if (rawCommit == null)
            return null;

        CommitUser commitCommitter = rawCommit.getCommitter();
        return commitCommitter != null ? commitCommitter.getName() : null;
    }

    /**
     * Get author date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    public static Date getAuthorDate(final RepositoryCommit commit) {
        Commit rawCommit = commit.getCommit();
        if (rawCommit == null)
            return null;

        CommitUser commitAuthor = rawCommit.getAuthor();
        return commitAuthor != null ? commitAuthor.getDate() : null;
    }

    /**
     * Get committer date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    public static Date getCommiterDate(final RepositoryCommit commit) {
        Commit rawCommit = commit.getCommit();
        if (rawCommit == null)
            return null;

        CommitUser commitCommiter = rawCommit.getCommitter();
        return commitCommiter != null ? commitCommiter.getDate() : null;
    }

    /**
     * Bind commit author avatar to image view
     *
     * @param commit
     * @param avatars
     * @param view
     * @return view
     */
    public static ImageView bindAuthor(final RepositoryCommit commit,
            final AvatarLoader avatars, final ImageView view) {
        User author = commit.getAuthor();
        if (author != null)
            avatars.bind(view, author);
        else {
            Commit rawCommit = commit.getCommit();
            if (rawCommit != null)
                avatars.bind(view, rawCommit.getAuthor());
        }
        return view;
    }

    /**
     * Bind commit committer avatar to image view
     *
     * @param commit
     * @param avatars
     * @param view
     * @return view
     */
    public static ImageView bindCommitter(final RepositoryCommit commit,
            final AvatarLoader avatars, final ImageView view) {
        User committer = commit.getCommitter();
        if (committer != null)
            avatars.bind(view, committer);
        else {
            Commit rawCommit = commit.getCommit();
            if (rawCommit != null)
                avatars.bind(view, rawCommit.getCommitter());
        }
        return view;
    }
}
