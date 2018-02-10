package com.github.pockethub.android.ui.item.news;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.item.BaseDataItem;
import com.github.pockethub.android.ui.item.BaseViewHolder;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.TimeUtils;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.User;

import butterknife.BindView;

public class NewsItem extends BaseDataItem<GitHubEvent, NewsItem.ViewHolder> {

    /**
     * Create a instance of the {@link NewsItem} corresponding to the event type.
     *
     * @param avatars Avatar image loader
     * @param item Event item
     * @return Subclass of {@link NewsItem} corresponding to the event type
     */
    public static NewsItem createNewsItem(AvatarLoader avatars, GitHubEvent item) {
        switch (item.type()) {
            case CommitCommentEvent:
                return new CommitCommentEventItem(avatars, item);
            case CreateEvent:
                return new CreateEventItem(avatars, item);
            case DeleteEvent:
                return new DeleteEventItem(avatars, item);
            case DownloadEvent:
            case ReleaseEvent:
                return new ReleaseEventItem(avatars, item);
            case FollowEvent:
                return new FollowEventItem(avatars, item);
            case ForkEvent:
                return new ForkEventItem(avatars, item);
            case GistEvent:
                return new GistEventItem(avatars, item);
            case GollumEvent:
                return new GollumEventItem(avatars, item);
            case IssueCommentEvent:
                return new IssueCommentEventItem(avatars, item);
            case IssuesEvent:
                return new IssuesEventItem(avatars, item);
            case MemberEvent:
                return new MemberEventItem(avatars, item);
            case PublicEvent:
                return new PublicEventItem(avatars, item);
            case PullRequestEvent:
                return new PullRequestEventItem(avatars, item);
            case PullRequestReviewCommentEvent:
                return new PullRequestReviewCommentEventItem(avatars, item);
            case PushEvent:
                return new PushEventItem(avatars, item);
            case TeamAddEvent:
                return new TeamAddEventItem(avatars, item);
            case WatchEvent:
                return new WatchEventItem(avatars, item);
            default:
                Log.d("NewsItem", "Event type not allowed: " + item.type());
                return null;
        }
    }

    public NewsItem(AvatarLoader avatarLoader, GitHubEvent dataItem) {
        super(avatarLoader, dataItem, dataItem.id().hashCode());
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        getAvatarLoader().bind(viewHolder.avatar, getData().actor());
        viewHolder.date.setText(TimeUtils.getRelativeTime(getData().createdAt()));
    }

    protected StyledText boldActor(final StyledText text, final GitHubEvent event) {
        return boldUser(text, event.actor());
    }

    protected StyledText boldUser(final StyledText text, final User user) {
        if (user != null) {
            text.bold(user.login());
        }
        return text;
    }

    protected StyledText boldRepo(final StyledText text, final GitHubEvent event) {
        GitHubEvent.RepoIdentifier repo = event.repo();
        if (repo != null) {
            text.bold(repo.repoWithUserName());
        }
        return text;
    }

    protected StyledText boldRepoName(final StyledText text,
                                    final GitHubEvent event) {
        GitHubEvent.RepoIdentifier repo = event.repo();
        if (repo != null) {
            String name = repo.repoWithUserName();
            if (!TextUtils.isEmpty(name)) {
                int slash = name.indexOf('/');
                if (slash != -1 && slash + 1 < name.length()) {
                    text.bold(name.substring(slash + 1));
                }
            }
        }
        return text;
    }


    protected void appendText(final StyledText text, String toAppend) {
        if (toAppend == null) {
            return;
        }
        toAppend = toAppend.trim();
        if (toAppend.length() == 0) {
            return;
        }

        text.append(toAppend);
    }

    @Override
    public int getLayout() {
        return R.layout.news_item;
    }

    @NonNull
    @Override
    public ViewHolder createViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.iv_avatar) ImageView avatar;

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        @BindView(R.id.tv_event)
        public TextView event;

        @BindView(R.id.tv_event_details) TextView details;
        @BindView(R.id.tv_event_icon) TextView icon;
        @BindView(R.id.tv_event_date) TextView date;

        public ViewHolder(@NonNull View rootView) {
            super(rootView);
        }
    }
}
