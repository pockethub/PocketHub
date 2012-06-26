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
package com.github.mobile.core.issue;

import android.text.TextUtils;

import org.eclipse.egit.github.core.Issue;

/**
 * Utilities for working with {@link Issue} models
 */
public class IssueUtils {

    /**
     * Is the given issue a pull request?
     *
     * @param issue
     * @return true if pull request, false otherwise
     */
    public static boolean isPullRequest(Issue issue) {
        return issue != null && issue.getPullRequest() != null
                && !TextUtils.isEmpty(issue.getPullRequest().getHtmlUrl());
    }
}
