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
package com.github.pockethub.android.core.commit

import android.text.SpannedString
import android.text.TextUtils
import android.widget.ImageView
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.util.AvatarLoader
import com.meisolsson.githubsdk.model.Commit
import com.meisolsson.githubsdk.model.GitHubFile
import com.meisolsson.githubsdk.model.git.GitCommit
import java.text.NumberFormat
import java.util.Date

/**
 * Utilities for working with commits
 */
object CommitUtils {

    /**
     * Length of used for abbreviations
     */
    private const val LENGTH = 10

    private val FORMAT = NumberFormat.getIntegerInstance()

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    @JvmStatic
    fun abbreviate(commit: Commit?): String? =
            if (commit != null) abbreviate(commit.sha()) else null

    /**
     * Abbreviate commit sha to default length if longer
     *
     * @param commit
     * @return abbreviated sha
     */
    @JvmStatic
    fun abbreviate(commit: GitCommit?): String? =
            if (commit != null) abbreviate(commit.sha()) else null

    /**
     * Abbreviate sha to default length if longer
     *
     * @param sha
     * @return abbreviated sha
     */
    @JvmStatic
    fun abbreviate(sha: String?): String? =
            if (!TextUtils.isEmpty(sha) && sha!!.length > LENGTH) sha.substring(0, LENGTH) else sha

    /**
     * Is the given commit SHA-1 valid?
     *
     * @param sha
     * @return true if valid, false otherwise
     */
    @JvmStatic
    fun isValidCommit(sha: String): Boolean =
            !TextUtils.isEmpty(sha) && sha.matches("[a-fA-F0-9]+".toRegex())

    /**
     * Get author of commit
     *
     *
     * This checks both the [Commit] and the underlying
     * [Commit] to retrieve a name
     *
     * @param commit
     * @return author name or null if missing
     */
    @JvmStatic
    fun getAuthor(commit: Commit): String? {
        val author = commit.author()
        if (author != null) {
            return author.login()
        }

        val rawCommit = commit.commit() ?: return null

        return rawCommit.author()?.name()
    }

    /**
     * Get committer of commit
     *
     *
     * This checks both the [Commit] and the underlying
     * [Commit] to retrieve a name
     *
     * @param commit
     * @return committer name or null if missing
     */
    @JvmStatic
    fun getCommitter(commit: Commit): String? {
        val committer = commit.committer()
        if (committer != null) {
            return committer.login()
        }

        val rawCommit = commit.commit() ?: return null

        return rawCommit.committer()?.name()
    }

    /**
     * Get author date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    @JvmStatic
    fun getAuthorDate(commit: Commit): Date? {
        val rawCommit = commit.commit() ?: return null

        val commitAuthor = rawCommit.author()
        return commitAuthor?.date()
    }

    /**
     * Get committer date of commit
     *
     * @param commit
     * @return author name or null if missing
     */
    @JvmStatic
    fun getCommitterDate(commit: Commit): Date? {
        val rawCommit = commit.commit() ?: return null

        val commitCommitter = rawCommit.committer()
        return if (commitCommitter?.date() != null) commitCommitter.date() else null
    }

    /**
     * Bind commit author avatar to image view
     *
     * @param commit
     * @param avatars
     * @param view
     * @return view
     */
    @JvmStatic
    fun bindAuthor(commit: Commit,
                   avatars: AvatarLoader, view: ImageView): ImageView {
        val author = commit.author()
        if (author != null) {
            avatars.bind(view, author)
        }

        return view
    }

    /**
     * Bind commit committer avatar to image view
     *
     * @param commit
     * @param avatars
     * @param view
     * @return view
     */
    @JvmStatic
    fun bindCommitter(commit: Commit,
                      avatars: AvatarLoader, view: ImageView): ImageView {
        val committer = commit.committer()
        if (committer != null) {
            avatars.bind(view, committer)
        }

        return view
    }

    /**
     * Get comment count
     *
     * @param commit
     * @return count
     */
    @JvmStatic
    fun getCommentCount(commit: Commit): String {
        val rawCommit = commit.commit()
        return if (rawCommit != null) FORMAT.format(rawCommit.commentCount()) else "0"
    }

    /**
     * Format stats into [SpannedString]
     *
     * @param files
     * @return styled text
     */
    @JvmStatic
    fun formatStats(files: Collection<GitHubFile>?): SpannedString {
        return buildSpannedString {
            var added = 0
            var deleted = 0
            var changed = 0
            if (files != null) {
                for (file in files) {
                    added += file.additions()!!
                    deleted += file.deletions()!!
                    changed++
                }
            }

            if (changed != 1) {
                append("${FORMAT.format(changed.toLong())} changed files")
            } else {
                append("1 changed file")
            }
            append(" with ")

            if (added != 1) {
                append("${FORMAT.format(added.toLong())} additions")
            } else {
                append("1 addition ")
            }
            append(" and ")

            if (deleted != 1) {
                append("${FORMAT.format(deleted.toLong())} deletions")
            } else {
                append("1 deletion")
            }
        }
    }

    /**
     * Get file name for commit file
     *
     * @param file
     * @return last segment of commit file path
     */
    @JvmStatic
    fun getName(file: GitHubFile?): String? = if (file != null) getName(file.filename()) else null

    /**
     * Get file name for path
     *
     * @param path
     * @return last segment of path
     */
    @JvmStatic
    fun getName(path: String?): String? {
        if (TextUtils.isEmpty(path)) {
            return path
        }

        val lastSlash = path!!.lastIndexOf('/')
        return if (lastSlash != -1 && lastSlash + 1 < path.length) {
            path.substring(lastSlash + 1)
        } else {
            path
        }
    }
}
