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
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;
import com.github.pockethub.android.Intents.Builder;
import com.github.pockethub.android.R;
import com.github.pockethub.android.accounts.AccountUtils;
import com.github.pockethub.android.core.issue.IssueUtils;
import com.github.pockethub.android.rx.ObserverAdapter;
import com.github.pockethub.android.rx.ProgressObserverAdapter;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.ui.TextWatcherAdapter;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.InfoUtils;
import com.github.pockethub.android.util.ToastUtils;
import com.meisolsson.githubsdk.model.request.issue.IssueRequest;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCollaboratorService;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.pockethub.android.Intents.EXTRA_ISSUE;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY_NAME;
import static com.github.pockethub.android.Intents.EXTRA_REPOSITORY_OWNER;
import static com.github.pockethub.android.Intents.EXTRA_USER;
import static com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_LABELS_UPDATE;
import static com.github.pockethub.android.RequestCodes.ISSUE_MILESTONE_UPDATE;

/**
 * Activity to edit or create an issue
 */
public class EditIssueActivity extends BaseActivity {

    private static final String TAG = "EditIssueActivity";

    /**
     * Create intent to create an issue
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return createIntent(null, repository.owner().login(),
            repository.name(), repository.owner());
    }

    /**
     * Create intent to edit an issue
     *
     * @param issue
     * @param repositoryOwner
     * @param repositoryName
     * @param user
     * @return intent
     */
    public static Intent createIntent(final Issue issue,
        final String repositoryOwner, final String repositoryName,
        final User user) {
        Builder builder = new Builder("repo.issues.edit.VIEW");
        if (user != null)
            builder.add(EXTRA_USER, user);
        builder.add(EXTRA_REPOSITORY_NAME, repositoryName);
        builder.add(EXTRA_REPOSITORY_OWNER, repositoryOwner);
        if (issue != null)
            builder.issue(issue);
        return builder.toIntent();
    }

    private EditText titleText;

    private EditText bodyText;

    private View milestoneGraph;

    private TextView milestoneText;

    private View milestoneClosed;

    private ImageView assigneeAvatar;

    private TextView assigneeText;

    private TextView labelsText;

    @Inject
    private AvatarLoader avatars;

    private Issue issue;

    private Repository repository;

    private MenuItem saveItem;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private LabelsDialog labelsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_issue_edit);

        titleText = finder.find(R.id.et_issue_title);
        bodyText = finder.find(R.id.et_issue_body);
        milestoneGraph = finder.find(R.id.ll_milestone_graph);
        milestoneText = finder.find(R.id.tv_milestone);
        milestoneClosed = finder.find(R.id.v_closed);
        assigneeAvatar = finder.find(R.id.iv_assignee_avatar);
        assigneeText = finder.find(R.id.tv_assignee_name);
        labelsText = finder.find(R.id.tv_labels);

        Intent intent = getIntent();

        if (savedInstanceState != null)
            issue = savedInstanceState.getParcelable(EXTRA_ISSUE);
        if (issue == null)
            issue = intent.getParcelableExtra(EXTRA_ISSUE);
        if (issue == null)
            issue = Issue.builder().build();

        repository = InfoUtils.createRepoFromData(
            intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
            intent.getStringExtra(EXTRA_REPOSITORY_NAME));

        checkCollaboratorStatus();

        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (issue.number() != null && issue.number() > 0)
            if (IssueUtils.isPullRequest(issue))
                actionBar.setTitle(getString(R.string.pull_request_title)
                    + issue.number());
            else
                actionBar.setTitle(getString(R.string.issue_title)
                    + issue.number());
        else
            actionBar.setTitle(R.string.new_issue);
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, (User) intent.getParcelableExtra(EXTRA_USER));

        titleText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                updateSaveMenu(s);
            }
        });

        updateSaveMenu();
        titleText.setText(issue.title());
        bodyText.setText(issue.body());
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode)
            return;

        switch (requestCode) {
            case ISSUE_MILESTONE_UPDATE:
                issue = issue.toBuilder().milestone(MilestoneDialogFragment.getSelected(arguments)).build();
                updateMilestone();
                break;
            case ISSUE_ASSIGNEE_UPDATE:
                User assignee = AssigneeDialogFragment.getSelected(arguments);
                if (assignee == null) {
                    assignee = User.builder().login("").build();
                }
                issue = issue.toBuilder().assignee(assignee).build();
                updateAssignee();
                break;
            case ISSUE_LABELS_UPDATE:
                issue = issue.toBuilder().labels(LabelsDialogFragment.getSelected(arguments)).build();
                updateLabels();
                break;
        }
    }

    private void showMainContent() {
        finder.find(R.id.sv_issue_content).setVisibility(View.VISIBLE);
        finder.find(R.id.pb_loading).setVisibility(View.GONE);
    }

    private void showCollaboratorOptions() {
        finder.find(R.id.tv_milestone_label).setVisibility(View.VISIBLE);
        finder.find(R.id.ll_milestone).setVisibility(View.VISIBLE);
        finder.find(R.id.tv_labels_label).setVisibility(View.VISIBLE);
        finder.find(R.id.ll_labels).setVisibility(View.VISIBLE);
        finder.find(R.id.tv_assignee_label).setVisibility(View.VISIBLE);
        finder.find(R.id.ll_assignee).setVisibility(View.VISIBLE);

        finder.onClick(R.id.ll_milestone, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (milestoneDialog == null)
                    milestoneDialog = new MilestoneDialog(
                        EditIssueActivity.this, ISSUE_MILESTONE_UPDATE,
                        repository);
                milestoneDialog.show(issue.milestone());
            }
        });

        finder.onClick(R.id.ll_assignee, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (assigneeDialog == null)
                    assigneeDialog = new AssigneeDialog(EditIssueActivity.this,
                        ISSUE_ASSIGNEE_UPDATE, repository);
                assigneeDialog.show(issue.assignee());
            }
        });

        finder.onClick(R.id.ll_labels, new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (labelsDialog == null)
                    labelsDialog = new LabelsDialog(EditIssueActivity.this,
                        ISSUE_LABELS_UPDATE, repository);
                labelsDialog.show(issue.labels());
            }
        });

        updateAssignee();
        updateLabels();
        updateMilestone();
    }

    private void updateMilestone() {
        Milestone milestone = issue.milestone();
        if (milestone != null) {
            milestoneText.setText(milestone.title());
            float closed = milestone.closedIssues();
            float total = closed + milestone.openIssues();
            if (total > 0) {
                ((LayoutParams) milestoneClosed.getLayoutParams()).weight = closed
                    / total;
                milestoneClosed.setVisibility(VISIBLE);
            } else
                milestoneClosed.setVisibility(GONE);
            milestoneGraph.setVisibility(VISIBLE);
        } else {
            milestoneText.setText(R.string.none);
            milestoneGraph.setVisibility(GONE);
        }
    }

    private void updateAssignee() {
        User assignee = issue.assignee();
        String login = assignee != null ? assignee.login() : null;
        if (!TextUtils.isEmpty(login)) {
            assigneeText.setText(new StyledText().bold(login));
            assigneeAvatar.setVisibility(VISIBLE);
            avatars.bind(assigneeAvatar, assignee);
        } else {
            assigneeAvatar.setVisibility(GONE);
            assigneeText.setText(R.string.unassigned);
        }
    }

    private void updateLabels() {
        List<Label> labels = issue.labels();
        if (labels != null && !labels.isEmpty())
            LabelDrawableSpan.setText(labelsText, labels);
        else
            labelsText.setText(R.string.none);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_ISSUE, issue);
    }

    private void updateSaveMenu() {
        if (titleText != null)
            updateSaveMenu(titleText.getText());
    }

    private void updateSaveMenu(final CharSequence text) {
        if (saveItem != null)
            saveItem.setEnabled(!TextUtils.isEmpty(text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(R.menu.activity_issue_edit, options);
        saveItem = options.findItem(R.id.m_apply);
        updateSaveMenu();
        return super.onCreateOptionsMenu(options);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.m_apply:
                IssueRequest.Builder request = IssueRequest.builder()
                        .body(bodyText.getText().toString())
                        .title(titleText.getText().toString())
                        .state(issue.state());

                if(issue.assignee() != null)
                    request.assignees(Collections.singletonList(issue.assignee().login()));

                if(issue.milestone() != null)
                    request.milestone(issue.milestone().number());

                if(issue.labels() != null) {
                    List<String> labels = new ArrayList<>();
                    for (Label label : issue.labels())
                        labels.add(label.name());

                    request.labels(labels);
                }

                IssueService service = ServiceGenerator.createService(this, IssueService.class);
                Observable<Issue> observable;
                int message;

                if (issue.number() != null && issue.number() > 0) {
                    observable = service.editIssue(repository.owner().login(), repository.name(), issue.number(), request.build());
                    message = R.string.updating_issue;
                } else {
                    observable =  service.createIssue(repository.owner().login(), repository.name(), request.build());
                    message = R.string.creating_issue;
                }

                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(this.<Issue>bindToLifecycle())
                        .subscribe(new ProgressObserverAdapter<Issue>(this, message) {

                            @Override
                            public void onNext(Issue issue) {
                                super.onNext(issue);
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_ISSUE, issue);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                Log.e(TAG, "Exception creating issue", e);
                                ToastUtils.show(EditIssueActivity.this, e.getMessage());
                            }
                        }.start());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkCollaboratorStatus() {
        ServiceGenerator.createService(this, RepositoryCollaboratorService.class)
                .isUserCollaborator(repository.owner().login(), repository.name(), AccountUtils.getLogin(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Response<Boolean>>bindToLifecycle())
                .subscribe(new ObserverAdapter<Response<Boolean>>() {
                    @Override
                    public void onNext(Response<Boolean> response) {
                        showMainContent();
                        if (response.code() == 204)
                            showCollaboratorOptions();
                    }

                    @Override
                    public void onError(Throwable e) {

                        /*if(e instanceof RetrofitError && ((RetrofitError) e).getResponse().getStatus() == 403){
                            //403 -> Forbidden
                            //The user is not a collaborator.
                            showMainContent();
                        }*/
                    }
                });
    }
}
