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
package com.github.mobile.ui.user;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.util.TypefaceUtils.ICON_ADD_MEMBER;
import static com.github.mobile.util.TypefaceUtils.ICON_COMMENT;
import static com.github.mobile.util.TypefaceUtils.ICON_CREATE;
import static com.github.mobile.util.TypefaceUtils.ICON_DELETE;
import static com.github.mobile.util.TypefaceUtils.ICON_FOLLOW;
import static com.github.mobile.util.TypefaceUtils.ICON_FORK;
import static com.github.mobile.util.TypefaceUtils.ICON_GIST;
import static com.github.mobile.util.TypefaceUtils.ICON_ISSUE_CLOSE;
import static com.github.mobile.util.TypefaceUtils.ICON_ISSUE_COMMENT;
import static com.github.mobile.util.TypefaceUtils.ICON_ISSUE_OPEN;
import static com.github.mobile.util.TypefaceUtils.ICON_ISSUE_REOPEN;
import static com.github.mobile.util.TypefaceUtils.ICON_PULL_REQUEST;
import static com.github.mobile.util.TypefaceUtils.ICON_PUSH;
import static com.github.mobile.util.TypefaceUtils.ICON_UPLOAD;
import static com.github.mobile.util.TypefaceUtils.ICON_WATCH;
import static com.github.mobile.util.TypefaceUtils.ICON_WIKI;
import static org.eclipse.egit.github.core.event.Event.TYPE_COMMIT_COMMENT;
import static org.eclipse.egit.github.core.event.Event.TYPE_CREATE;
import static org.eclipse.egit.github.core.event.Event.TYPE_DELETE;
import static org.eclipse.egit.github.core.event.Event.TYPE_DOWNLOAD;
import static org.eclipse.egit.github.core.event.Event.TYPE_FOLLOW;
import static org.eclipse.egit.github.core.event.Event.TYPE_FORK;
import static org.eclipse.egit.github.core.event.Event.TYPE_FORK_APPLY;
import static org.eclipse.egit.github.core.event.Event.TYPE_GIST;
import static org.eclipse.egit.github.core.event.Event.TYPE_GOLLUM;
import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUES;
import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUE_COMMENT;
import static org.eclipse.egit.github.core.event.Event.TYPE_MEMBER;
import static org.eclipse.egit.github.core.event.Event.TYPE_PUBLIC;
import static org.eclipse.egit.github.core.event.Event.TYPE_PULL_REQUEST;
import static org.eclipse.egit.github.core.event.Event.TYPE_PULL_REQUEST_REVIEW_COMMENT;
import static org.eclipse.egit.github.core.event.Event.TYPE_PUSH;
import static org.eclipse.egit.github.core.event.Event.TYPE_TEAM_ADD;
import static org.eclipse.egit.github.core.event.Event.TYPE_WATCH;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TimeUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Download;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.CommitCommentPayload;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.DeletePayload;
import org.eclipse.egit.github.core.event.DownloadPayload;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.FollowPayload;
import org.eclipse.egit.github.core.event.GistPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.event.PullRequestReviewCommentPayload;
import org.eclipse.egit.github.core.event.PushPayload;
import org.eclipse.egit.github.core.event.TeamAddPayload;

/**
 * Adapter for a list of news events
 */
public class NewsListAdapter extends ItemListAdapter<Event, NewsItemView> {

    /**
     * Can the given event be rendered by this view holder?
     *
     * @param event
     * @return true if renderable, false otherwise
     */
    public static boolean isValid(final Event event) {
        if (event == null)
            return false;

        final EventPayload payload = event.getPayload();
        if (payload == null || EventPayload.class.equals(payload.getClass()))
            return false;

        final String type = event.getType();
        if (TextUtils.isEmpty(type))
            return false;

        return TYPE_COMMIT_COMMENT.equals(type) //
                || TYPE_CREATE.equals(type) //
                || TYPE_DELETE.equals(type) //
                || TYPE_DOWNLOAD.equals(type) //
                || TYPE_FOLLOW.equals(type) //
                || TYPE_FORK.equals(type) //
                || TYPE_FORK_APPLY.equals(type) //
                || TYPE_GIST.equals(type) //
                || TYPE_GOLLUM.equals(type) //
                || TYPE_ISSUE_COMMENT.equals(type) //
                || TYPE_ISSUES.equals(type) //
                || TYPE_MEMBER.equals(type) //
                || TYPE_PUBLIC.equals(type) //
                || TYPE_PULL_REQUEST.equals(type) //
                || TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type) //
                || TYPE_PUSH.equals(type) //
                || TYPE_TEAM_ADD.equals(type) //
                || TYPE_WATCH.equals(type);
    }

    private static void appendComment(final StyledText details, final Comment comment) {
        if (comment != null)
            appendText(details, comment.getBody());
    }

    private static void appendCommitComment(final StyledText details, final CommitComment comment) {
        if (comment == null)
            return;

        String id = comment.getCommitId();
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

    private static void appendText(final StyledText details, String text) {
        if (text == null)
            return;
        text = text.trim();
        if (text.length() == 0)
            return;

        details.append(text);
    }

    private static void formatCommitComment(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" commented on ");
        main.bold(event.getRepo().getName());

        CommitCommentPayload payload = (CommitCommentPayload) event.getPayload();
        appendCommitComment(details, payload.getComment());
    }

    private static void formatDownload(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" uploaded a file to ");
        main.bold(event.getRepo().getName());

        DownloadPayload payload = (DownloadPayload) event.getPayload();
        Download download = payload.getDownload();
        if (download != null)
            appendText(details, download.getName());
    }

    private static void formatCreate(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        main.append(" created ");
        CreatePayload payload = (CreatePayload) event.getPayload();
        String refType = payload.getRefType();
        main.append(refType);
        main.append(' ');
        String repoName = event.getRepo().getName();
        if (!"repository".equals(refType)) {
            main.append(payload.getRef());
            main.append(" at ");
        } else
            repoName = repoName.substring(repoName.indexOf('/') + 1);

        main.bold(repoName);
    }

    private static void formatDelete(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        DeletePayload payload = (DeletePayload) event.getPayload();
        main.append(" deleted ");
        main.append(payload.getRefType());
        main.append(' ');
        main.append(payload.getRef());
        main.append(" at ");

        main.bold(event.getRepo().getName());
    }

    private static void formatFollow(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" started following ");
        main.append(((FollowPayload) event.getPayload()).getTarget().getLogin());
    }

    private static void formatFork(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" forked repository ");
        main.bold(event.getRepo().getName());
    }

    private static void formatGist(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        GistPayload payload = (GistPayload) event.getPayload();

        main.append(' ');
        String action = payload.getAction();
        if ("create".equals(action))
            main.append("created");
        else if ("update".equals(action))
            main.append("updated");
        else
            main.append(action);
        main.append(" Gist ");
        main.append(payload.getGist().getId());
    }

    private static void formatWiki(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" updated the wiki in ");
        main.bold(event.getRepo().getName());
    }

    private static void formatIssueComment(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        main.append(" commented on ");

        IssueCommentPayload payload = (IssueCommentPayload) event.getPayload();

        Issue issue = payload.getIssue();
        String number;
        if (issue.getPullRequest() == null || issue.getPullRequest().getHtmlUrl() == null)
            number = "issue " + issue.getNumber();
        else
            number = "pull request " + issue.getNumber();
        main.bold(number);

        main.append(" on ");

        main.bold(event.getRepo().getName());

        appendComment(details, payload.getComment());
    }

    private static void formatIssues(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        IssuesPayload payload = (IssuesPayload) event.getPayload();
        String action = payload.getAction();
        Issue issue = payload.getIssue();
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("issue " + issue.getNumber());
        main.append(" on ");

        main.bold(event.getRepo().getName());

        appendText(details, issue.getTitle());
    }

    private static void formatAddMember(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" was added as a collaborator to ");
        main.bold(event.getRepo().getName());
    }

    private static void formatPublic(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" open sourced repository ");
        main.bold(event.getRepo().getName());
    }

    private static void formatWatch(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" started watching ");
        main.bold(event.getRepo().getName());
    }

    private static void formatReviewComment(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());
        main.append(" commented on ");
        main.bold(event.getRepo().getName());

        PullRequestReviewCommentPayload payload = (PullRequestReviewCommentPayload) event.getPayload();
        appendCommitComment(details, payload.getComment());
    }

    private static void formatPullRequest(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        PullRequestPayload payload = (PullRequestPayload) event.getPayload();
        String action = payload.getAction();
        if ("synchronize".equals(action))
            action = "updated";
        main.append(' ');
        main.append(action);
        main.append(' ');
        main.bold("pull request " + payload.getNumber());
        main.append(" on ");

        main.bold(event.getRepo().getName());
    }

    private static void formatPush(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        main.append(" pushed to ");
        PushPayload payload = (PushPayload) event.getPayload();
        String ref = payload.getRef();
        if (ref.startsWith("refs/heads/"))
            ref = ref.substring(11);
        main.bold(ref);
        main.append(" at ");

        main.bold(event.getRepo().getName());
    }

    private static void formatTeamAdd(Event event, StyledText main, StyledText details) {
        main.bold(event.getActor().getLogin());

        TeamAddPayload payload = (TeamAddPayload) event.getPayload();

        String value;
        User user = payload.getUser();
        if (user != null)
            value = user.getLogin();
        else
            value = payload.getRepo().getName();
        main.append(" added ");
        main.append(value);

        main.append(" to team");

        Team team = payload.getTeam();
        String teamName = team != null ? team.getName() : null;
        if (teamName != null)
            main.append(' ').append(teamName);
    }

    private final AvatarLoader avatars;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public NewsListAdapter(LayoutInflater inflater, Event[] elements, AvatarLoader avatars) {
        super(layout.news_item, inflater, elements);

        this.avatars = avatars;
    }

    /**
     * Create list adapter
     *
     *
     * @param inflater
     * @param avatars
     */
    public NewsListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    public long getItemId(final int position) {
        final String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.getItemId(position);
    }

    @Override
    protected void update(final int position, final NewsItemView view, final Event event) {
        avatars.bind(view.avatarView, event.getActor());

        StyledText main = new StyledText();
        StyledText details = new StyledText();
        char icon = ' ';

        String type = event.getType();
        if (TYPE_COMMIT_COMMENT.equals(type)) {
            icon = ICON_COMMENT;
            formatCommitComment(event, main, details);
        } else if (TYPE_CREATE.equals(type)) {
            icon = ICON_CREATE;
            formatCreate(event, main, details);
        } else if (TYPE_DELETE.equals(type)) {
            icon = ICON_DELETE;
            formatDelete(event, main, details);
        } else if (TYPE_DOWNLOAD.equals(type)) {
            icon = ICON_UPLOAD;
            formatDownload(event, main, details);
        } else if (TYPE_FOLLOW.equals(type)) {
            icon = ICON_FOLLOW;
            formatFollow(event, main, details);
        } else if (TYPE_FORK.equals(type)) {
            icon = ICON_FORK;
            formatFork(event, main, details);
        } else if (TYPE_GIST.equals(type)) {
            icon = ICON_GIST;
            formatGist(event, main, details);
        } else if (TYPE_GOLLUM.equals(type)) {
            icon = ICON_WIKI;
            formatWiki(event, main, details);
        } else if (TYPE_ISSUE_COMMENT.equals(type)) {
            icon = ICON_ISSUE_COMMENT;
            formatIssueComment(event, main, details);
        } else if (TYPE_ISSUES.equals(type)) {
            String action = ((IssuesPayload) event.getPayload()).getAction();
            if ("opened".equals(action))
                icon = ICON_ISSUE_OPEN;
            else if ("reopened".equals(action))
                icon = ICON_ISSUE_REOPEN;
            else if ("closed".equals(action))
                icon = ICON_ISSUE_CLOSE;
            formatIssues(event, main, details);
        } else if (TYPE_MEMBER.equals(type)) {
            icon = ICON_ADD_MEMBER;
            formatAddMember(event, main, details);
        } else if (TYPE_PUBLIC.equals(type))
            formatPublic(event, main, details);
        else if (TYPE_PULL_REQUEST.equals(type)) {
            icon = ICON_PULL_REQUEST;
            formatPullRequest(event, main, details);
        } else if (TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type)) {
            icon = ICON_COMMENT;
            formatReviewComment(event, main, details);
        } else if (TYPE_PUSH.equals(type)) {
            icon = ICON_PUSH;
            formatPush(event, main, details);
        } else if (TYPE_TEAM_ADD.equals(type)) {
            icon = ICON_ADD_MEMBER;
            formatTeamAdd(event, main, details);
        } else if (TYPE_WATCH.equals(type)) {
            icon = ICON_WATCH;
            formatWatch(event, main, details);
        }

        if (icon != ' ') {
            view.iconText.setText(Character.toString(icon));
            view.iconText.setVisibility(VISIBLE);
        } else
            view.iconText.setVisibility(GONE);

        view.eventText.setText(main);

        if (!TextUtils.isEmpty(details)) {
            view.detailsText.setText(details);
            view.detailsText.setVisibility(VISIBLE);
        } else
            view.detailsText.setVisibility(GONE);

        view.dateText.setText(TimeUtils.getRelativeTime(event.getCreatedAt()));
    }

    @Override
    protected NewsItemView createView(final View view) {
        return new NewsItemView(view);
    }
}
