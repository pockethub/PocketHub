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
import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_DEFAULT;
import static org.eclipse.egit.github.core.client.IGitHubConstants.PROTOCOL_HTTPS;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.github.mobile.accounts.AccountUtils;
import com.github.mobile.core.commit.CommitMatch;
import com.github.mobile.core.commit.CommitUrlMatcher;
import com.github.mobile.core.gist.GistUrlMatcher;
import com.github.mobile.core.issue.IssueUrlMatcher;
import com.github.mobile.core.user.UserUrlMatcher;
import com.github.mobile.ui.commit.CommitViewActivity;
import com.github.mobile.ui.gist.GistsViewActivity;
import com.github.mobile.ui.issue.IssuesViewActivity;
import com.github.mobile.ui.user.UserViewActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;

/**
 * Helper to handle any custom activity launching done by selecting URLs
 */
public class UrlLauncher {

    private final GistUrlMatcher gistMatcher = new GistUrlMatcher();

    private final IssueUrlMatcher issueMatcher = new IssueUrlMatcher();

    private final CommitUrlMatcher commitMatcher = new CommitUrlMatcher();

    private final UserUrlMatcher userMatcher = new UserUrlMatcher();

    private final Context context;

    /**
     * @param context
     */
    public UrlLauncher(final Context context) {
        this.context = context;
    }

    private boolean isValidLogin(final String login) {
        return login != null && !login.equals(AccountUtils.getLogin(context));
    }

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

        CommitMatch commitMatch = commitMatcher.getCommit(uri);
        if (commitMatch != null)
            return createCommitIntent(uri, commitMatch);

        String login = userMatcher.getLogin(uri);
        if (isValidLogin(login))
            return createUserIntent(login);

        Intent intent = new Intent(ACTION_VIEW, Uri.parse(uri));
        intent.addCategory(CATEGORY_BROWSABLE);
        return intent;
    }

    private Intent createUserIntent(final String login) {
        return UserViewActivity.createIntent(new User().setLogin(login));
    }

    private Intent createCommitIntent(final String uri, final CommitMatch match) {
        return CommitViewActivity.createIntent(match.repository, match.commit);
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
     * Convert global view intent one into one that can be possibly opened
     * inside the current application.
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

        if (TextUtils.isEmpty(data.getHost())
                || TextUtils.isEmpty(data.getScheme())) {
            String host = data.getHost();
            if (TextUtils.isEmpty(host))
                host = HOST_DEFAULT;
            String scheme = data.getScheme();
            if (TextUtils.isEmpty(scheme))
                scheme = PROTOCOL_HTTPS;
            String prefix = scheme + "://" + host;

            String path = data.getPath();
            if (!TextUtils.isEmpty(path))
                if (path.charAt(0) == '/')
                    data = Uri.parse(prefix + path);
                else
                    data = Uri.parse(prefix + '/' + path);
            else
                data = Uri.parse(prefix);
            intent.setData(data);
        }

        String uri = data.toString();
        int issueNumber = issueMatcher.getNumber(uri);
        if (issueNumber > 0)
            return createIssueIntent(uri, issueNumber);

        String gistId = gistMatcher.getId(uri);
        if (gistId != null)
            return createGistIntent(uri, gistId);

        CommitMatch match = commitMatcher.getCommit(uri);
        if (match != null)
            return createCommitIntent(uri, match);

        String login = userMatcher.getLogin(uri);
        if (isValidLogin(login))
            return createUserIntent(login);

        return null;
    }
}
