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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.IssueState;
import com.alorma.github.sdk.bean.dto.response.Label;
import com.alorma.github.sdk.bean.dto.response.Milestone;
import com.alorma.github.sdk.bean.dto.response.PullRequest;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.User;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.bean.issue.IssueStory;
import com.alorma.github.sdk.bean.issue.IssueStoryComment;
import com.alorma.github.sdk.services.issues.DeleteIssueCommentClient;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.issue.IssueStore;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.core.issue.RefreshIssueTask;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.ConfirmDialogFragment;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.HeaderFooterListAdapter;
import com.github.pockethub.android.ui.SelectableLinkMovementMethod;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.comment.CommentListAdapter;
import com.github.pockethub.android.ui.comment.DeleteCommentListener;
import com.github.pockethub.android.ui.comment.EditCommentListener;
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity;
import com.github.pockethub.android.ui.view.IssueHeaderView;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.HttpImageGetter;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ShareUtils;
import com.github.pockethub.android.util.TimeUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.github.pockethub.android.util.TypefaceUtils;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
import static com.github.pockethub.android.util.TypefaceUtils.ICON_COMMIT;

/**
 * Fragment to display an issue
 */
public class IssueFragment extends DialogFragment {

    private static final String TAG = "IssueFragment";

    private int issueNumber;

    private List<Object> items;

    private Repo repositoryId;

    private Issue issue;

    private User user;

    private boolean canWrite;

    @Inject
    private AvatarLoader avatars;

    @Inject
    private IssueStore store;

    private ListView list;

    private ProgressBar progress;

    private View listHeaderView;

    private View loadingView;

    private View footerView;

    private HeaderFooterListAdapter<CommentListAdapter> adapter;

    private EditMilestoneTask milestoneTask;

    private EditAssigneeTask assigneeTask;

    private EditLabelsTask labelsTask;

    private EditStateTask stateTask;

    private TextView stateText;

    private TextView bodyText;

    private ViewGroup commitsView;

    private TextView assigneeText;

    private ImageView assigneeAvatar;

    private TextView labelsArea;

    private View milestoneArea;

    private View milestoneProgressArea;

    private TextView milestoneText;

    private MenuItem stateItem;

    @Inject
    private HttpImageGetter bodyImageGetter;

    @Inject
    private HttpImageGetter commentImageGetter;

    private IssueHeaderView issueHeader;

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

        ProgressObserverAdapter<Issue> observer = new ProgressObserverAdapter<Issue>(getActivity()) {
            @Override
            public void onNext(Issue issue) {
                super.onNext(issue);
                updateHeader(issue);
                refreshIssue();
            }
        };

        milestoneTask = new EditMilestoneTask(dialogActivity, repositoryId, issueNumber, observer);
        assigneeTask = new EditAssigneeTask(dialogActivity, repositoryId, issueNumber, observer);
        labelsTask = new EditLabelsTask(dialogActivity, repositoryId, issueNumber, observer);
        stateTask = new EditStateTask(dialogActivity, repositoryId, issueNumber, observer);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter.addHeader(listHeaderView);
        adapter.addFooter(footerView);

        issue = store.getIssue(repositoryId, issueNumber);

        TextView loadingText = (TextView) loadingView
                .findViewById(R.id.tv_loading);
        loadingText.setText(R.string.loading_comments);

        if (issue == null || (issue.comments > 0 && items == null))
            adapter.addHeader(loadingView);

        if (issue != null && items != null)
            updateList(issue, items);
        else {
            if (issue != null)
                updateHeader(issue);
            refreshIssue();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_issue, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = finder.find(android.R.id.list);
        progress = finder.find(R.id.pb_loading);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);

        issueHeader = (IssueHeaderView) view.findViewById(R.id.issue_header);

        listHeaderView = inflater.inflate(R.layout.issue_header, null);

        stateText = (TextView) listHeaderView.findViewById(R.id.tv_state);
        commitsView = (ViewGroup) listHeaderView.findViewById(R.id.ll_issue_commits);
        assigneeText = (TextView) listHeaderView.findViewById(R.id.tv_assignee_name);
        assigneeAvatar = (ImageView) listHeaderView
                .findViewById(R.id.iv_assignee_avatar);
        labelsArea = (TextView) listHeaderView.findViewById(R.id.tv_labels);
        milestoneArea = listHeaderView.findViewById(R.id.ll_milestone);
        milestoneText = (TextView) listHeaderView.findViewById(R.id.tv_milestone);
        milestoneProgressArea = listHeaderView.findViewById(R.id.v_closed);
        bodyText = (TextView) listHeaderView.findViewById(R.id.tv_issue_body);
        bodyText.setMovementMethod(SelectableLinkMovementMethod.getInstance());

        loadingView = inflater.inflate(R.layout.loading_item, null);

        footerView = inflater.inflate(R.layout.footer_separator, null);

        commitsView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (IssueUtils.isPullRequest(issue))
                    openPullRequestCommits();
            }
        });

        stateText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (issue != null)
                    stateTask.confirm(IssueState.open.equals(issue.state));
            }
        });

        milestoneArea.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (issue != null && canWrite)
                    milestoneTask.prompt(issue.milestone);
            }
        });

        listHeaderView.findViewById(R.id.ll_assignee).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (issue != null && canWrite)
                            assigneeTask.prompt(issue.assignee);
                    }
                });

        labelsArea.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (issue != null && canWrite)
                    labelsTask.prompt(issue.labels);
            }
        });

        Activity activity = getActivity();
        String userName = AccountUtils.getLogin(activity);

        adapter = new HeaderFooterListAdapter<>(list,
                new CommentListAdapter(activity.getLayoutInflater(), null, avatars,
                        commentImageGetter, editCommentListener, deleteCommentListener, userName, canWrite, issue));
        list.setAdapter(adapter);
    }

    private void updateHeader(final Issue issue) {
        if (!isUsable())
            return;

        issueHeader.updateHeader(issue);

        String body = issue.body_html;
        if (!TextUtils.isEmpty(body))
            bodyImageGetter.bind(bodyText, body, issue.id);
        else
            bodyText.setText(R.string.no_description_given);

        if (IssueUtils.isPullRequest(issue) && issue.pullRequest.commits > 0) {
            ViewUtils.setGone(commitsView, false);

            TextView icon = (TextView) listHeaderView.findViewById(R.id.tv_commit_icon);
            TypefaceUtils.setOcticons(icon);
            icon.setText(ICON_COMMIT);

            String commits = getString(R.string.pull_request_commits,
                    issue.pullRequest.commits);
            ((TextView) listHeaderView.findViewById(R.id.tv_pull_request_commits)).setText(commits);
        } else
            ViewUtils.setGone(commitsView, true);

        boolean open = IssueState.open.equals(issue.state);
        if (!open) {
            StyledText text = new StyledText();
            text.bold(getString(R.string.closed));
            Date closedAt = TimeUtils.stringToDate(issue.closedAt);
            if (closedAt != null)
                text.append(' ').append(closedAt);
            stateText.setText(text);
        }
        ViewUtils.setGone(stateText, open);

        User assignee = issue.assignee;
        if (assignee != null) {
            StyledText name = new StyledText();
            name.bold(assignee.login);
            name.append(' ').append(getString(R.string.assigned));
            assigneeText.setText(name);
            assigneeAvatar.setVisibility(VISIBLE);
            avatars.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText(R.string.unassigned);
        }

        List<Label> labels = issue.labels;
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(labelsArea, labels);
            labelsArea.setVisibility(VISIBLE);
        } else
            labelsArea.setVisibility(GONE);

        if (issue.milestone != null) {
            Milestone milestone = issue.milestone;
            StyledText milestoneLabel = new StyledText();
            milestoneLabel.append(getString(R.string.milestone_prefix));
            milestoneLabel.append(' ');
            milestoneLabel.bold(milestone.title);
            milestoneText.setText(milestoneLabel);
            float closed = milestone.closedIssues;
            float total = closed + milestone.openIssues;
            if (total > 0) {
                ((LayoutParams) milestoneProgressArea.getLayoutParams()).weight = closed
                        / total;
                milestoneProgressArea.setVisibility(VISIBLE);
            } else
                milestoneProgressArea.setVisibility(GONE);
            milestoneArea.setVisibility(VISIBLE);
        } else
            milestoneArea.setVisibility(GONE);

        ViewUtils.setGone(issueHeader, false);
        ViewUtils.setGone(progress, true);
        ViewUtils.setGone(list, false);
        updateStateItem(issue);
    }

    private void refreshIssue() {
        Observable.create(new RefreshIssueTask(getActivity(), repositoryId, issueNumber, bodyImageGetter))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<IssueStory>bindToLifecycle())
                .subscribe(new ObserverAdapter<IssueStory>() {
                    @Override
                    public void onNext(IssueStory fullIssue) {
                        if (!isUsable())
                            return;

                        issue = fullIssue.issue;
                        items = new ArrayList<>();
                        items.addAll(fullIssue.details);
                        updateList(fullIssue.issue, items);
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show(getActivity(), e, R.string.error_issue_load);
                        ViewUtils.setGone(progress, true);
                    }
                });
    }

    private void updateList(Issue issue, List<Object> items) {
        adapter.getWrappedAdapter().setItems(items);
        adapter.removeHeader(loadingView);
        adapter.getWrappedAdapter().setIssue(issue);

        adapter.getWrappedAdapter().notifyDataSetChanged();

        listHeaderView.setVisibility(VISIBLE);
        updateHeader(issue);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            milestoneTask.edit(MilestoneDialogFragment.getSelected(arguments));
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            assigneeTask.edit(AssigneeDialogFragment.getSelected(arguments));
            break;
        case ISSUE_LABELS_UPDATE:
            ArrayList<Label> labels = LabelsDialogFragment
                    .getSelected(arguments);
            if (labels != null && !labels.isEmpty())
                labelsTask.edit(labels.toArray(new Label[labels.size()]));
            else
                labelsTask.edit(null);
            break;
        case ISSUE_CLOSE:
            stateTask.edit(true);
            break;
        case ISSUE_REOPEN:
            stateTask.edit(false);
            break;
        case COMMENT_DELETE:
            final GithubComment comment = arguments.getParcelable(EXTRA_COMMENT);
            RepoInfo info = InfoUtils.createRepoInfo(repositoryId);
            showProgressIndeterminate(R.string.deleting_comment);
            new DeleteIssueCommentClient(info, comment.id).observable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.<Response>bindToLifecycle())
                    .subscribe(new ObserverAdapter<Response>() {

                        @Override
                        public void onNext(Response response) {
                            if (items != null) {
                                int commentPosition = findCommentPositionInItems(comment);
                                if (commentPosition >= 0) {
                                    issue.comments--;
                                    items.remove(commentPosition);
                                    updateList(issue, items);
                                }
                            } else {
                                refreshIssue();
                            }
                            dismissProgress();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "Exception deleting comment on issue", e);
                            ToastUtils.show(getActivity(), e.getMessage());
                            dismissProgress();
                        }
                    });
            break;
        }
    }

    private void updateStateItem(Issue issue) {
        if (issue != null && stateItem != null) {
            if (IssueState.open.equals(issue.state)) {
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
            if(issue != null)
                isCreator = issue.user.login.equals(AccountUtils.getLogin(getActivity()));
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
        if (RESULT_OK != resultCode || data == null)
            return;

        switch (requestCode) {
        case ISSUE_EDIT:
            Issue editedIssue = data.getParcelableExtra(EXTRA_ISSUE);
            bodyImageGetter.encode(editedIssue.id, editedIssue.body_html);
            updateHeader(editedIssue);
            return;
        case COMMENT_CREATE:
            GithubComment comment = data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (items != null) {
                IssueStoryComment storyComment = new IssueStoryComment(comment);
                items.add(storyComment);
                issue.comments = issue.comments + 1;
                updateList(issue, items);
            } else
                refreshIssue();
            return;
        case COMMENT_EDIT:
            comment = data
                    .getParcelableExtra(EXTRA_COMMENT);
            if (items != null && comment != null) {
                int commentPosition = findCommentPositionInItems(comment);
                if (commentPosition >= 0) {
                    commentImageGetter.removeFromCache(comment.id);
                    replaceCommentInItems(commentPosition, comment);
                    updateList(issue, items);
                }
            } else
                refreshIssue();
        }
    }

    private void shareIssue() {
        String id = InfoUtils.createRepoId(repositoryId);
        if (IssueUtils.isPullRequest(issue))
            startActivity(ShareUtils.create("Pull Request " + issueNumber
                    + " on " + id, "https://github.com/" + id + "/pull/"
                    + issueNumber));
        else
            startActivity(ShareUtils
                    .create("Issue " + issueNumber + " on " + id,
                            "https://github.com/" + id + "/issues/"
                                    + issueNumber));
    }

    private void openPullRequestCommits() {
        if (IssueUtils.isPullRequest(issue)) {
            PullRequest pullRequest = issue.pullRequest;

            String base = pullRequest.base.sha;
            String head = pullRequest.head.sha;
            Repo repo = pullRequest.base.repo;
            startActivity(CommitCompareViewActivity.createIntent(repo, base, head));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.m_edit:
            if (issue != null)
                startActivityForResult(EditIssueActivity.createIntent(issue,
                                repositoryId.owner.login, repositoryId.name, user),
                        ISSUE_EDIT);
            return true;
        case R.id.m_comment:
            if (issue != null)
                startActivityForResult(CreateCommentActivity.createIntent(
                        repositoryId, issueNumber, user), COMMENT_CREATE);
            return true;
        case R.id.m_refresh:
            refreshIssue();
            return true;
        case R.id.m_share:
            if (issue != null)
                shareIssue();
            return true;
        case R.id.m_state:
            if (issue != null)
                stateTask.confirm(IssueState.open.equals(issue.state));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Edit existing comment
     */
    final EditCommentListener editCommentListener = new EditCommentListener() {
        public void onEditComment(GithubComment comment) {
            startActivityForResult(EditCommentActivity.createIntent(
                    repositoryId, issueNumber, comment, user), COMMENT_EDIT);
        }
    };

    /**
     * Delete existing comment
     */
    final DeleteCommentListener deleteCommentListener = new DeleteCommentListener() {
        public void onDeleteComment(GithubComment comment) {
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_COMMENT, comment);
            ConfirmDialogFragment.show(
                    getActivity(),
                    COMMENT_DELETE,
                    getActivity()
                            .getString(R.string.confirm_comment_delete_title),
                    getActivity().getString(
                            R.string.confirm_comment_delete_message), args);
        }
    };

    /**
     * Finds the position of the given comment in the list of this issue's items.
     *
     * @param comment The comment to look for.
     * @return The position of the comment in the list, or -1 if not found.
     */
    private int findCommentPositionInItems(@NonNull GithubComment comment) {
        int commentPosition = -1;
        Object currentItem = null;
        for (int currentPosition = 0; currentPosition < items.size(); currentPosition++) {
            currentItem = items.get(currentPosition);
            if (currentItem instanceof IssueStoryComment &&
                comment.id.equals((((IssueStoryComment) currentItem).comment).id)) {
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
    private boolean replaceCommentInItems(int commentPosition, @NonNull GithubComment comment) {
        Object item = items.get(commentPosition);
        boolean result = false;
        if (item instanceof IssueStoryComment) {
            ((IssueStoryComment) item).comment = comment;
            result = true;
        }
        return result;
    }

}
