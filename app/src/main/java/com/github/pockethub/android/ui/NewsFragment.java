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
package com.github.pockethub.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.ui.item.news.CommitCommentEventItem;
import com.github.pockethub.android.ui.item.news.CreateEventItem;
import com.github.pockethub.android.ui.item.news.DeleteEventItem;
import com.github.pockethub.android.ui.item.news.FollowEventItem;
import com.github.pockethub.android.ui.item.news.ForkEventItem;
import com.github.pockethub.android.ui.item.news.GistEventItem;
import com.github.pockethub.android.ui.item.news.GollumEventItem;
import com.github.pockethub.android.ui.item.news.IssueCommentEventItem;
import com.github.pockethub.android.ui.item.news.IssuesEventItem;
import com.github.pockethub.android.ui.item.news.MemberEventItem;
import com.github.pockethub.android.ui.item.news.NewsItem;
import com.github.pockethub.android.ui.item.news.PublicEventItem;
import com.github.pockethub.android.ui.item.news.PullRequestEventItem;
import com.github.pockethub.android.ui.item.news.PullRequestReviewCommentEventItem;
import com.github.pockethub.android.ui.item.news.PushEventItem;
import com.github.pockethub.android.ui.item.news.ReleaseEventItem;
import com.github.pockethub.android.ui.item.news.TeamAddEventItem;
import com.github.pockethub.android.ui.item.news.WatchEventItem;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Release;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.gist.GistEventMatcher;
import com.github.pockethub.android.core.issue.IssueEventMatcher;
import com.github.pockethub.android.core.repo.RepositoryEventMatcher;
import com.github.pockethub.android.core.user.UserEventMatcher;
import com.github.pockethub.android.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity;
import com.github.pockethub.android.ui.commit.CommitViewActivity;
import com.github.pockethub.android.ui.gist.GistsViewActivity;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.ui.repo.RepositoryViewActivity;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.ConvertUtils;
import com.github.pockethub.android.util.InfoUtils;
import com.meisolsson.githubsdk.model.git.GitComment;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.payload.CommitCommentPayload;
import com.meisolsson.githubsdk.model.payload.PushPayload;
import com.meisolsson.githubsdk.model.payload.ReleasePayload;
import com.xwray.groupie.Item;

import javax.inject.Inject;

import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import static com.meisolsson.githubsdk.model.GitHubEventType.CommitCommentEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.DownloadEvent;
import static com.meisolsson.githubsdk.model.GitHubEventType.PushEvent;

/**
 * Base news fragment class with utilities for subclasses to built on
 */
public abstract class NewsFragment extends PagedItemFragment<GitHubEvent> {

    @Inject
    protected AvatarLoader avatars;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_news);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (!(item instanceof NewsItem)) {
            return;
        }

        GitHubEvent event = ((NewsItem) item).getGitHubEvent();

        if (DownloadEvent.equals(event.type())) {
            openDownload(event);
            return;
        }

        if (PushEvent.equals(event.type())) {
            openPush(event);
            return;
        }

        if (CommitCommentEvent.equals(event.type())) {
            openCommitComment(event);
            return;
        }

        Issue issue = IssueEventMatcher.getIssue(event);
        if (issue != null) {
            Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
            viewIssue(issue, repo);
            return;
        }

        Gist gist = GistEventMatcher.getGist(event);
        if (gist != null) {
            startActivity(GistsViewActivity.createIntent(gist));
            return;
        }

        Repository repo = RepositoryEventMatcher.getRepository(event);
        if (repo != null) {
            viewRepository(repo);
        }

        UserPair users = UserEventMatcher.getUsers(event);
        if (users != null) {
            viewUser(users);
        }
    }

    @Override
    public boolean onItemLongClick(@NonNull Item item, @NonNull View view) {
        if (!isAdded()) {
            return false;
        }

        if (!(item instanceof NewsItem)) {
            return false;
        }


        final GitHubEvent event = ((NewsItem) item).getGitHubEvent();
        final Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        final User user = event.actor();

        if (repo != null && user != null) {
            final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                    .title(R.string.navigate_to);

            // Hacky but necessary since material dialogs has a different API
            final MaterialDialog[] dialogHolder = new MaterialDialog[1];

            View dialogView = getActivity().getLayoutInflater().inflate(
                    R.layout.nav_dialog, null);
            avatars.bind((ImageView) dialogView.findViewById(R.id.iv_user_avatar), user);
            avatars.bind((ImageView) dialogView.findViewById(R.id.iv_repo_avatar), repo.owner());
            ((TextView) dialogView.findViewById(R.id.tv_login)).setText(user.login());
            ((TextView) dialogView.findViewById(R.id.tv_repo_name)).setText(InfoUtils.createRepoId(repo));
            dialogView.findViewById(R.id.ll_user_area).setOnClickListener(v1 -> {
                dialogHolder[0].dismiss();
                viewUser(user);
            });
            dialogView.findViewById(R.id.ll_repo_area).setOnClickListener(v1 -> {
                dialogHolder[0].dismiss();
                viewRepository(repo);
            });
            builder.customView(dialogView, false);

            MaterialDialog dialog = builder.build();
            dialogHolder[0] = dialog;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            return true;
        }

        return false;
    }

    // https://developer.github.com/v3/repos/downloads/#downloads-api-is-deprecated
    private void openDownload(GitHubEvent event) {
        Release release = ((ReleasePayload) event.payload()).release();
        if (release == null) {
            return;
        }

        String url = release.htmlUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Intent intent = new Intent(ACTION_VIEW, Uri.parse(url));
        intent.addCategory(CATEGORY_BROWSABLE);
        startActivity(intent);
    }

    private void openCommitComment(GitHubEvent event) {
        Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        if (repo == null) {
            return;
        }

        if(repo.name().contains("/")) {
            String[] repoId = repo.name().split("/");
            repo = InfoUtils.createRepoFromData(repoId[0], repoId[1]);
        }

        CommitCommentPayload payload = ((CommitCommentPayload) event.payload());
        GitComment comment = payload.comment();
        if (comment == null) {
            return;
        }

        String sha = comment.commitId();
        if (!TextUtils.isEmpty(sha)) {
            startActivity(CommitViewActivity.createIntent(repo, sha));
        }
    }

    private void openPush(GitHubEvent event) {
        Repository repo = ConvertUtils.eventRepoToRepo(event.repo());
        if (repo == null) {
            return;
        }

        PushPayload payload = ((PushPayload) event.payload());
        List<GitCommit> commits = payload.commits();
        if (commits.isEmpty()) {
            return;
        }

        if (commits.size() > 1) {
            String base = payload.before();
            String head = payload.head();
            if (!TextUtils.isEmpty(base) && !TextUtils.isEmpty(head)) {
                startActivity(CommitCompareViewActivity.createIntent(repo, base, head));
            }
        } else {
            GitCommit commit = commits.get(0);
            String sha = commit != null ? commit.sha() : null;
            if (!TextUtils.isEmpty(sha)) {
                startActivity(CommitViewActivity.createIntent(repo, sha));
            }
        }
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    protected void viewRepository(Repository repository) {
        startActivity(RepositoryViewActivity.createIntent(repository));
    }

    /**
     * Start an activity to view the given {@link UserPair}
     * <p>
     * This method does nothing by default, subclasses should override
     *
     * @param users
     */
    protected void viewUser(UserPair users) {
    }

    /**
     * Start an activity to view the given {@link User}
     *
     * @param user
     * @return true if new activity started, false otherwise
     */
    protected boolean viewUser(User user) {
        return false;
    }

    /**
     * Start an activity to view the given {@link Issue}
     *
     * @param issue
     * @param repository
     */
    protected void viewIssue(Issue issue, Repository repository) {
        if (repository != null) {
            startActivity(IssuesViewActivity.createIntent(issue, repository));
        } else {
            startActivity(IssuesViewActivity.createIntent(issue));
        }
    }

    @Override
    protected Item createItem(GitHubEvent item) {
        return NewsItem.createNewsItem(avatars, item);
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_news;
    }

    @Override
    protected int getErrorMessage() {
        return R.string.error_news_load;
    }
}
