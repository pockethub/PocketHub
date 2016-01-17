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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.events.EventType;
import com.alorma.github.sdk.bean.dto.response.events.payload.MemberEventPayload;
import com.alorma.github.sdk.bean.dto.response.events.payload.Payload;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.api.FollowEventPayload;
import com.github.pockethub.api.GistEventPayload;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TypefaceUtils;
import com.google.gson.Gson;

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

/**
 * Adapter for a list of news events
 */
public class NewsListAdapter extends SingleTypeAdapter<GithubEvent> {

    private final IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(this);

    /**
     * Can the given event be rendered by this view holder?
     *
     * @param event
     * @return true if renderable, false otherwise
     */
    public static boolean isValid(final GithubEvent event) {
        if (event == null)
            return false;

        if (event.payload == null)
            return false;

        Gson gson = new Gson();
        final String json = gson.toJson(event.payload);

        final String type = event.type.toString();
        if (TextUtils.isEmpty(type))
            return false;

        return TYPE_COMMIT_COMMENT.equals(type) //
                || (TYPE_CREATE.equals(type) //
                && (gson.fromJson(json, Payload.class)).ref_type != null) //
                || TYPE_DELETE.equals(type) //
                || TYPE_DOWNLOAD.equals(type) //
                || TYPE_FOLLOW.equals(type) //
                || TYPE_FORK.equals(type) //
                || TYPE_FORK_APPLY.equals(type) //
                || (TYPE_GIST.equals(type)
                && (gson.fromJson(json, GistEventPayload.class)).gist != null)
                || TYPE_GOLLUM.equals(type) //
                || (TYPE_ISSUE_COMMENT.equals(type) //
                && (gson.fromJson(json, Payload.class)).issue != null) //
                || (TYPE_ISSUES.equals(type) //
                && (gson.fromJson(json, Payload.class)).issue != null) //
                || TYPE_MEMBER.equals(type) //
                || TYPE_PUBLIC.equals(type) //
                || TYPE_PULL_REQUEST.equals(type) //
                || TYPE_PULL_REQUEST_REVIEW_COMMENT.equals(type) //
                || TYPE_PUSH.equals(type) //
                || TYPE_TEAM_ADD.equals(type) //
                || TYPE_WATCH.equals(type);
    }

    private final AvatarLoader avatars;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public NewsListAdapter(LayoutInflater inflater, GithubEvent[] elements,
            AvatarLoader avatars) {
        super(inflater, R.layout.news_item);

        this.avatars = avatars;
        setItems(elements);
    }

    private Class getClassFromType(EventType type) {
        switch (type){
            case WatchEvent:
                return Payload.class;
            case CreateEvent:
                return Payload.class;
            case CommitCommentEvent:
                return Payload.class;
            case DownloadEvent:
                return Payload.class;
            case FollowEvent:
                return FollowEventPayload.class;
            case ForkEvent:
                return Payload.class;
            case GistEvent:
                return GistEventPayload.class;
            case IssueCommentEvent:
                return Payload.class;
            case IssuesEvent:
                return Payload.class;
            case MemberEvent:
                return MemberEventPayload.class;
            case PublicEvent:
                return Payload.class;
            case PullRequestEvent:
                return Payload.class;
            case PullRequestReviewCommentEvent:
                return Payload.class;
            case PushEvent:
                return Payload.class;
            case TeamAddEvent:
                return Payload.class;
            case DeleteEvent:
                return Payload.class;
            case ReleaseEvent:
                return Payload.class;
            case Unhandled:
                return Payload.class;

            default:
                return Payload.class;
        }
    }

    @Override
    public void setItems(Object[] items) {
        if(items != null) {
            GithubEvent[] elements = new GithubEvent[items.length];
            Gson gson = new Gson();
            for (int i = 0; i < items.length; i++) {
                GithubEvent element = (GithubEvent) items[i];
                String json = gson.toJson(element.payload);
                element.payload = gson.fromJson(json, Payload.class);
                elements[i] = element;
            }
            super.setItems(elements);
        }else{
            super.setItems(items);
        }
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
        final String id = String.valueOf(getItem(position).id);
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.iv_avatar, R.id.tv_event, R.id.tv_event_details,
                R.id.tv_event_icon, R.id.tv_event_date };
    }

    @Override
    protected View initialize(View view) {
        view = super.initialize(view);

        TypefaceUtils.setOcticons(textView(view, 3));
        return view;
    }

    @Override
    protected void update(int position, GithubEvent event) {

        iconAndViewTextManager.update(position, event);
    }

    public AvatarLoader getAvatars() {
        return avatars;
    }

    ImageView imageViewAgent(int childViewIndex) {
        return this.imageView(childViewIndex);
    }

    TextView setTextAgent(int childViewIndex, CharSequence text) {
        return this.setText(childViewIndex, text);
    }

    View setGoneAgent(int childViewIndex, boolean gone) {
        return this.setGone(childViewIndex, gone);
    }
}
