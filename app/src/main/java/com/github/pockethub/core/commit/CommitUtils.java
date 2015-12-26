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
package com.github.pockethub.core.commit;

import android.text.TextUtils;
import android.widget.ImageView;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.alorma.github.sdk.bean.dto.response.GitCommit;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TimeUtils;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Utilities for working with commits
 */
public class CommitUtils {

    /**
     * Length of used for abbreviations
     */
    public static final int LENGTH = 10;

    private static final NumberFormat FORMAT = NumberFormat
            .getIntegerInstance();

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    public static String abbreviate(final Commit commit) {
        return commit != null ? abbreviate(commit.sha) : null;
    }

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    public static String abbreviate(final GitCommit commit) {
        return commit != null ? abbreviate(commit.sha) : null;
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
     * This checks both the {@link Commit} and the underlying
     * {@link Commit} to retrieve a name
     *
     * @param commit
     * @return author name or null if missing
     */
    public static String getAuthor(final Commit commit) {
        User author = commit.author;
        if (author != null)
            return author.login;

        GitCommit rawCommit = commit.commit;
        if (rawCommit == null)
            return null;

        User commitAuthor = rawCommit.author;
        return commitAuthor != null ? commitAuthor.login : null;
    }

    /**
     * Get committer of commit
     * <p>
     * This checks both the {@link Commit} and the underlying
     * {@link Commit} to retrieve a name
     *
     * @param commit
     * @return committer name or null if missing
     */
    public static String getCommitter(final Commit commit) {
        User committer = commit.committer;
        if (committer != null)
            return committer.login;

        GitCommit rawCommit = commit.commit;
        if (rawCommit == null)
            return null;

        User commitCommitter = rawCommit.committer;
        return commitCommitter != null ? commitCommitter.login : null;
    }

    /**
     * Get author date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    public static Date getAuthorDate(final Commit commit) {
        GitCommit rawCommit = commit.commit;
        if (rawCommit == null)
            return null;

        User commitAuthor = rawCommit.author;
        return commitAuthor != null && commitAuthor.date != null ? TimeUtils.stringToDate(commitAuthor.date) : null;
    }

    /**
     * Get committer date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    public static Date getCommitterDate(final Commit commit) {
        GitCommit rawCommit = commit.commit;
        if (rawCommit == null)
            return null;

        User commitCommitter = rawCommit.committer;
        return commitCommitter != null && commitCommitter.date != null? TimeUtils.stringToDate(commitCommitter.date): null;
    }

    /**
     * Bind commit author avatar to image view
     *
     * @param commit
     * @param avatars
     * @param view
     * @return view
     */
    public static ImageView bindAuthor(final Commit commit,
            final AvatarLoader avatars, final ImageView view) {
        User author = commit.author;
        if (author != null)
            avatars.bind(view, author);
        else {
            GitCommit rawCommit = commit.commit;
            if (rawCommit != null)
                avatars.bind(view, rawCommit.author);
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
    public static ImageView bindCommitter(final Commit commit,
            final AvatarLoader avatars, final ImageView view) {
        User committer = commit.committer;
        if (committer != null)
            avatars.bind(view, committer);
        else {
            GitCommit rawCommit = commit.commit;
            if (rawCommit != null)
                avatars.bind(view, rawCommit.committer);
        }
        return view;
    }

    /**
     * Get comment count
     *
     * @param commit
     * @return count
     */
    public static String getCommentCount(final Commit commit) {
        final GitCommit rawCommit = commit.commit;
        if (rawCommit != null)
            return FORMAT.format(rawCommit.comment_count);
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
                added += file.additions;
                deleted += file.deletions;
                changed++;
            }

        if (changed != 1)
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
    public static String getName(final CommitFile file) {
        return file != null ? getName(file.getFileName()) : null;
    }

    /**
     * Get file name for path
     *
     * @param path
     * @return last segment of path
     */
    public static String getName(final String path) {
        if (TextUtils.isEmpty(path))
            return path;

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash + 1 < path.length())
            return path.substring(lastSlash + 1);
        else
            return path;
    }
}
