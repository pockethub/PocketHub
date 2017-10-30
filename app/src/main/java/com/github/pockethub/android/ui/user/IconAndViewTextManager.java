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

package com.github.pockethub.android.ui.user;

import android.text.TextUtils;
import android.view.View;

import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.Release;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.ReviewComment;
import com.meisolsson.githubsdk.model.Team;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload;
import com.meisolsson.githubsdk.model.payload.CreatePayload;
import com.meisolsson.githubsdk.model.payload.DeletePayload;
import com.meisolsson.githubsdk.model.payload.FollowPayload;
import com.meisolsson.githubsdk.model.payload.GistPayload;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;
import com.meisolsson.githubsdk.model.payload.MemberPayload;
import com.meisolsson.githubsdk.model.payload.PullRequestPayload;
import com.meisolsson.githubsdk.model.payload.PullRequestReviewCommentPayload;
import com.meisolsson.githubsdk.model.payload.PushPayload;
import com.meisolsson.githubsdk.model.payload.ReleasePayload;
import com.meisolsson.githubsdk.model.payload.TeamAddPayload;

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
            final GitHubComment comment) {
        if (comment != null) {
            appendText(details, comment.body());
        }
    }

    private void appendReviewComment(StyledText details, ReviewComment comment) {
        if (comment == null) {
            return;
        }

        String id = comment.commitId();
        if (!TextUtils.isEmpty(id)) {
            if (id.length() > 10) {
                id = id.substring(0, 10);
            }
            appendText(details, "Comment in");
            details.append(' ');
            details.monospace(id);
            details.append(':').append('\n');
        }
        appendText(details, comment.body());
    }

    private void appendCommitComment(final StyledText details,
            final GitComment comment) {
        if (comment == null) {
            return;
        }

        String id = comment.commitId();
        if (!TextUtils.isEmpty(id)) {
            if (id.length() > 10) {
                id = id.substring(0, 10);
            }
            appendText(details, "Comment in");
            details.append(' ');
            details.monospace(id);
            details.append(':').append('\n');
        }
        appendText(details, comment.body());
    }

    private void appendText(final StyledText details, String text) {
        if (text == null) {
            return;
        }
        text = text.trim();
        if (text.length() == 0) {
            return;
        }

        details.append(text);
    }

    private StyledText boldActor(final StyledText text, final GitHubEvent event) {
        return boldUser(text, event.actor());
    }

    private StyledText boldUser(final StyledText text, final User user) {
        if (user != null) {
            text.bold(user.login());
        }
        return text;
    }

    private StyledText boldRepo(final StyledText text, final GitHubEvent event) {
        Repository repo = event.repo();
        if (repo != null) {
            text.bold(repo.name());
        }
        return text;
    }

    private StyledText boldRepoName(final StyledText text,
            final GitHubEvent event) {
        Repository repo = event.repo();
        if (repo != null) {
            String name = repo.name();
            if (!TextUtils.isEmpty(name)) {
                int slash = name.indexOf('/');
                if (slash != -1 && slash + 1 < name.length()) {
                    text.bold(name.substring(slash + 1));
                }
            }
        }
        return text;
    }

    void formatCommitComment(GitHubEvent event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        CommitCommentPayload payload = (CommitCommentPayload) event.payload();
        appendCommitComment(details, payload.comment());
    }

    void formatDownload(GitHubEvent event, StyledText main,
                               StyledText details) {
        boldActor(main, event);
        main.append(" uploaded a file to ");
        boldRepo(main, event);

        ReleasePayload payload = (ReleasePayload) event.payload();
        Release download = payload.release();
        if (download != null) {
            appendText(details, download.name());
        }
    }

    void formatCreate(GitHubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        CreatePayload payload = (CreatePayload) event.payload();

        main.append(" created ");
        String refType = payload.refType();
        main.append(refType);
        main.append(' ');
        if (!"repository".equals(refType)) {
            main.append(payload.ref());
            main.append(" at ");
            boldRepo(main, event);
        } else {
            boldRepoName(main, event);
        }
    }

    void formatDelete(GitHubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        DeletePayload payload = (DeletePayload) event.payload();

        main.append(" deleted ");
        main.append(payload.refType());
        main.append(' ');
        main.append(payload.ref());
        main.append(" at ");

        boldRepo(main, event);
    }

    void formatFollow(GitHubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" started following ");
        boldUser(main, ((FollowPayload) event.payload()).target());
    }

    void formatFork(GitHubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" forked repository ");
        boldRepo(main, event);
    }

    void formatGist(GitHubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        GistPayload payload = (GistPayload) event.payload();

        main.append(' ');
        String action = payload.action();
        if ("create".equals(action)) {
            main.append("created");
        } else if ("update".equals(action)) {
            main.append("updated");
        } else {
            main.append(action);
        }
        main.append(" Gist ");
        main.append(payload.gist().id());
    }

    void formatWiki(GitHubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);
        main.append(" updated the wiki in ");
        boldRepo(main, event);
    }

    void formatIssueComment(GitHubEvent event, StyledText main,
                                   StyledText details) {
        boldActor(main, event);

        main.append(" commented on ");

        IssueCommentPayload payload = (IssueCommentPayload) event.payload();
        Issue issue = payload.issue();
        String number;
        if (IssueUtils.isPullRequest(issue)) {
            number = "pull request " + issue.number();
        } else {
            number = "issue " + issue.number();
        }
        main.bold(number);

        main.append(" on ");

        boldRepo(main, event);

        appendComment(details, payload.comment());
    }

    void formatIssues(GitHubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);

        IssuesPayload payload = (IssuesPayload) event.payload();

        String action = payload.action();
        Issue issue = payload.issue();
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("issue " + issue.number());
        main.append(" on ");

        boldRepo(main, event);

        appendText(details, issue.title());
    }

    void formatAddMember(GitHubEvent event, StyledText main,
                                StyledText details) {
        MemberPayload payload = (MemberPayload) event.payload();

        boldActor(main, event);
        main.append(" added ");
        main.bold(payload.member().login());
        main.append(" as a collaborator to ");
        boldRepo(main, event);
    }

    void formatPublic(GitHubEvent event, StyledText main,
                             StyledText details) {
        boldActor(main, event);
        main.append(" open sourced repository ");
        boldRepo(main, event);
    }

    void formatWatch(GitHubEvent event, StyledText main,
                            StyledText details) {
        boldActor(main, event);
        main.append(" starred ");
        boldRepo(main, event);
    }

    void formatReviewComment(GitHubEvent event, StyledText main,
                                    StyledText details) {
        boldActor(main, event);
        main.append(" commented on ");
        boldRepo(main, event);

        appendReviewComment(details, ((PullRequestReviewCommentPayload) event.payload()).comment());
    }

    void formatPullRequest(GitHubEvent event, StyledText main,
                                  StyledText details) {
        boldActor(main, event);

        PullRequestPayload payload = (PullRequestPayload) event.payload();

        String action = payload.action();
        if ("synchronize".equals(action)) {
            action = "updated";
        }
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("pull request " + payload.number());
        main.append(" on ");

        boldRepo(main, event);

        if (ISSUES_PAYLOAD_ACTION_OPENED.equals(action) || "closed".equals(action)) {
            PullRequest request = payload.pullRequest();
            if (request != null) {
                String title = request.title();
                if (!TextUtils.isEmpty(title)) {
                    details.append(title);
                }
            }
        }
    }

    void formatPush(GitHubEvent event, StyledText main,
                           StyledText details) {
        boldActor(main, event);

        PushPayload payload = (PushPayload) event.payload();

        main.append(" pushed to ");
        String ref = payload.ref();
        if (ref.startsWith("refs/heads/")) {
            ref = ref.substring(11);
        }
        main.bold(ref);
        main.append(" at ");

        boldRepo(main, event);

        final List<GitCommit> commits = payload.commits();
        int size = commits.size();
        if (size > 0) {
            if (size != 1) {
                details.append(FORMAT_INT.format(size)).append(" new commits");
            } else {
                details.append("1 new commit");
            }

            int max = 3;
            int appended = 0;
            for (GitCommit commit : commits) {
                if (commit == null) {
                    continue;
                }

                String sha = commit.sha();
                if (TextUtils.isEmpty(sha)) {
                    continue;
                }

                details.append('\n');
                if (sha.length() > 7) {
                    details.monospace(sha.substring(0, 7));
                } else {
                    details.monospace(sha);
                }

                String message = commit.message();
                if (!TextUtils.isEmpty(message)) {
                    details.append(' ');
                    int newline = message.indexOf('\n');
                    if (newline > 0) {
                        details.append(message.subSequence(0, newline));
                    } else {
                        details.append(message);
                    }
                }

                appended++;
                if (appended == max) {
                    break;
                }
            }
        }
    }

    void formatTeamAdd(GitHubEvent event, StyledText main,
                              StyledText details) {
        boldActor(main, event);

        main.append(" added ");

        TeamAddPayload payload = (TeamAddPayload) event.payload();
        Repository repo = payload.repository();
        String repoName = repo != null ? repo.name() : null;
        if (repoName != null) {
            main.bold(repoName);
        }

        main.append(" to team");

        Team team = payload.team();
        String teamName = team != null ? team.name() : null;
        if (teamName != null) {
            main.append(' ').bold(teamName);
        }
    }

    protected void update(int position, GitHubEvent event) {
        newsListAdapter.getAvatars().bind(newsListAdapter.imageViewAgent(0), event.actor());

        StyledText main = new StyledText();
        StyledText details = new StyledText();
        String icon = setIconAndFormatStyledText(event, main, details);

        if (icon != null) {
            newsListAdapter.setTextAgent(3, icon).setVisibility(View.VISIBLE);
        } else {
            newsListAdapter.setGoneAgent(3, true);
        }

        newsListAdapter.setTextAgent(1, main);

        if (!TextUtils.isEmpty(details)) {
            newsListAdapter.setTextAgent(2, details).setVisibility(View.VISIBLE);
        } else {
            newsListAdapter.setGoneAgent(2, true);
        }

        newsListAdapter.setTextAgent(4, TimeUtils.getRelativeTime(event.createdAt()));
    }

    String setIconAndFormatStyledText(GitHubEvent event, StyledText main, StyledText details) {

        return EventType.valueOf(event.type().toString()).generateIconAndFormatStyledText(this, event, main, details);
    }
}
