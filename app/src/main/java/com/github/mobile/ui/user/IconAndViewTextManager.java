package com.github.mobile.ui.user;

import android.text.TextUtils;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.TypefaceUtils;

import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.IssuesPayload;

public class IconAndViewTextManager {
    private final NewsListAdapter newsListAdapter;

    public IconAndViewTextManager(NewsListAdapter newsListAdapter) {
        this.newsListAdapter = newsListAdapter;
    }

    protected void update(int position, Event event) {
        newsListAdapter.getAvatars().bind(newsListAdapter.imageViewAgent(0), event.getActor());

        StyledText main = new StyledText();
        StyledText details = new StyledText();
        String icon = null;

        String type = event.getType();
        if (Event.TYPE_COMMIT_COMMENT.equals(type)) {
            icon = TypefaceUtils.ICON_COMMENT;
            NewsListAdapter.formatCommitComment(event, main, details);
        } else if (Event.TYPE_CREATE.equals(type)) {
            icon = TypefaceUtils.ICON_CREATE;
            NewsListAdapter.formatCreate(event, main, details);
        } else if (Event.TYPE_DELETE.equals(type)) {
            icon = TypefaceUtils.ICON_DELETE;
            NewsListAdapter.formatDelete(event, main, details);
        } else if (Event.TYPE_DOWNLOAD.equals(type)) {
            icon = TypefaceUtils.ICON_UPLOAD;
            NewsListAdapter.formatDownload(event, main, details);
        } else if (Event.TYPE_FOLLOW.equals(type)) {
            icon = TypefaceUtils.ICON_FOLLOW;
            NewsListAdapter.formatFollow(event, main, details);
        } else if (Event.TYPE_FORK.equals(type)) {
            icon = TypefaceUtils.ICON_FORK;
            NewsListAdapter.formatFork(event, main, details);
        } else if (Event.TYPE_GIST.equals(type)) {
            icon = TypefaceUtils.ICON_GIST;
            NewsListAdapter.formatGist(event, main, details);
        } else if (Event.TYPE_GOLLUM.equals(type)) {
            icon = TypefaceUtils.ICON_WIKI;
            NewsListAdapter.formatWiki(event, main, details);
        } else if (Event.TYPE_ISSUE_COMMENT.equals(type)) {
            icon = TypefaceUtils.ICON_ISSUE_COMMENT;
            NewsListAdapter.formatIssueComment(event, main, details);
        } else if (Event.TYPE_ISSUES.equals(type)) {
            String action = ((IssuesPayload) event.getPayload()).getAction();
            if ("opened".equals(action))
                icon = TypefaceUtils.ICON_ISSUE_OPEN;
            else if ("reopened".equals(action))
                icon = TypefaceUtils.ICON_ISSUE_REOPEN;
            else if ("closed".equals(action))
                icon = TypefaceUtils.ICON_ISSUE_CLOSE;
            NewsListAdapter.formatIssues(event, main, details);
        } else if (Event.TYPE_MEMBER.equals(type)) {
            icon = TypefaceUtils.ICON_ADD_MEMBER;
            NewsListAdapter.formatAddMember(event, main, details);
        } else if (Event.TYPE_PUBLIC.equals(type))
            NewsListAdapter.formatPublic(event, main, details);
        else if (Event.TYPE_PULL_REQUEST.equals(type)) {
            icon = TypefaceUtils.ICON_PULL_REQUEST;
            NewsListAdapter.formatPullRequest(event, main, details);
        } else if (Event.TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type)) {
            icon = TypefaceUtils.ICON_COMMENT;
            NewsListAdapter.formatReviewComment(event, main, details);
        } else if (Event.TYPE_PUSH.equals(type)) {
            icon = TypefaceUtils.ICON_PUSH;
            NewsListAdapter.formatPush(event, main, details);
        } else if (Event.TYPE_TEAM_ADD.equals(type)) {
            icon = TypefaceUtils.ICON_ADD_MEMBER;
            NewsListAdapter.formatTeamAdd(event, main, details);
        } else if (Event.TYPE_WATCH.equals(type)) {
            icon = TypefaceUtils.ICON_STAR;
            NewsListAdapter.formatWatch(event, main, details);
        }

        if (icon != null)
            ViewUtils.setGone(newsListAdapter.setTextAgent(3, icon), false);
        else
            newsListAdapter.setGoneAgent(3, true);

        newsListAdapter.setTextAgent(1, main);

        if (!TextUtils.isEmpty(details))
            ViewUtils.setGone(newsListAdapter.setTextAgent(2, details), false);
        else
            newsListAdapter.setGoneAgent(2, true);

        newsListAdapter.setTextAgent(4, TimeUtils.getRelativeTime(event.getCreatedAt()));
    }
}