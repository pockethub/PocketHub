package com.github.mobile.android.ui.user;

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
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.Time;
import com.github.mobile.android.util.TypefaceHelper;
import com.madgag.android.listviews.ViewHolder;

import java.text.MessageFormat;

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
 * View holder for a rendered news event
 */
public class NewsEventViewHolder implements ViewHolder<Event> {

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

    private final AvatarHelper avatarHelper;

    private final ImageView avatarView;

    private final TextView eventText;

    private final TextView iconText;

    private final TextView dateText;

    /**
     * Create view holder
     *
     * @param view
     * @param avatarHelper
     */
    public NewsEventViewHolder(final View view, final AvatarHelper avatarHelper) {
        this.avatarHelper = avatarHelper;
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        eventText = (TextView) view.findViewById(id.tv_event);
        iconText = (TextView) view.findViewById(id.tv_event_icon);
        TypefaceHelper.setOctocons(iconText);
        dateText = (TextView) view.findViewById(id.tv_event_date);
    }

    public void updateViewFor(Event event) {
        avatarHelper.bind(avatarView, event.getActor());

        String relativeTime = Time.relativeTimeFor(event.getCreatedAt()).toString();
        String actor = "<b>" + event.getActor().getLogin() + "</b>";
        String repoName = "<b>" + event.getRepo().getName() + "</b>";
        String type = event.getType();
        String text = null;
        String icon = null;

        if (TYPE_COMMIT_COMMENT.equals(type)) {
            icon = "\uf243";
            text = MessageFormat.format("{0} commented on commit on {1}", actor, repoName);
        } else if (TYPE_CREATE.equals(type)) {
            icon = "\uf203";
            CreatePayload payload = (CreatePayload) event.getPayload();
            String refType = payload.getRefType();
            String created;
            if (!"repository".equals(refType))
                created = payload.getRef() + " at " + repoName;
            else
                created = repoName.substring(repoName.indexOf('/') + 1);

            text = MessageFormat.format("{0} created {1} {2}", actor, refType, created);
        } else if (TYPE_DELETE.equals(type)) {
            icon = "\uf204";
            DeletePayload payload = (DeletePayload) event.getPayload();
            String refType = payload.getRefType();
            text = MessageFormat.format("{0} deleted {1} {2} at {3}", actor, refType, payload.getRef(), repoName);
        } else if (TYPE_DOWNLOAD.equals(type)) {
            icon = "\uf212";
            text = MessageFormat.format("{0} uploaded a file to {1}", actor, repoName);
        } else if (TYPE_FOLLOW.equals(type)) {
            icon = "\uf228";
            text = MessageFormat.format("{0} started following {1}", actor, ((FollowPayload) event.getPayload())
                    .getTarget().getLogin());
        } else if (TYPE_FORK.equals(type)) {
            icon = "\uf202";
            text = MessageFormat.format("{0} forked repository {1}", actor, repoName);
        } else if (TYPE_GIST.equals(type)) {
            icon = "\uf214";
            GistPayload payload = (GistPayload) event.getPayload();
            String action;
            if ("create".equals(payload.getAction()))
                action = "created";
            else if ("update".equals(payload.getAction()))
                action = "updated";
            else
                action = payload.getAction();
            text = MessageFormat.format("{0} {1} Gist {2}", actor, action, payload.getGist().getId());
        } else if (TYPE_GOLLUM.equals(type)) {
            icon = "\uf207";
            text = MessageFormat.format("{0} updated the wiki in {1}", actor, repoName);
        } else if (TYPE_ISSUE_COMMENT.equals(type)) {
            icon = "\uf241";
            Issue issue = ((IssueCommentPayload) event.getPayload()).getIssue();
            text = MessageFormat.format("{0} commented on <b>issue {1}</b> on {2}", actor,
                    Integer.toString(issue.getNumber()), repoName);
        } else if (TYPE_ISSUES.equals(type)) {
            IssuesPayload payload = (IssuesPayload) event.getPayload();
            String action = payload.getAction();
            if ("opened".equals(action))
                icon = "\uf238";
            else if ("reopened".equals(action))
                icon = "\uf239";
            else if ("closed".equals(action))
                icon = "\uf240";
            text = MessageFormat.format("{0} {1} <b>issue {2}</b> on {3}", actor, action,
                    Integer.toString(payload.getIssue().getNumber()), repoName);
        } else if (TYPE_MEMBER.equals(type)) {
            icon = "\uf226";
            text = MessageFormat.format("{0} was added as a collaborator to {1}", actor, repoName);
        } else if (TYPE_PUBLIC.equals(type))
            text = MessageFormat.format("{0} open sourced repository {1}", actor, repoName);
        else if (TYPE_PULL_REQUEST.equals(type)) {
            icon = "\uf234";
            PullRequestPayload payload = (PullRequestPayload) event.getPayload();
            String action = payload.getAction();
            if ("synchronize".equals(action))
                action = "updated";
            text = MessageFormat.format("{0} {1} <b>pull request {2}</b> on {3}", actor, action,
                    Integer.toString(payload.getPullRequest().getNumber()), repoName);
        } else if (TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type)) {
            icon = "\uf243";
            text = MessageFormat.format("{0} commented on {1}", actor, repoName);
        } else if (TYPE_PUSH.equals(type)) {
            icon = "\uf205";
            PushPayload payload = (PushPayload) event.getPayload();
            String ref = payload.getRef();
            if (ref.startsWith("refs/heads/"))
                ref = ref.substring(11);
            text = MessageFormat.format("{0} pushed to <b>{1}</b> at {2}", actor, ref, repoName);
        } else if (TYPE_TEAM_ADD.equals(type)) {
            icon = "\uf226";
            TeamAddPayload payload = (TeamAddPayload) event.getPayload();
            Team team = payload.getTeam();
            String teamName;
            if (team != null)
                teamName = " " + team.getName();
            else
                teamName = "";
            String value;
            User user = payload.getUser();
            if (user != null)
                value = user.getLogin();
            else
                value = payload.getRepo().getName();
            text = MessageFormat.format("{0} added {1} to team{2}", actor, value, teamName);
        } else if (TYPE_WATCH.equals(type)) {
            icon = "\uf229";
            text = MessageFormat.format("{0} started watching {1}", actor, repoName);
        }

        iconText.setText(icon);
        eventText.setText(Html.fromHtml(text));
        dateText.setText(relativeTime);
    }
}
