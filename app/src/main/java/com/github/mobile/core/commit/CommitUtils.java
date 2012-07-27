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

import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;

/**
 * Utilities for working with commits
 */
public class CommitUtils {

    private static final NumberFormat FORMAT = NumberFormat
            .getIntegerInstance();

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
        return !TextUtils.isEmpty(sha) && sha.matches("[a-fA-F0-9]+");
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

    /**
     * Get comment count
     *
     * @param commit
     * @return count
     */
    public static String getCommentCount(final RepositoryCommit commit) {
        final Commit rawCommit = commit.getCommit();
        if (rawCommit != null)
            return FORMAT.format(rawCommit.getCommentCount());
        else
            return "0";
    }

    /**
     * Format stats into {@link StyledText}
     *
     * @param files
     * @return styled text
     */
    public static StyledText formatStats(final Collection<CommitFile> files) {
        StyledText fileDetails = new StyledText();
        int added = 0;
        int deleted = 0;
        int changed = 0;
        if (files != null)
            for (CommitFile file : files) {
                added += file.getAdditions();
                deleted += file.getDeletions();
                changed++;
            }

        if (changed > 1)
            fileDetails.append(FORMAT.format(changed)).append(" changed files");
        else
            fileDetails.append("1 changed file");
        fileDetails.append(" with ");

        if (added != 1)
            fileDetails.append(FORMAT.format(added)).append(" additions");
        else
            fileDetails.append("1 addition ");
        fileDetails.append(" and ");

        if (deleted != 1)
            fileDetails.append(FORMAT.format(deleted)).append(" deletions");
        else
            fileDetails.append("1 deletion");

        return fileDetails;
    }

    /**
     * Get file name for commit file
     *
     * @param file
     * @return last segment of commit file path
     */
    public static String getName(CommitFile file) {
        String path = file.getFilename();
        if (TextUtils.isEmpty(path))
            return null;

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash + 1 < path.length())
            return path.substring(lastSlash + 1);
        else
            return path;
    }
}
