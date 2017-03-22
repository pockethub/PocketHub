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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.GitHubEventType;
import com.meisolsson.githubsdk.model.payload.CreatePayload;
import com.meisolsson.githubsdk.model.payload.GistPayload;
import com.meisolsson.githubsdk.model.payload.IssueCommentPayload;
import com.meisolsson.githubsdk.model.payload.IssuesPayload;

import static com.meisolsson.githubsdk.model.GitHubEventType.*;

/**
 * Adapter for a list of news events
 */
public class NewsListAdapter extends SingleTypeAdapter<GitHubEvent> {

    private final IconAndViewTextManager iconAndViewTextManager = new IconAndViewTextManager(this);

    /**
     * Can the given event be rendered by this view holder?
     *
     * @param event
     * @return true if renderable, false otherwise
     */
    public static boolean isValid(final GitHubEvent event) {
        if (event == null || event.payload() == null) {
            return false;
        }

        final GitHubEventType type = event.type();

        return CommitCommentEvent.equals(type) //
                || (CreateEvent.equals(type) //
                && ((CreatePayload) event.payload()).refType() != null) //
                || DeleteEvent.equals(type) //
                || DownloadEvent.equals(type) //
                || FollowEvent.equals(type) //
                || ForkEvent.equals(type) //
                || (GistEvent.equals(type)
                && ((GistPayload) event.payload()).gist() != null)
                || GollumEvent.equals(type) //
                || (IssueCommentEvent.equals(type) //
                && ((IssueCommentPayload) event.payload()).issue() != null) //
                || (IssuesEvent.equals(type) //
                && ((IssuesPayload) event.payload()).issue() != null) //
                || MemberEvent.equals(type) //
                || PublicEvent.equals(type) //
                || PullRequestEvent.equals(type) //
                || PullRequestReviewCommentEvent.equals(type) //
                || PushEvent.equals(type) //
                || TeamAddEvent.equals(type) //
                || WatchEvent.equals(type);
    }

    private final AvatarLoader avatars;

    /**
     * Create list adapter
     *
     * @param inflater
     * @param elements
     * @param avatars
     */
    public NewsListAdapter(LayoutInflater inflater, GitHubEvent[] elements,
            AvatarLoader avatars) {
        super(inflater, R.layout.news_item);

        this.avatars = avatars;
        setItems(elements);
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
        final String id = String.valueOf(getItem(position).id());
        return !TextUtils.isEmpty(id) ? id.hashCode() : super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.iv_avatar, R.id.tv_event, R.id.tv_event_details,
                R.id.tv_event_icon, R.id.tv_event_date };
    }

    @Override
    protected void update(int position, GitHubEvent event) {

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
