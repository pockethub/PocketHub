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

package com.github.pockethub.ui.user;

import android.text.TextUtils;

import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.PullRequest;
import com.alorma.github.sdk.bean.dto.response.Release;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.Team;
import com.alorma.github.sdk.bean.dto.response.User;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.api.FollowEventPayload;
import com.github.pockethub.api.GistEventPayload;
import com.github.pockethub.core.issue.IssueUtils;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.TimeUtils;

import java.util.List;

import static com.github.kevinsawicki.wishlist.ViewUpdater.FORMAT_INT;

public class IconAndViewTextManager {
    public static final String ISSUES_PAYLOAD_ACTION_OPENED = "opened";
    public static final String ISSUES_PAYLOAD_ACTION_REOPENED = "reopened";
    public static final String ISSUES_PAYLOAD_ACTION_CLOSED = "closed";
    private final NewsListAdapter newsListAdapter;

    public IconAndViewTextManager(NewsListAdapter newsListAdapter) {
        this.newsListAdapter = newsListAdapter;
    }

    private void appendComment(final StyledText details,
            final GithubComment comment) {
        if (comment != null)
            appendText(details, comment.body);
    }

    private void appendCommitComment(final StyledText details,
            final CommitComment comment) {
        if (comment == null)
            return;

        String id = comment.commit_id;
        if (!TextUtils.isEmpty(id)) {
            if (id.length() > 10)
                id = id.substring(0, 10);
            appendText(details, "Comment in");
            details.append(' ');
            details.monospace(id);
            details.append(':').append('\n');
        }
        appendComment(details, comment);
    }

    private void appendText(final StyledText details, String text) {
        if (text == null)
            return;
        text = text.trim();
        if (text.length() == 0)
            return;

        details.append(text);
    }

    private StyledText boldActor(final StyledText text, final GithubEvent event) {
        return boldUser(text, event.actor);
    }

    private StyledText boldUser(final StyledText text, final User user) {
        if (user != null)
            text.bold(user.login);
        return text;
    }

    private StyledText boldRepo(final StyledText text, final GithubEvent event) {
        Repo repo = event.repo;
        if (repo != null)
            text.bold(repo.name);
        return text;
    }

    private StyledText boldRepoName(final StyledText text,
            final GithubEvent event) {
        Repo repo = event.repo;
        if (repo != null) {
            String name = repo.name;
            if (!TextUtils.isEmpty(name)) {
                int slash = name.indexOf('/');
                if (slash != -1 && slash + 1 < name.length())
                    text.bold(name.substring(slash + 1));
            }
        }
        return text;
    }

    void formatCommitComment(GithubEvent event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        appendCommitComment(details, event.payload.comment);
    }

    void formatDownload(GithubEvent event, StyledText main,
                               StyledText details) {
        boldActor(main, event);
        main.append(" uploaded a file to ");
        boldRepo(main, event);

        Release download = event.payload.release;
        if (download != null)
            appendText(details, download.name);
    }

    void formatCreate(GithubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        main.append(" created ");
        String refType = event.payload.ref_type;
        main.append(refType);
        main.append(' ');
        if (!"repository".equals("")) {
            main.append(event.payload.ref);
            main.append(" at ");
            boldRepo(main, event);
        } else
            boldRepoName(main, event);
    }

    void formatDelete(GithubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        main.append(" deleted ");
        main.append(event.payload.ref_type);
        main.append(' ');
        main.append(event.payload.ref);
        main.append(" at ");

        boldRepo(main, event);
    }

    void formatFollow(GithubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" started following ");
        boldUser(main, ((FollowEventPayload) event.payload).target);
    }

    void formatFork(GithubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" forked repository ");
        boldRepo(main, event);
    }

    void formatGist(GithubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        GistEventPayload payload = (GistEventPayload) event.payload;

        main.append(' ');
        String action = payload.action;
        if ("create".equals(action))
            main.append("created");
        else if ("update".equals(action))
            main.append("updated");
        else
            main.append(action);
        main.append(" Gist ");
        main.append(payload.gist.id);
    }

    void formatWiki(GithubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" updated the wiki in ");
        boldRepo(main, event);
    }

    void formatIssueComment(GithubEvent event, StyledText main,
                                   StyledText details) {
        boldActor(main, event);

        main.append(" commented on ");

        Issue issue = event.payload.issue;
        String number;
        if (IssueUtils.isPullRequest(issue))
            number = "pull request " + issue.number;
        else
            number = "issue " + issue.number;
        main.bold(number);

        main.append(" on ");

        boldRepo(main, event);

        appendComment(details, event.payload.comment);
    }

    void formatIssues(GithubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        String action = event.payload.action;
        Issue issue = event.payload.issue;
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("issue " + issue.number);
        main.append(" on ");

        boldRepo(main, event);

        appendText(details, issue.title);
    }

    void formatAddMember(GithubEvent event, StyledText main,
                                StyledText details) {
        boldActor(main, event);
        main.append(" added ");
        main.bold(event.payload.sender.login);
        main.append(" as a collaborator to ");
        boldRepo(main, event);
    }

    void formatPublic(GithubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" open sourced repository ");
        boldRepo(main, event);
    }

    void formatWatch(GithubEvent event, StyledText main,
                            StyledText details) {
        boldActor(main, event);
        main.append(" starred ");
        boldRepo(main, event);
    }

    void formatReviewComment(GithubEvent event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        appendCommitComment(details, event.payload.comment);
    }

    void formatPullRequest(GithubEvent event, StyledText main,
                                  StyledText details) {
        boldActor(main, event);

        String action = event.payload.action;
        if ("synchronize".equals(action))
            action = "updated";
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("pull request " + event.payload.number);
        main.append(" on ");

        boldRepo(main, event);

        if (ISSUES_PAYLOAD_ACTION_OPENED.equals(action) || "closed".equals(action)) {
            PullRequest request = event.payload.pull_request;
            if (request != null) {
                String title = request.title;
                if (!TextUtils.isEmpty(title))
                    details.append(title);
            }
        }
    }

    void formatPush(GithubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        main.append(" pushed to ");
        String ref = event.payload.ref;
        if (ref.startsWith("refs/heads/"))
            ref = ref.substring(11);
        main.bold(ref);
        main.append(" at ");

        boldRepo(main, event);

        final List<Commit> commits = event.payload.commits;
        int size = commits != null ? commits.size() : -1;
        if (size > 0) {
            if (size != 1)
                details.append(FORMAT_INT.format(size)).append(" new commits");
            else
                details.append("1 new commit");

            int max = 3;
            int appended = 0;
            for (Commit commit : commits) {
                if (commit == null)
                    continue;

                String sha = commit.sha;
                if (TextUtils.isEmpty(sha))
                    continue;

                details.append('\n');
                if (sha.length() > 7)
                    details.monospace(sha.substring(0, 7));
                else
                    details.monospace(sha);

                String message = commit.message;
                if (!TextUtils.isEmpty(message)) {
                    details.append(' ');
                    int newline = message.indexOf('\n');
                    if (newline > 0)
                        details.append(message.subSequence(0, newline));
                    else
                        details.append(message);
                }

                appended++;
                if (appended == max)
                    break;
            }
        }
    }

    void formatTeamAdd(GithubEvent event, StyledText main,
                              StyledText details) {
        boldActor(main, event);

        main.append(" added ");

        Repo repo = event.payload.repository;
        String repoName = repo != null ? repo.name : null;
        if (repoName != null)
            main.bold(repoName);

        main.append(" to team");

        Team team = event.payload.team;
        String teamName = team != null ? team.name : null;
        if (teamName != null)
            main.append(' ').bold(teamName);
    }

    protected void update(int position, GithubEvent event) {
        newsListAdapter.getAvatars().bind(newsListAdapter.imageViewAgent(0), event.actor);

        StyledText main = new StyledText();
        StyledText details = new StyledText();
        String icon = setIconAndFormatStyledText(event, main, details);

        if (icon != null)
            ViewUtils.setGone(newsListAdapter.setTextAgent(3, icon), false);
        else
            newsListAdapter.setGoneAgent(3, true);

        newsListAdapter.setTextAgent(1, main);

        if (!TextUtils.isEmpty(details))
            ViewUtils.setGone(newsListAdapter.setTextAgent(2, details), false);
        else
            newsListAdapter.setGoneAgent(2, true);

        newsListAdapter.setTextAgent(4, TimeUtils.getRelativeTime(event.created_at));
    }

    String setIconAndFormatStyledText(GithubEvent event, StyledText main, StyledText details) {

        return EventType.valueOf(event.type.toString()).generateIconAndFormatStyledText(this, event, main, details);
    }
}
