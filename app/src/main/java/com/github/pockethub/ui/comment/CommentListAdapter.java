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
package com.github.pockethub.ui.comment;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.issue.IssueEvent;
import com.alorma.github.sdk.bean.issue.IssueStoryComment;
import com.alorma.github.sdk.bean.issue.IssueStoryEvent;
import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.HttpImageGetter;
import com.github.pockethub.util.TimeUtils;
import com.github.pockethub.util.TypefaceUtils;

import java.util.Collection;

/**
 * Adapter for a list of {@link GithubComment} objects
 */
public class CommentListAdapter extends MultiTypeAdapter {

    private final AvatarLoader avatars;

    private final HttpImageGetter imageGetter;

    /**
     * Callback listener to be invoked when user tries to edit a comment.
     */
    private final EditCommentListener editCommentListener;

    /**
     * Callback listener to be invoked when user tries to edit a comment.
     */
    private final DeleteCommentListener deleteCommentListener;

    private final boolean isOwner;

    private final String userName;

    private Context context;

    private Issue issue;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(LayoutInflater inflater, GithubComment[] elements,
            AvatarLoader avatars, HttpImageGetter imageGetter, Issue issue) {
        this(inflater, elements, avatars, imageGetter, null, null, null, false, issue);
        this.context = inflater.getContext();
    }

    /**
     * Create list adapter
     *
     * @param inflater
     * @param avatars
     * @param imageGetter
     */
    public CommentListAdapter(LayoutInflater inflater, AvatarLoader avatars,
            HttpImageGetter imageGetter, Issue issue) {
        this(inflater, null, avatars, imageGetter, issue);
        this.context = inflater.getContext();
    }

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     * @param imageGetter
     * @param userName
     * @param isOwner
     */
    public CommentListAdapter(LayoutInflater inflater, GithubComment[] elements,
            AvatarLoader avatars, HttpImageGetter imageGetter,
            EditCommentListener editCommentListener, DeleteCommentListener deleteCommentListener,
            String userName, boolean isOwner, Issue issue) {
        super(inflater);

        this.issue = issue;
        this.userName = userName;
        this.isOwner = isOwner;
        this.context = inflater.getContext();
        this.avatars = avatars;
        this.imageGetter = imageGetter;
        this.editCommentListener = editCommentListener;
        this.deleteCommentListener = deleteCommentListener;
        setItems(elements);
    }

    @Override
    protected void update(int position, Object obj, int type) {
        if(type == 0)
            updateComment((GithubComment) obj);
        else
            updateEvent((IssueEvent) obj);
    }

    protected void updateEvent(final IssueEvent event) {
        TypefaceUtils.setOcticons(textView(0));
        String message = String.format("<b>%s</b> %s", event.actor.login, event.event);
        avatars.bind(imageView(2), event.actor);

        String eventString = event.event;

        switch (eventString) {
        case "assigned":
        case "unassigned":
            setText(0, TypefaceUtils.ICON_PERSON);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.text_description));
            break;
        case "labeled":
        case "unlabeled":
            setText(0, TypefaceUtils.ICON_TAG);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.text_description));
            break;
        case "referenced":
            setText(0, TypefaceUtils.ICON_BOOKMARK);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.text_description));
            break;
        case "milestoned":
        case "demilestoned":
            setText(0, TypefaceUtils.ICON_MILESTONE);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.text_description));
            break;
        case "closed":
            setText(0, TypefaceUtils.ICON_ISSUE_CLOSE);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.issue_event_closed));
            break;
        case "reopened":
            setText(0, TypefaceUtils.ICON_ISSUE_REOPEN);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.issue_event_reopened));
            break;
        case "renamed":
            setText(0, TypefaceUtils.ICON_EDIT);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.text_description));
            break;
        case "merged":
            message += String.format(" commit <b>%s</b> into <tt>%s</tt> from <tt>%s</tt>", event.commit_id.substring(0, 6), issue.pullRequest.base.ref,
                issue.pullRequest.head.ref);
            setText(0, TypefaceUtils.ICON_MERGE);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.issue_event_merged));
            break;
        case "locked":
            setText(0, TypefaceUtils.ICON_LOCK);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.issue_event_lock));
            break;
        case "unlocked":
            setText(0, TypefaceUtils.ICON_KEY);
            textView(0).setTextColor(
                    context.getResources().getColor(R.color.issue_event_lock));
            break;
        }

        message += " " + TimeUtils.getRelativeTime(event.created_at);
        setText(1, Html.fromHtml(message));
    }

    protected void updateComment(final GithubComment comment) {
        imageGetter.bind(textView(0), comment.body, comment.id);
        avatars.bind(imageView(3), comment.user);

        setText(1, comment.user.login);
        setText(2, TimeUtils.getRelativeTime(comment.updated_at));

        final boolean canEdit = (isOwner || comment.user.login.equals(userName))
            && editCommentListener != null;

        final boolean canDelete = (isOwner || comment.user.login.equals(userName))
            && deleteCommentListener != null;

        final ImageView ivMore = view(4);

        if(!canEdit && !canDelete)
            ivMore.setVisibility(View.INVISIBLE);
        else
            ivMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMorePopup(ivMore, comment, canEdit, canDelete);
            }
        });
    }

    private void showMorePopup(View v, final GithubComment comment, final boolean canEdit, final boolean canDelete ) {
        PopupMenu menu = new PopupMenu(context, v);
        menu.inflate(R.menu.comment_popup);

        menu.getMenu().findItem(R.id.m_edit).setEnabled(canEdit);
        menu.getMenu().findItem(R.id.m_delete).setEnabled(canDelete);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.m_edit:
                        if (editCommentListener != null) {
                            editCommentListener.onEditComment(comment);
                        }
                        break;
                    case R.id.m_delete:
                        if (deleteCommentListener != null) {
                            deleteCommentListener.onDeleteComment(comment);
                        }
                        break;
                }
                return false;
            }
        });

        menu.show();
    }

    public MultiTypeAdapter setItems(Collection<?> items) {
        if (items == null || items.isEmpty())
            return this;
        return setItems(items.toArray());
    }

    public MultiTypeAdapter setItems(final Object[] items) {
        if (items == null || items.length == 0)
            return this;

        this.clear();

        for (Object item : items) {
            if(item instanceof GithubComment)
                this.addItem(0, item);
            else if(item instanceof GithubEvent)
                this.addItem(1, item);
            else if(item instanceof IssueStoryComment)
                this.addItem(0, ((IssueStoryComment) item).comment);
            else if(item instanceof IssueStoryEvent)
                this.addItem(1, ((IssueStoryEvent) item).event);
        }

        notifyDataSetChanged();
        return this;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    @Override
    protected View initialize(int type, View view) {
        view = super.initialize(type, view);

        textView(view, 0).setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected int getChildLayoutId(int type) {
        if(type == 0)
            return R.layout.comment_item;
        else
            return R.layout.comment_event_item;
    }

    @Override
    protected int[] getChildViewIds(int type) {
        if(type == 0)
            return new int[] { R.id.tv_comment_body, R.id.tv_comment_author,
                    R.id.tv_comment_date, R.id.iv_avatar, R.id.iv_more };
        else
            return new int[]{R.id.tv_event_icon, R.id.tv_event, R.id.iv_avatar};
    }
}
