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
package com.github.mobile.ui;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import android.content.Intent;
import android.net.Uri;

import com.github.mobile.core.gist.GistUrlMatcher;
import com.github.mobile.core.issue.IssueUrlMatcher;
import com.github.mobile.ui.gist.GistsViewActivity;
import com.github.mobile.ui.issue.IssuesViewActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;

/**
 * Helper to handle any custom activity launching done by selecting URLs
 */
public class UrlLauncher {

    private final GistUrlMatcher gistMatcher = new GistUrlMatcher();

    private final IssueUrlMatcher issueMatcher = new IssueUrlMatcher();

    /**
     * Create intent to launch view of URI
     *
     * @param uri
     * @return intent
     */
    public Intent create(final String uri) {
        int issueNumber = issueMatcher.getNumber(uri);
        if (issueNumber > 0)
            return createIssueIntent(uri, issueNumber);

        String gistId = gistMatcher.getId(uri);
        if (gistId != null)
            return createGistIntent(uri, gistId);

        Intent intent = new Intent(ACTION_VIEW, Uri.parse(uri));
        intent.addCategory(CATEGORY_BROWSABLE);
        return intent;
    }

    private Intent createIssueIntent(final String uri, final int number) {
        Issue issue = new Issue();
        issue.setNumber(number);
        issue.setHtmlUrl(uri);
        return IssuesViewActivity.createIntent(issue);
    }

    private Intent createGistIntent(final String uri, final String id) {
        Gist gist = new Gist().setId(id).setHtmlUrl(uri);
        return GistsViewActivity.createIntent(gist);
    }

    /**
     * Convert global view intent one into one that can be possibly opened inside the current application.
     *
     * @param intent
     * @return converted intent or null if non-application specific
     */
    public Intent convert(final Intent intent) {
        if (intent == null)
            return null;

        if (!ACTION_VIEW.equals(intent.getAction()))
            return null;

        Uri data = intent.getData();
        if (data == null)
            return null;

        String uri = data.toString();
        int issueNumber = issueMatcher.getNumber(uri);
        if (issueNumber > 0)
            return createIssueIntent(uri, issueNumber);

        String gistId = gistMatcher.getId(uri);
        if (gistId != null)
            return createGistIntent(uri, gistId);

        return null;
    }
}
