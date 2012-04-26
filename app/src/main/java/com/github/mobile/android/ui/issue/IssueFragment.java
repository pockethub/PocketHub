package com.github.mobile.android.ui.issue;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.RequestCodes.COMMENT_CREATE;
import static com.github.mobile.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_CLOSE;
import static com.github.mobile.android.RequestCodes.ISSUE_EDIT;
import static com.github.mobile.android.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_MILESTONE_UPDATE;
import static com.github.mobile.android.RequestCodes.ISSUE_REOPEN;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENTS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_ISSUE_NUMBER;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_NAME;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY_OWNER;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.MultiChoiceDialogFragment;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.RefreshAnimation;
import com.github.mobile.android.SingleChoiceDialogFragment;
import com.github.mobile.android.async.AuthenticatedUserTask;
import com.github.mobile.android.comment.CommentViewHolder;
import com.github.mobile.android.comment.CreateCommentActivity;
import com.github.mobile.android.core.issue.FullIssue;
import com.github.mobile.android.issue.EditIssueActivity;
import com.github.mobile.android.issue.IssueHeaderViewHolder;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.ui.DialogResultListener;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.ErrorHelper;
import com.github.mobile.android.util.HtmlFormatter;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.IssueService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;

/**
 * Fragment to display an issue
 */
public class IssueFragment extends RoboSherlockFragment implements DialogResultListener {

    private static final String TAG = "IssueFragment";

    @Inject
    private ContextScopedProvider<IssueService> service;

    private String repositoryName;

    private String repositoryOwner;

    private int issueNumber;

    private List<Comment> comments;

    private RepositoryId repositoryId;

    private Issue issue;

    @Inject
    private AvatarHelper avatarHelper;

    @Inject
    private IssueStore store;

    @InjectView(android.R.id.list)
    private ListView list;

    private View headerView;

    private IssueHeaderViewHolder headerHolder;

    private View loadingView;

    private RefreshAnimation refreshAnimation = new RefreshAnimation();

    private EditMilestoneTask milestoneTask;

    private EditAssigneeTask assigneeTask;

    private EditLabelsTask labelsTask;

    private EditStateTask stateTask;

    private EditIssueTask bodyTask;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        repositoryName = args.getString(EXTRA_REPOSITORY_NAME);
        repositoryOwner = args.getString(EXTRA_REPOSITORY_OWNER);
        repositoryId = RepositoryId.create(repositoryOwner, repositoryName);
        issueNumber = args.getInt(EXTRA_ISSUE_NUMBER);
        comments = (List<Comment>) args.getSerializable(EXTRA_COMMENTS);

        DialogFragmentActivity dialogActivity = (DialogFragmentActivity) getActivity();

        milestoneTask = new EditMilestoneTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        assigneeTask = new EditAssigneeTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        labelsTask = new EditLabelsTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        stateTask = new EditStateTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };

        bodyTask = new EditIssueTask(dialogActivity, repositoryId, issueNumber) {
            protected void onSuccess(Issue editedIssue) throws Exception {
                super.onSuccess(editedIssue);

                headerHolder.updateViewFor(editedIssue);
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setFastScrollEnabled(true);
        list.addHeaderView(headerView, null, false);

        issue = store.getIssue(repositoryId, issueNumber);

        if (issue != null)
            headerHolder.updateViewFor(issue);
        else {
            ((TextView) loadingView.findViewById(id.tv_loading)).setText(string.loading_issue);
            headerView.setVisibility(GONE);
        }

        if (issue == null || (issue.getComments() > 0 && comments == null))
            list.addHeaderView(loadingView, null, false);

        List<Comment> initialComments = comments;
        if (initialComments == null)
            initialComments = Collections.emptyList();
        list.setAdapter(new ViewHoldingListAdapter<Comment>(initialComments, ViewInflator.viewInflatorFor(
                getActivity(), layout.comment_view_item), ReflectiveHolderFactory.reflectiveFactoryFor(
                CommentViewHolder.class, avatarHelper)));

        if (issue != null && comments != null)
            updateList(issue, comments);

        refreshIssue();
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

        headerView.findViewById(id.ll_milestone).setOnClickListener(new OnClickListener() {

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

        headerView.findViewById(id.v_labels).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    labelsTask.prompt(issue.getLabels());
            }
        });

        headerView.findViewById(id.ll_state).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (issue != null)
                    stateTask.confirm(STATE_OPEN.equals(issue.getState()));
            }
        });

        headerHolder = new IssueHeaderViewHolder(headerView, avatarHelper);
        loadingView = inflater.inflate(layout.load_item, null);
    }

    private void refreshIssue() {
        new AuthenticatedUserTask<FullIssue>(getActivity()) {

            public FullIssue run() throws Exception {
                Issue issue = store.refreshIssue(repositoryId, issueNumber);
                List<Comment> comments;
                if (issue.getComments() > 0)
                    comments = service.get(getContext()).getComments(repositoryId, issueNumber);
                else
                    comments = Collections.emptyList();
                for (Comment comment : comments)
                    comment.setBodyHtml(HtmlFormatter.format(comment.getBodyHtml()).toString());
                return new FullIssue(issue, comments);
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Issue failed to load", e);

                ErrorHelper.show(getContext().getApplicationContext(), e, string.error_issue_load);
            }

            protected void onSuccess(FullIssue fullIssue) throws Exception {
                issue = fullIssue.getIssue();
                comments = fullIssue;
                getArguments().putSerializable(EXTRA_COMMENTS, fullIssue);
                updateList(fullIssue.getIssue(), fullIssue);
            }

            protected void onFinally() throws RuntimeException {
                refreshAnimation.stop();
            }
        }.execute();
    }

    private void updateList(Issue issue, List<Comment> comments) {
        list.removeHeaderView(loadingView);
        headerView.setVisibility(VISIBLE);
        headerHolder.updateViewFor(issue);

        ViewHoldingListAdapter<Comment> adapter = getRootAdapter();
        if (adapter != null)
            adapter.setList(comments);
    }

    @SuppressWarnings("unchecked")
    private ViewHoldingListAdapter<Comment> getRootAdapter() {
        ListAdapter adapter = list.getAdapter();
        if (adapter == null)
            return null;
        adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        if (adapter instanceof ViewHoldingListAdapter<?>)
            return (ViewHoldingListAdapter<Comment>) adapter;
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
            assigneeTask.edit(arguments.getString(SingleChoiceDialogFragment.ARG_SELECTED));
            break;
        case ISSUE_LABELS_UPDATE:
            labelsTask.edit(arguments.getStringArray(MultiChoiceDialogFragment.ARG_SELECTED));
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

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Exception creating comment", e);

                Toast.makeText(getContext().getApplicationContext(), e.getMessage(), LENGTH_LONG).show();
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
            refreshAnimation.setRefreshItem(item).start(getActivity());
            refreshIssue();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}