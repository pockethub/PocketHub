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
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.TimeUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.event.CreatePayload;
import org.eclipse.egit.github.core.event.DeletePayload;
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
public class NewsEventListAdapter extends ItemListAdapter<Event, NewsEventItemView> {

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

    private static CharSequence formatCommitComment(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" commented on commit on ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatDownload(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" uploaded a file to ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatCreate(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" created ");
        CreatePayload payload = (CreatePayload) event.getPayload();
        String refType = payload.getRefType();
        builder.append(refType);
        builder.append(' ');
        String repoName = event.getRepo().getName();
        if (!"repository".equals(refType)) {
            builder.append(payload.getRef());
            builder.append(" at ");
        } else
            repoName = repoName.substring(repoName.indexOf('/') + 1);

        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatDelete(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        DeletePayload payload = (DeletePayload) event.getPayload();
        builder.append(" deleted ");
        builder.append(payload.getRefType());
        builder.append(' ');
        builder.append(payload.getRef());
        builder.append(" at ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatFollow(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" started following ");
        builder.append(((FollowPayload) event.getPayload()).getTarget().getLogin());

        return builder;
    }

    private static CharSequence formatFork(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" forked repository ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatGist(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        GistPayload payload = (GistPayload) event.getPayload();

        builder.append(' ');
        String action = payload.getAction();
        if ("create".equals(action))
            builder.append("created");
        else if ("update".equals(action))
            builder.append("updated");
        else
            builder.append(action);
        builder.append(" Gist ");
        builder.append(payload.getGist().getId());

        return builder;
    }

    private static CharSequence formatWiki(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" updated the wiki in ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatIssueComment(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" commented on ");

        Issue issue = ((IssueCommentPayload) event.getPayload()).getIssue();
        String number;
        if (issue.getPullRequest() == null || issue.getPullRequest().getHtmlUrl() == null)
            number = "issue " + issue.getNumber();
        else
            number = "pull request " + issue.getNumber();
        builder.append(number);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - number.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" on ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatIssues(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        IssuesPayload payload = (IssuesPayload) event.getPayload();
        String action = payload.getAction();
        builder.append(' ');
        builder.append(action);
        builder.append(' ');
        String issueNumber = "issue " + payload.getIssue().getNumber();
        builder.append(issueNumber);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - issueNumber.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" on ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatAddMember(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" was added as a collaborator to ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatPublic(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" open sourced repository ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatWatch(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" started watching ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatReviewComment(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" commented on ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatPullRequest(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        String actor = event.getActor().getLogin();
        builder.append(actor);
        builder.setSpan(new StyleSpan(BOLD), 0, actor.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        PullRequestPayload payload = (PullRequestPayload) event.getPayload();
        String action = payload.getAction();
        if ("synchronize".equals(action))
            action = "updated";
        builder.append(' ');
        builder.append(action);
        builder.append(' ');
        String issueNumber = "pull request " + payload.getNumber();
        builder.append(issueNumber);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - issueNumber.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" on ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatPush(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        String actor = event.getActor().getLogin();
        builder.append(actor);
        builder.setSpan(new StyleSpan(BOLD), 0, actor.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.append(" pushed to ");
        PushPayload payload = (PushPayload) event.getPayload();
        String ref = payload.getRef();
        if (ref.startsWith("refs/heads/"))
            ref = ref.substring(11);
        builder.append(ref);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - ref.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" at ");

        String repoName = event.getRepo().getName();
        builder.append(repoName);
        builder.setSpan(new StyleSpan(BOLD), builder.length() - repoName.length(), builder.length(),
                SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private static CharSequence formatTeamAdd(Event event) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        builder.append(event.getActor().getLogin());
        builder.setSpan(new StyleSpan(BOLD), 0, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

        TeamAddPayload payload = (TeamAddPayload) event.getPayload();

        String value;
        User user = payload.getUser();
        if (user != null)
            value = user.getLogin();
        else
            value = payload.getRepo().getName();
        builder.append(" added ");
        builder.append(value);

        builder.append(" to team");

        Team team = payload.getTeam();
        String teamName = team != null ? team.getName() : null;
        if (teamName != null)
            builder.append(' ').append(teamName);

        return builder;
    }

    private final AvatarUtils avatars;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public NewsEventListAdapter(LayoutInflater inflater, Event[] elements, AvatarUtils avatars) {
        super(layout.event_item, inflater, elements);

        this.avatars = avatars;
    }

    /**
     * Create list adapter
     *
     * @param inflater
     * @param avatars
     */
    public NewsEventListAdapter(LayoutInflater inflater, AvatarUtils avatars) {
        this(inflater, null, avatars);
    }

    @Override
    public long getItemId(int position) {
        String id = getItem(position).getId();
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.hashCode();
    }

    @Override
    protected void update(final NewsEventItemView view, final Event event) {
        avatars.bind(view.avatarView, event.getActor());

        CharSequence text = null;
        char icon = ' ';

        String type = event.getType();
        if (TYPE_COMMIT_COMMENT.equals(type)) {
            icon = ICON_COMMENT;
            text = formatCommitComment(event);
        } else if (TYPE_CREATE.equals(type)) {
            icon = ICON_CREATE;
            text = formatCreate(event);
        } else if (TYPE_DELETE.equals(type)) {
            icon = ICON_DELETE;
            text = formatDelete(event);
        } else if (TYPE_DOWNLOAD.equals(type)) {
            icon = ICON_UPLOAD;
            text = formatDownload(event);
        } else if (TYPE_FOLLOW.equals(type)) {
            icon = ICON_FOLLOW;
            text = formatFollow(event);
        } else if (TYPE_FORK.equals(type)) {
            icon = ICON_FORK;
            text = formatFork(event);
        } else if (TYPE_GIST.equals(type)) {
            icon = ICON_GIST;
            text = formatGist(event);
        } else if (TYPE_GOLLUM.equals(type)) {
            icon = ICON_WIKI;
            text = formatWiki(event);
        } else if (TYPE_ISSUE_COMMENT.equals(type)) {
            icon = ICON_ISSUE_COMMENT;
            text = formatIssueComment(event);
        } else if (TYPE_ISSUES.equals(type)) {
            String action = ((IssuesPayload) event.getPayload()).getAction();
            if ("opened".equals(action))
                icon = ICON_ISSUE_OPEN;
            else if ("reopened".equals(action))
                icon = ICON_ISSUE_REOPEN;
            else if ("closed".equals(action))
                icon = ICON_ISSUE_CLOSE;
            text = formatIssues(event);
        } else if (TYPE_MEMBER.equals(type)) {
            icon = ICON_ADD_MEMBER;
            text = formatAddMember(event);
        } else if (TYPE_PUBLIC.equals(type))
            text = formatPublic(event);
        else if (TYPE_PULL_REQUEST.equals(type)) {
            icon = ICON_PULL_REQUEST;
            text = formatPullRequest(event);
        } else if (TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type)) {
            icon = ICON_COMMENT;
            text = formatReviewComment(event);
        } else if (TYPE_PUSH.equals(type)) {
            icon = ICON_PUSH;
            text = formatPush(event);
        } else if (TYPE_TEAM_ADD.equals(type)) {
            icon = ICON_ADD_MEMBER;
            text = formatTeamAdd(event);
        } else if (TYPE_WATCH.equals(type)) {
            icon = ICON_WATCH;
            text = formatWatch(event);
        }

        view.iconText.setText(icon != ' ' ? Character.toString(icon) : null);
        view.eventText.setText(text);
        view.dateText.setText(TimeUtils.getRelativeTime(event.getCreatedAt()).toString());
    }

    @Override
    protected NewsEventItemView createView(final View view) {
        return new NewsEventItemView(view);
    }
}
