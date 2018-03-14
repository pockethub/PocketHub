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
package com.github.pockethub.android.ui.issue;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.core.issue.RefreshIssueTaskFactory;
import com.github.pockethub.android.rx.AutoDisposeUtils;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.DialogResultListener;
import com.github.pockethub.android.ui.base.BaseFragment;
import com.github.pockethub.android.ui.comment.DeleteCommentListener;
import com.github.pockethub.android.ui.comment.EditCommentListener;
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity;
import com.github.pockethub.android.ui.item.GitHubCommentItem;
import com.github.pockethub.android.ui.item.LoadingItem;
import com.github.pockethub.android.ui.item.issue.IssueEventItem;
import com.github.pockethub.android.ui.item.issue.IssueHeaderItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.PullRequest;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.meisolsson.githubsdk.service.issues.IssueCommentService;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.android.Intents.EXTRA_CAN_WRITE_REPO;
import static com.github.pockethub.android.Intents.EXTRA_COMMENT;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.pockethub.android.Intents.EXTRA_USER;
import static com.github.pockethub.android.RequestCodes.COMMENT_CREATE;
import static com.github.pockethub.android.RequestCodes.COMMENT_DELETE;
import static com.github.pockethub.android.RequestCodes.COMMENT_EDIT;
import static com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_CLOSE;
import static com.github.pockethub.android.RequestCodes.ISSUE_EDIT;
import static com.github.pockethub.android.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_MILESTONE_UPDATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_REOPEN;

/**
 * Fragment to display an issue
 */
public class IssueFragment extends BaseFragment
        implements IssueHeaderItem.OnIssueHeaderActionListener, DialogResultListener {

    private static final String TAG = "IssueFragment";

    private int issueNumber;

    private List<Object> items;

    private Repository repositoryId;

    private Issue issue;

    private User user;

    private boolean canWrite;

    @Inject
    protected AvatarLoader avatars;

    @Inject
    protected IssueStore store;

    @BindView(android.R.id.list)
    protected RecyclerView list;

    @BindView(R.id.pb_loading)
    protected ProgressBar progress;

    private GroupAdapter adapter = new GroupAdapter();

    private Section mainSection = new Section();

    @Inject
    protected RefreshIssueTaskFactory refreshIssueTaskFactory;

    @Inject
    protected EditLabelsTaskFactory labelsTaskFactory;

    @Inject
    protected EditMilestoneTaskFactory milestoneTaskFactory;

    @Inject
    protected EditAssigneeTaskFactory assigneeTaskFactory;

    @Inject
    protected EditStateTaskFactory stateTaskFactory;

    private EditMilestoneTask milestoneTask;

    private EditAssigneeTask assigneeTask;

    private EditLabelsTask labelsTask;

    private EditStateTask stateTask;

    private MenuItem stateItem;

    @Inject
    protected HttpImageGetter bodyImageGetter;

    @Inject
    protected HttpImageGetter commentImageGetter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        repositoryId = InfoUtils.createRepoFromData(
                args.getString(EXTRA_REPOSITORY_OWNER),
                args.getString(EXTRA_REPOSITORY_NAME));
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER);
        user = args.getParcelable(EXTRA_USER);
        canWrite = args.getBoolean(EXTRA_CAN_WRITE_REPO, false);

        BaseActivity dialogActivity = (BaseActivity) getActivity();

        milestoneTask = milestoneTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver());
        labelsTask = labelsTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver());
        assigneeTask = assigneeTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver());
        stateTask = stateTaskFactory.create(dialogActivity, repositoryId, issueNumber, createObserver());

        adapter.add(mainSection);
    }

    private Consumer<Issue> createObserver() {
        return issue -> {
            updateHeader(issue);
            refreshIssue();
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        issue = store.getIssue(repositoryId, issueNumber);
        if (issue == null || (issue.comments() > 0 && items == null)) {
            mainSection.setFooter(new LoadingItem(R.string.loading_comments));
        }

        if (issue != null && items != null) {
            updateList(issue, items);
        } else {
            if (issue != null) {
                updateHeader(issue);
            }
            refreshIssue();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DividerItemDecoration itemDecoration =
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.list_divider_5dp));

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(itemDecoration);
        list.setAdapter(adapter);
    }

    private void updateHeader(final Issue issue) {
        if (!isAdded()) {
            return;
        }

        mainSection.setHeader(
                new IssueHeaderItem(avatars, bodyImageGetter, getActivity(), this, issue));

        progress.setVisibility(GONE);
        list.setVisibility(VISIBLE);
        updateStateItem(issue);
    }

    private void refreshIssue() {
        refreshIssueTaskFactory.create(repositoryId, issueNumber)
                .refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(fullIssue -> isAdded())
                .as(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe(fullIssue -> {
                    issue = fullIssue.getIssue();
                    items = new ArrayList<>();
                    items.addAll(fullIssue.getEvents());
                    items.addAll(fullIssue.getComments());
                    updateList(fullIssue.getIssue(), items);
                }, e -> {
                    ToastUtils.show(getActivity(), R.string.error_issue_load);
                    progress.setVisibility(GONE);
                });
    }

    private void updateList(Issue issue, List<Object> items) {
        Collections.sort(items, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                Date l = getDate(lhs);
                Date r = getDate(rhs);

                if (l == null && r != null) {
                    return 1;
                } else if (l != null && r == null) {
                    return -1;
                } else if (l == null && r == null) {
                    return 0;
                } else {
                    return l.compareTo(r);
                }
            }

            private Date getDate(Object obj) {
                if (obj instanceof GitHubComment) {
                    return ((GitHubComment) obj).createdAt();
                } else if (obj instanceof GitHubEvent) {
                    return ((GitHubEvent) obj).createdAt();
                } else if (obj instanceof IssueEvent) {
                    return ((IssueEvent) obj).createdAt();
                }

                return null;
            }
        });

        List<Item> listItems = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof IssueEvent) {
                listItems.add(new IssueEventItem(avatars, getActivity(), issue, (IssueEvent) item));
            } else if (item instanceof GitHubComment) {
                listItems.add(
                        new GitHubCommentItem(avatars, commentImageGetter,
                                editCommentListener, deleteCommentListener,
                                AccountUtils.getLogin(getActivity()), canWrite,
                                (GitHubComment) item
                        )
                );
            }
        }

        mainSection.removeFooter();
        mainSection.update(listItems);
        updateHeader(issue);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            milestoneTask.edit(MilestoneDialogFragment.getSelected(arguments));
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            assigneeTask.edit(AssigneeDialogFragment.getSelected(arguments));
            break;
        case ISSUE_LABELS_UPDATE:
            ArrayList<Label> labels = LabelsDialogFragment.getSelected(arguments);
            labelsTask.edit(labels);
            break;
        case ISSUE_CLOSE:
            stateTask.edit(true);
            break;
        case ISSUE_REOPEN:
            stateTask.edit(false);
            break;
        case COMMENT_DELETE:
            final GitHubComment comment = arguments.getParcelable(EXTRA_COMMENT);

            ServiceGenerator.createService(getActivity(), IssueCommentService.class)
                    .deleteIssueComment(repositoryId.owner().login(), repositoryId.name(), comment.id())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(getActivity(), R.string.deleting_comment))
                    .as(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe(response -> {
                        if (items != null) {
                            int commentPosition = findCommentPositionInItems(comment);
                            if (commentPosition >= 0) {
                                issue = issue.toBuilder().comments(issue.comments() - 1).build();
                                items.remove(commentPosition);
                                updateList(issue, items);
                            }
                        } else {
                            refreshIssue();
                        }
                    }, e -> {
                        Log.d(TAG, "Exception deleting comment on issue", e);
                        ToastUtils.show(getActivity(), e.getMessage());
                    });
            break;
        }
    }

    private void updateStateItem(Issue issue) {
        if (issue != null && stateItem != null) {
            if (IssueState.Open.equals(issue.state())) {
                stateItem.setTitle(R.string.close);
                stateItem.setIcon(R.drawable.ic_github_issue_closed_white_24dp);
            } else {
                stateItem.setTitle(R.string.reopen);
                stateItem.setIcon(R.drawable.ic_github_issue_reopened_white_24dp);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem editItem = menu.findItem(R.id.m_edit);
        MenuItem stateItem = menu.findItem(R.id.m_state);
        if (editItem != null && stateItem != null) {
            boolean isCreator = false;
            if(issue != null) {
                isCreator = issue.user().login().equals(AccountUtils.getLogin(getActivity()));
            }
            editItem.setVisible(canWrite || isCreator);
            stateItem.setVisible(canWrite || isCreator);
        }
        updateStateItem(issue);
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_issue_view, optionsMenu);
        stateItem = optionsMenu.findItem(R.id.m_state);
        updateStateItem(issue);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode || data == null) {
            return;
        }

        switch (requestCode) {
        case ISSUE_EDIT:
            Issue editedIssue = data.getParcelableExtra(EXTRA_ISSUE);
            bodyImageGetter.encode(editedIssue.id(), editedIssue.bodyHtml());
            updateHeader(editedIssue);
            return;
        case COMMENT_CREATE:
            GitHubComment comment = data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (items != null) {
                items.add(comment);
                issue = issue.toBuilder().comments(issue.comments() + 1).build();
                updateList(issue, items);
            } else {
                refreshIssue();
            }
            return;
        case COMMENT_EDIT:
            comment = data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (items != null && comment != null) {
                int commentPosition = findCommentPositionInItems(comment);
                if (commentPosition >= 0) {
                    commentImageGetter.removeFromCache(comment.id());
                    replaceCommentInItems(commentPosition, comment);
                    updateList(issue, items);
                }
            } else {
                refreshIssue();
            }
        }
    }

    private void shareIssue() {
        String id = InfoUtils.createRepoId(repositoryId);
        if (IssueUtils.isPullRequest(issue)) {
            startActivity(ShareUtils.create("Pull Request " + issueNumber
                    + " on " + id, "https://github.com/" + id + "/pull/"
                    + issueNumber));
        } else {
            startActivity(ShareUtils
                    .create("Issue " + issueNumber + " on " + id,
                            "https://github.com/" + id + "/issues/"
                                    + issueNumber));
        }
    }

    private void openPullRequestCommits() {
        if (IssueUtils.isPullRequest(issue)) {
            PullRequest pullRequest = issue.pullRequest();

            String base = pullRequest.base().sha();
            String head = pullRequest.head().sha();
            Repository repo = pullRequest.base().repo();
            startActivity(CommitCompareViewActivity.createIntent(repo, base, head));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.m_edit:
            if (issue != null) {
                startActivityForResult(EditIssueActivity.createIntent(issue,
                        repositoryId.owner().login(), repositoryId.name(), user),
                        ISSUE_EDIT);
            }
            return true;
        case R.id.m_comment:
            if (issue != null) {
                startActivityForResult(CreateCommentActivity.createIntent(
                        repositoryId, issueNumber, user), COMMENT_CREATE);
            }
            return true;
        case R.id.m_refresh:
            refreshIssue();
            return true;
        case R.id.m_share:
            if (issue != null) {
                shareIssue();
            }
            return true;
        case R.id.m_state:
            if (issue != null) {
                stateTask.confirm(IssueState.Open.equals(issue.state()));
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Edit existing comment
     */
    final EditCommentListener editCommentListener = new EditCommentListener() {
        @Override
        public void onEditComment(GitHubComment comment) {
            startActivityForResult(EditCommentActivity.createIntent(
                    repositoryId, issueNumber, comment, user), COMMENT_EDIT);
        }
    };

    /**
     * Delete existing comment
     */
    final DeleteCommentListener deleteCommentListener = comment -> {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_COMMENT, comment);
        ConfirmDialogFragment.show(
                getActivity(),
                COMMENT_DELETE,
                getActivity()
                        .getString(R.string.confirm_comment_delete_title),
                getActivity().getString(
                        R.string.confirm_comment_delete_message), args);
    };

    /**
     * Finds the position of the given comment in the list of this issue's items.
     *
     * @param comment The comment to look for.
     * @return The position of the comment in the list, or -1 if not found.
     */
    private int findCommentPositionInItems(@NonNull GitHubComment comment) {
        int commentPosition = -1;
        Object currentItem = null;
        for (int currentPosition = 0; currentPosition < items.size(); currentPosition++) {
            currentItem = items.get(currentPosition);
            if (currentItem instanceof GitHubComment &&
                    comment.id() == ((GitHubComment) currentItem).id()) {
                commentPosition = currentPosition;
                break;
            }
        }
        return commentPosition;
    }

    /**
     * Replaces a comment in the list by another
     *
     * @param commentPosition The position of the comment in the list
     * @param comment         The comment to replace
     * @return True if successfully removed, false otherwise.
     */
    private boolean replaceCommentInItems(int commentPosition, @NonNull GitHubComment comment) {
        Object item = items.get(commentPosition);
        boolean result = false;
        if (item instanceof GitHubComment) {
            items.set(commentPosition, comment);
            result = true;
        }
        return result;
    }

    @Override
    public void onCommitsClicked() {
        if (IssueUtils.isPullRequest(issue)) {
            openPullRequestCommits();
        }
    }

    @Override
    public void onStateClicked() {
        if (issue != null) {
            stateTask.confirm(IssueState.Open.equals(issue.state()));
        }
    }

    @Override
    public void onMilestonesClicked() {
        if (issue != null && canWrite) {
            milestoneTask.prompt(issue.milestone());
        }
    }

    @Override
    public void onAssigneesClicked() {
        if (issue != null && canWrite) {
            assigneeTask.prompt(issue.assignee());
        }
    }

    @Override
    public void onLabelsClicked() {
        if (issue != null && canWrite) {
            labelsTask.prompt(issue.labels());
        }
    }
}
