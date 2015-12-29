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
package com.github.pockethub.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.sdk.bean.dto.response.Commit;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubEvent;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Release;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.dto.response.events.payload.CommitCommentEventPayload;
import com.alorma.github.sdk.bean.dto.response.events.payload.PushEventPayload;
import com.alorma.github.sdk.bean.dto.response.events.payload.ReleaseEventPayload;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewFinder;
import com.github.pockethub.R;
import com.github.pockethub.core.gist.GistEventMatcher;
import com.github.pockethub.core.issue.IssueEventMatcher;
import com.github.pockethub.core.repo.RepositoryEventMatcher;
import com.github.pockethub.core.user.UserEventMatcher;
import com.github.pockethub.core.user.UserEventMatcher.UserPair;
import com.github.pockethub.ui.commit.CommitCompareViewActivity;
import com.github.pockethub.ui.commit.CommitViewActivity;
import com.github.pockethub.ui.gist.GistsViewActivity;
import com.github.pockethub.ui.issue.IssuesViewActivity;
import com.github.pockethub.ui.repo.RepositoryViewActivity;
import com.github.pockethub.ui.user.NewsListAdapter;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.ConvertUtils;
import com.github.pockethub.util.InfoUtils;
import com.google.gson.Gson;
import com.google.inject.Inject;

import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_BROWSABLE;
import static com.alorma.github.sdk.bean.dto.response.events.EventType.CommitCommentEvent;
import static com.alorma.github.sdk.bean.dto.response.events.EventType.DownloadEvent;
import static com.alorma.github.sdk.bean.dto.response.events.EventType.PushEvent;

/**
 * Base news fragment class with utilities for subclasses to built on
 */
public abstract class NewsFragment extends PagedItemFragment<GithubEvent> {

    /**
     * Matcher for finding an {@link Issue} from an {@link GithubEvent}
     */
    protected final IssueEventMatcher issueMatcher = new IssueEventMatcher();

    /**
     * Matcher for finding a {@link Gist} from an {@link GithubEvent}
     */
    protected final GistEventMatcher gistMatcher = new GistEventMatcher();

    /**
     * Matcher for finding a {@link Repo} from an {@link GithubEvent}
     */
    protected final RepositoryEventMatcher repoMatcher = new RepositoryEventMatcher();

    /**
     * Matcher for finding a {@link User} from an {@link GithubEvent}
     */
    protected final UserEventMatcher userMatcher = new UserEventMatcher();

    @Inject
    private AvatarLoader avatars;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_news);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        GithubEvent event = (GithubEvent) l.getItemAtPosition(position);

        if (DownloadEvent.equals(event.getType())) {
            openDownload(event);
            return;
        }

        if (PushEvent.equals(event.getType())) {
            openPush(event);
            return;
        }

        if (CommitCommentEvent.equals(event.getType())) {
            openCommitComment(event);
            return;
        }

        Issue issue = issueMatcher.getIssue(event);
        if (issue != null) {
            Repo repo = ConvertUtils.eventRepoToRepo(event.repo);
            viewIssue(issue, repo);
            return;
        }

        Gist gist = gistMatcher.getGist(event);
        if (gist != null) {
            startActivity(GistsViewActivity.createIntent(gist));
            return;
        }

        Repo repo = repoMatcher.getRepository(event);
        if (repo != null)
            viewRepository(repo);

        UserPair users = userMatcher.getUsers(event);
        if (users != null)
            viewUser(users);
    }

    @Override
    public boolean onListItemLongClick(ListView l, View v, int position,
            long itemId) {
        if (!isUsable())
            return false;

        final GithubEvent event = (GithubEvent) l.getItemAtPosition(position);
        final Repo repo = ConvertUtils.eventRepoToRepo(event.repo);
        final User user = event.actor;

        if (repo != null && user != null) {
            final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                    .title(R.string.navigate_to);

            // Hacky but necessary since material dialogs has a different API
            final MaterialDialog[] dialogHolder = new MaterialDialog[1];

            View view = getActivity().getLayoutInflater().inflate(
                    R.layout.nav_dialog, null);
            ViewFinder finder = new ViewFinder(view);
            avatars.bind(finder.imageView(R.id.iv_user_avatar), user);
            avatars.bind(finder.imageView(R.id.iv_repo_avatar), repo.owner);
            finder.setText(R.id.tv_login, user.login);
            finder.setText(R.id.tv_repo_name, InfoUtils.createRepoId(repo));
            finder.onClick(R.id.ll_user_area, new OnClickListener() {

                public void onClick(View v) {
                    dialogHolder[0].dismiss();
                    viewUser(user);
                }
            });
            finder.onClick(R.id.ll_repo_area, new OnClickListener() {

                public void onClick(View v) {
                    dialogHolder[0].dismiss();
                    viewRepository(repo);
                }
            });
            builder.customView(view, false);

            MaterialDialog dialog = builder.build();
            dialogHolder[0] = dialog;
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            return true;
        }

        return false;
    }

    // https://developer.github.com/v3/repos/downloads/#downloads-api-is-deprecated
    private void openDownload(GithubEvent event) {
        Gson gson = new Gson();
        String json = gson.toJson(event.payload);

        Release release = gson.fromJson(json, ReleaseEventPayload.class).release;
        if (release == null)
            return;

        String url = release.html_url;
        if (TextUtils.isEmpty(url))
            return;

        Intent intent = new Intent(ACTION_VIEW, Uri.parse(url));
        intent.addCategory(CATEGORY_BROWSABLE);
        startActivity(intent);
    }

    private void openCommitComment(GithubEvent event) {
        Repo repo = ConvertUtils.eventRepoToRepo(event.repo);
        if (repo == null)
            return;

        if(repo.name.contains("/")) {
            String[] repoId = repo.name.split("/");
            repo = InfoUtils.createRepoFromData(repoId[0], repoId[1]);
        }

        Gson gson = new Gson();
        String json = gson.toJson(event.payload);

        CommitCommentEventPayload payload = gson.fromJson(json, CommitCommentEventPayload.class);
        CommitComment comment = payload.comment;
        if (comment == null)
            return;

        String sha = comment.commit_id;
        if (!TextUtils.isEmpty(sha))
            startActivity(CommitViewActivity.createIntent(repo, sha));
    }

    private void openPush(GithubEvent event) {
        Repo repo = ConvertUtils.eventRepoToRepo(event.repo);
        if (repo == null)
            return;

        Gson gson = new Gson();
        String json = gson.toJson(event.payload);

        PushEventPayload payload = gson.fromJson(json, PushEventPayload.class);
        List<Commit> commits = payload.commits;
        if (commits == null || commits.isEmpty())
            return;

        if (commits.size() > 1) {
            String base = payload.before;
            String head = payload.head;
            if (!TextUtils.isEmpty(base) && !TextUtils.isEmpty(head))
                startActivity(CommitCompareViewActivity.createIntent(repo,
                        base, head));
        } else {
            Commit commit = commits.get(0);
            String sha = commit != null ? commit.sha : null;
            if (!TextUtils.isEmpty(sha))
                startActivity(CommitViewActivity.createIntent(repo, sha));
        }
    }

    /**
     * Start an activity to view the given repository
     *
     * @param repository
     */
    protected void viewRepository(Repo repository) {
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
    protected void viewIssue(Issue issue, Repo repository) {
        if (repository != null)
            startActivity(IssuesViewActivity.createIntent(issue, repository));
        else
            startActivity(IssuesViewActivity.createIntent(issue));
    }

    @Override
    protected SingleTypeAdapter<GithubEvent> createAdapter(List<GithubEvent> items) {
        return new NewsListAdapter(getActivity().getLayoutInflater(),
                items.toArray(new GithubEvent[items.size()]), avatars);
    }

    @Override
    protected int getLoadingMessage() {
        return R.string.loading_news;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_news_load;
    }
}
