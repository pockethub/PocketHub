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

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
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
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TimeUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Download;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
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

    private static final int MAX_TEXT = 80;

    private static void appendComment(final SpannableStringBuilder details, final Comment comment) {
        if (comment == null)
            return;
        appendText(details, comment.getBody());
    }

    private static void appendText(final SpannableStringBuilder details, String text) {
        if (text == null)
            return;
        text = text.trim();
        if (text.length() == 0)
            return;

        if (text.length() < MAX_TEXT)
            details.append(text);
        else
            details.append(text, 0, MAX_TEXT).append('\u2026');
    }

    private static void formatCommitComment(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" commented on commit on ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatDownload(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" uploaded a file to ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        DownloadPayload payload = (DownloadPayload) event.getPayload();
        Download download = payload.getDownload();
        if (download != null)
            appendText(details, download.getName());
    }

    private static void formatCreate(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

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

        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatDelete(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        DeletePayload payload = (DeletePayload) event.getPayload();
        main.append(" deleted ");
        main.append(payload.getRefType());
        main.append(' ');
        main.append(payload.getRef());
        main.append(" at ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatFollow(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" started following ");
        main.append(((FollowPayload) event.getPayload()).getTarget().getLogin());
    }

    private static void formatFork(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" forked repository ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatGist(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

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

    private static void formatWiki(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" updated the wiki in ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatIssueComment(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" commented on ");

        IssueCommentPayload payload = (IssueCommentPayload) event.getPayload();

        Issue issue = payload.getIssue();
        String number;
        if (issue.getPullRequest() == null || issue.getPullRequest().getHtmlUrl() == null)
            number = "issue " + issue.getNumber();
        else
            number = "pull request " + issue.getNumber();
        main.append(number);
        main.setSpan(new StyleSpan(BOLD), main.length() - number.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" on ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        appendComment(details, payload.getComment());
    }

    private static void formatIssues(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        IssuesPayload payload = (IssuesPayload) event.getPayload();
        String action = payload.getAction();
        Issue issue = payload.getIssue();
        main.append(' ');
        main.append(action);
        main.append(' ');
        String issueNumber = "issue " + issue.getNumber();
        main.append(issueNumber);
        main.setSpan(new StyleSpan(BOLD), main.length() - issueNumber.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        main.append(" on ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        appendText(details, issue.getTitle());
    }

    private static void formatAddMember(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" was added as a collaborator to ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatPublic(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" open sourced repository ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatWatch(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" started watching ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatReviewComment(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" commented on ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatPullRequest(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        String actor = event.getActor().getLogin();
        main.append(actor);
        main.setSpan(new StyleSpan(BOLD), 0, actor.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        PullRequestPayload payload = (PullRequestPayload) event.getPayload();
        String action = payload.getAction();
        if ("synchronize".equals(action))
            action = "updated";
        main.append(' ');
        main.append(action);
        main.append(' ');
        String prNumber = "pull request " + payload.getNumber();
        main.append(prNumber);
        main.setSpan(new StyleSpan(BOLD), main.length() - prNumber.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        main.append(" on ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatPush(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        String actor = event.getActor().getLogin();
        main.append(actor);
        main.setSpan(new StyleSpan(BOLD), 0, actor.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        main.append(" pushed to ");
        PushPayload payload = (PushPayload) event.getPayload();
        String ref = payload.getRef();
        if (ref.startsWith("refs/heads/"))
            ref = ref.substring(11);
        main.append(ref);
        main.setSpan(new StyleSpan(BOLD), main.length() - ref.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        main.append(" at ");

        String repoName = event.getRepo().getName();
        main.append(repoName);
        main.setSpan(new StyleSpan(BOLD), main.length() - repoName.length(), main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void formatTeamAdd(Event event, SpannableStringBuilder main, SpannableStringBuilder details) {
        main.append(event.getActor().getLogin());
        main.setSpan(new StyleSpan(BOLD), 0, main.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

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
     * @param inflater
     * @param avatars
     */
    public NewsListAdapter(LayoutInflater inflater, AvatarLoader avatars) {
        this(inflater, null, avatars);
    }

    @Override
    public long getItemId(int position) {
        String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.hashCode();
    }

    @Override
    protected void update(final NewsItemView view, final Event event) {
        avatars.bind(view.avatarView, event.getActor());

        SpannableStringBuilder main = new SpannableStringBuilder();
        SpannableStringBuilder details = new SpannableStringBuilder();
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

        view.iconText.setText(icon != ' ' ? Character.toString(icon) : null);
        view.eventText.setText(main);
        if (details.length() > 0) {
            view.detailsText.setVisibility(VISIBLE);
            view.detailsText.setText(details);
        } else
            view.detailsText.setVisibility(GONE);
        view.dateText.setText(TimeUtils.getRelativeTime(event.getCreatedAt()).toString());
    }

    @Override
    protected NewsItemView createView(final View view) {
        return new NewsItemView(view);
    }
}
