/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.issue;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.Intents.EXTRA_COMMENT_BODY;
import static com.github.mobile.Intents.EXTRA_ISSUE;
import static com.github.mobile.Intents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.mobile.RequestCodes.COMMENT_CREATE;
import static com.github.mobile.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_CLOSE;
import static com.github.mobile.RequestCodes.ISSUE_EDIT;
import static com.github.mobile.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_MILESTONE_UPDATE;
import static com.github.mobile.RequestCodes.ISSUE_REOPEN;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.core.issue.FullIssue;
import com.github.mobile.core.issue.IssueStore;
import com.github.mobile.core.issue.RefreshIssueTask;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.DialogResultListener;
import com.github.mobile.ui.SingleChoiceDialogFragment;
import com.github.mobile.ui.comment.CommentListAdapter;
import com.github.mobile.ui.comment.CreateCommentActivity;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.HttpImageGetter;
import com.github.mobile.util.TimeUtils;
import com.github.mobile.util.ToastUtils;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectView;

/**
 * Fragment to display an issue
 */
public class IssueFragment extends RoboSherlockFragment implements DialogResultListener {

    private String repositoryName;

    private String repositoryOwner;

    private int issueNumber;

    private List<Comment> comments;

    private RepositoryId repositoryId;

    private Issue issue;

    @Inject
    private AvatarLoader avatarHelper;

    @Inject
    private IssueStore store;

    @InjectView(android.R.id.list)
    private ListView list;

    private View headerView;

    private View loadingView;

    private EditMilestoneTask milestoneTask;

    private EditAssigneeTask assigneeTask;

    private EditLabelsTask labelsTask;

    private EditStateTask stateTask;

    private EditIssueTask bodyTask;

    private TextView titleText;

    private TextView bodyText;

    private TextView createdText;

    private ImageView creatorAvatar;

    private TextView assigneeText;

    private TextView assigneeLabel;

    private ImageView assigneeAvatar;

    private View labelsArea;

    private View milestoneArea;

    private View milestoneProgressArea;

    private TextView milestoneText;

    private HttpImageGetter bodyImageGetter;

    private HttpImageGetter commentImageGetter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        repositoryName = args.getString(EXTRA_REPOSITORY_NAME);
        repositoryOwner = args.getString(EXTRA_REPOSITORY_OWNER);
        repositoryId = RepositoryId.create(repositoryOwner, repositoryName);
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER);

        DialogFragmentActivity dialogActivity = (DialogFragmentActivity) getActivity();

        milestoneTask = new EditMilestoneTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                updateHeader(editedIssue);
            }
        };

        assigneeTask = new EditAssigneeTask(dialogActivity, repositoryId, issueNumber) {

            @Override
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                updateHeader(editedIssue);
            }
        };

        labelsTask = new EditLabelsTask(dialogActivity, repositoryId, issueNumber) {

            @Override
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                updateHeader(editedIssue);
            }
        };

        stateTask = new EditStateTask(dialogActivity, repositoryId, issueNumber) {

            @Override
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                updateHeader(editedIssue);
            }
        };

        bodyTask = new EditIssueTask(dialogActivity, repositoryId, issueNumber) {

            @Override
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                updateHeader(editedIssue);
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bodyImageGetter = new HttpImageGetter(getActivity());
        commentImageGetter = new HttpImageGetter(getActivity());

        list.addHeaderView(headerView, null, false);

        issue = store.getIssue(repositoryId, issueNumber);

        TextView loadingText = (TextView) loadingView.findViewById(id.tv_loading);
        loadingText.setText(string.loading_comments);
        loadingView.findViewById(id.v_separator).setVisibility(GONE);

        if (issue == null) {
            loadingText.setText(string.loading_issue);
            headerView.setVisibility(GONE);
        }

        if (issue == null || (issue.getComments() > 0 && comments == null))
            list.addHeaderView(loadingView, null, false);

        List<Comment> initialComments = comments;
        if (initialComments == null)
            initialComments = Collections.emptyList();

        Activity activity = getActivity();
        list.setAdapter(new CommentListAdapter(activity.getLayoutInflater(), initialComments
                .toArray(new Comment[initialComments.size()]), avatarHelper, commentImageGetter));

        if (issue != null && comments != null)
            updateList(issue, comments);
        else {
            if (issue != null)
                updateHeader(issue);
            refreshIssue();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.issue_view, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LayoutInflater inflater = getLayoutInflater(savedInstanceState);

        headerView = inflater.inflate(layout.issue_header, null);

        titleText = (TextView) headerView.findViewById(id.tv_issue_title);
        createdText = (TextView) headerView.findViewById(id.tv_issue_creation);
        creatorAvatar = (ImageView) headerView.findViewById(id.iv_avatar);
        assigneeText = (TextView) headerView.findViewById(id.tv_assignee_name);
        assigneeLabel = (TextView) headerView.findViewById(id.tv_assignee_label);
        assigneeAvatar = (ImageView) headerView.findViewById(id.iv_assignee_gravatar);
        labelsArea = headerView.findViewById(id.v_labels);
        milestoneArea = headerView.findViewById(id.ll_milestone);
        milestoneText = (TextView) headerView.findViewById(id.tv_milestone);
        milestoneProgressArea = (View) headerView.findViewById(id.v_closed);
        bodyText = (TextView) headerView.findViewById(id.tv_issue_body);
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());

        loadingView = inflater.inflate(layout.loading_item, null);

        milestoneArea.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    milestoneTask.prompt(issue.getMilestone());
            }
        });

        headerView.findViewById(id.ll_assignee).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null) {
                    User assignee = issue.getAssignee();
                    assigneeTask.prompt(assignee != null ? assignee.getLogin() : null);
                }
            }
        });

        labelsArea.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    labelsTask.prompt(issue.getLabels());
            }
        });
    }

    private void updateHeader(final Issue issue) {
        titleText.setText(issue.getTitle());

        bodyImageGetter.bind(bodyText, issue.getBodyHtml(), issue.getId());

        String reported = "<b>" + issue.getUser().getLogin() + "</b> opened "
                + TimeUtils.getRelativeTime(issue.getCreatedAt());

        createdText.setText(Html.fromHtml(reported));
        avatarHelper.bind(creatorAvatar, issue.getUser());

        User assignee = issue.getAssignee();
        if (assignee != null) {
            assigneeText.setText(assignee.getLogin());
            assigneeText.setVisibility(VISIBLE);
            assigneeLabel.setText(string.assigned);
            assigneeAvatar.setVisibility(VISIBLE);
            avatarHelper.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setVisibility(GONE);
            assigneeLabel.setText(string.unassigned);
        }

        if (!issue.getLabels().isEmpty()) {
            labelsArea.setVisibility(VISIBLE);
            LabelsDrawable drawable = new LabelsDrawable(getResources(), labelsArea, createdText.getTextSize(),
                    issue.getLabels());
            drawable.getPaint().setColor(getResources().getColor(android.R.color.transparent));
            labelsArea.setBackgroundDrawable(drawable);
            LayoutParams params = new LayoutParams(drawable.getBounds().width(), drawable.getBounds().height());
            labelsArea.setLayoutParams(params);
        } else
            labelsArea.setVisibility(GONE);

        if (issue.getMilestone() != null) {
            Milestone milestone = issue.getMilestone();
            milestoneText.setText(milestone.getTitle());
            float closed = milestone.getClosedIssues();
            float total = closed + milestone.getOpenIssues();
            if (total > 0) {
                ((LayoutParams) milestoneProgressArea.getLayoutParams()).weight = closed / total;
                milestoneProgressArea.setVisibility(VISIBLE);
            } else
                milestoneProgressArea.setVisibility(GONE);
            milestoneArea.setVisibility(VISIBLE);
        } else
            milestoneArea.setVisibility(GONE);

        String state = issue.getState();
        if (state != null && state.length() > 0)
            state = state.substring(0, 1).toUpperCase(Locale.US) + state.substring(1);
        else
            state = "";
    }

    private void refreshIssue() {
        new RefreshIssueTask(getActivity(), repositoryId, issueNumber, bodyImageGetter, commentImageGetter) {

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);

                ToastUtils.show(getActivity(), e, string.error_issue_load);
            }

            @Override
            protected void onSuccess(FullIssue fullIssue) throws Exception {
                super.onSuccess(fullIssue);

                issue = fullIssue.getIssue();
                comments = fullIssue;
                updateList(fullIssue.getIssue(), fullIssue);
            }
        }.execute();

    }

    private void updateList(Issue issue, List<Comment> comments) {
        list.removeHeaderView(loadingView);
        headerView.setVisibility(VISIBLE);
        updateHeader(issue);

        CommentListAdapter adapter = getRootAdapter();
        if (adapter != null)
            adapter.setItems(comments.toArray(new Comment[comments.size()]));
    }

    private CommentListAdapter getRootAdapter() {
        ListAdapter adapter = list.getAdapter();
        if (adapter == null)
            return null;
        adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        if (adapter instanceof CommentListAdapter)
            return (CommentListAdapter) adapter;
        else
            return null;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
        case ISSUE_MILESTONE_UPDATE:
            milestoneTask.edit(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_ASSIGNEE_UPDATE:
            assigneeTask.edit(arguments.getString(AssigneeDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_LABELS_UPDATE:
            labelsTask.edit(arguments.getStringArray(LabelsDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_CLOSE:
            stateTask.edit(true);
            break;
        case ISSUE_REOPEN:
            stateTask.edit(false);
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(menu.issue_view, optionsMenu);
    }

    private void createComment(final String comment) {
        new CreateCommentTask(getActivity(), repositoryId, issueNumber) {

            @Override
            protected void onSuccess(Comment comment) throws Exception {
                super.onSuccess(comment);

                if (getActivity() != null)
                    refreshIssue();
            }
        }.create(comment);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && ISSUE_EDIT == requestCode && data != null) {
            Issue editedIssue = (Issue) data.getSerializableExtra(EXTRA_ISSUE);
            bodyTask.edit(editedIssue.getTitle(), editedIssue.getBody());
            return;
        }

        if (RESULT_OK == resultCode && COMMENT_CREATE == requestCode && data != null) {
            String comment = data.getStringExtra(EXTRA_COMMENT_BODY);
            if (!TextUtils.isEmpty(comment))
                createComment(comment);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Don't allow options before issue loads
        if (issue == null)
            return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
        case id.issue_labels:
            labelsTask.prompt(issue.getLabels());
            return true;
        case id.issue_milestone:
            milestoneTask.prompt(issue.getMilestone());
            return true;
        case id.issue_assignee:
            User assignee = issue.getAssignee();
            assigneeTask.prompt(assignee != null ? assignee.getLogin() : null);
            return true;
        case id.issue_state:
            stateTask.confirm(STATE_OPEN.equals(issue.getState()));
            return true;
        case id.issue_description:
            startActivityForResult(EditIssueActivity.createIntent(issue), ISSUE_EDIT);
            return true;
        case id.issue_comment:
            String title = getString(string.issue_title) + issueNumber;
            String subtitle = repositoryId.generateId();
            startActivityForResult(CreateCommentActivity.createIntent(title, subtitle), COMMENT_CREATE);
            return true;
        case id.refresh:
            refreshIssue();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}