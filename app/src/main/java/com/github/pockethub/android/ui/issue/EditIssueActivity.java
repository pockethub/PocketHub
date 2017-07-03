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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.rx.RxProgress;
import com.github.pockethub.android.util.ImageBinPoster;
import com.github.pockethub.android.util.PermissionsUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import retrofit2.Response;

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

    private static final int REQUEST_CODE_SELECT_PHOTO = 0;
    private static final int READ_PERMISSION_REQUEST = 1;

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
        if (user != null) {
            builder.add(EXTRA_USER, user);
        }
        builder.add(EXTRA_REPOSITORY_NAME, repositoryName);
        builder.add(EXTRA_REPOSITORY_OWNER, repositoryOwner);
        if (issue != null) {
            builder.issue(issue);
        }
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

    private FloatingActionButton addImageFab;

    @Inject
    private AvatarLoader avatars;

    private Issue issue;

    private Repository repository;

    private MenuItem saveItem;

    private MilestoneDialog milestoneDialog;

    private AssigneeDialog assigneeDialog;

    private LabelsDialog labelsDialog;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_issue_edit);

        titleText = (EditText) findViewById(R.id.et_issue_title);
        bodyText = (EditText) findViewById(R.id.et_issue_body);
        milestoneGraph = findViewById(R.id.ll_milestone_graph);
        milestoneText = (TextView) findViewById(R.id.tv_milestone);
        milestoneClosed = findViewById(R.id.v_closed);
        assigneeAvatar = (ImageView) findViewById(R.id.iv_assignee_avatar);
        assigneeText = (TextView) findViewById(R.id.tv_assignee_name);
        labelsText = (TextView) findViewById(R.id.tv_labels);
        addImageFab = (FloatingActionButton) findViewById(R.id.fab_add_image);

        Intent intent = getIntent();

        if (savedInstanceState != null) {
            issue = savedInstanceState.getParcelable(EXTRA_ISSUE);
        }
        if (issue == null) {
            issue = intent.getParcelableExtra(EXTRA_ISSUE);
        }
        if (issue == null) {
            issue = Issue.builder().build();
        }

        repository = InfoUtils.createRepoFromData(
            intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
            intent.getStringExtra(EXTRA_REPOSITORY_NAME));

        checkCollaboratorStatus();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (issue.number() != null && issue.number() > 0) {
            if (IssueUtils.isPullRequest(issue)) {
                actionBar.setTitle(getString(R.string.pull_request_title)
                        + issue.number());
            } else {
                actionBar.setTitle(getString(R.string.issue_title)
                        + issue.number());
            }
        } else {
            actionBar.setTitle(R.string.new_issue);
        }
        actionBar.setSubtitle(InfoUtils.createRepoId(repository));
        avatars.bind(actionBar, (User) intent.getParcelableExtra(EXTRA_USER));

        titleText.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                updateSaveMenu(s);
            }
        });

        // @TargetApi(…) required to ensure build passes
        // noinspection Convert2Lambda
        addImageFab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Activity activity = EditIssueActivity.this;
                    String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

                    if (ContextCompat.checkSelfPermission(activity, permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        PermissionsUtils.askForPermission(activity, READ_PERMISSION_REQUEST,
                                permission, R.string.read_permission_title,
                                R.string.read_permission_content);
                    } else {
                        startImagePicker();
                    }
                } else {
                    startImagePicker();
                }
            }
        });

        updateSaveMenu();
        titleText.setText(issue.title());
        bodyText.setText(issue.body());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PERMISSION_REQUEST) {

            boolean result = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }

            if (result) {
                startImagePicker();
            }
        }
    }

    private void startImagePicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (RESULT_OK != resultCode) {
            return;
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            progressDialog = new MaterialDialog.Builder(this)
                    .content(R.string.loading)
                    .progress(true, 0)
                    .build();
            progressDialog.show();
            ImageBinPoster.post(this, data.getData(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    progressDialog.dismiss();
                    showImageError();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        insertImage(ImageBinPoster.getUrl(response.body().string()));
                    } else {
                        showImageError();
                    }
                }
            });
        }
    }

    private void showImageError() {
        ToastUtils.show(this, R.string.error_image_upload);
    }

    private void insertImage(final String url) {
        runOnUiThread(() -> bodyText.append("![](" + url + ")"));
    }

    private void showMainContent() {
        findViewById(R.id.sv_issue_content).setVisibility(View.VISIBLE);
        findViewById(R.id.pb_loading).setVisibility(View.GONE);
    }

    private void showCollaboratorOptions() {
        View milestone = findViewById(R.id.ll_milestone);
        View labels = findViewById(R.id.ll_labels);
        View assignee = findViewById(R.id.ll_assignee);

        findViewById(R.id.tv_milestone_label).setVisibility(View.VISIBLE);
        milestone.setVisibility(View.VISIBLE);
        findViewById(R.id.tv_labels_label).setVisibility(View.VISIBLE);
        labels.setVisibility(View.VISIBLE);
        findViewById(R.id.tv_assignee_label).setVisibility(View.VISIBLE);
        assignee.setVisibility(View.VISIBLE);

        milestone.setOnClickListener(v -> {
            if (milestoneDialog == null) {
                milestoneDialog = new MilestoneDialog(this, ISSUE_MILESTONE_UPDATE, repository);
            }
            milestoneDialog.show(issue.milestone());
        });

        assignee.setOnClickListener(v -> {
            if (assigneeDialog == null) {
                assigneeDialog = new AssigneeDialog(this, ISSUE_ASSIGNEE_UPDATE, repository);
            }
            assigneeDialog.show(issue.assignee());
        });

        labels.setOnClickListener(v -> {
            if (labelsDialog == null) {
                labelsDialog = new LabelsDialog(this, ISSUE_LABELS_UPDATE, repository);
            }
            labelsDialog.show(issue.labels());
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
            } else {
                milestoneClosed.setVisibility(GONE);
            }
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
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(labelsText, labels);
        } else {
            labelsText.setText(R.string.none);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_ISSUE, issue);
    }

    private void updateSaveMenu() {
        if (titleText != null) {
            updateSaveMenu(titleText.getText());
        }
    }

    private void updateSaveMenu(final CharSequence text) {
        if (saveItem != null) {
            saveItem.setEnabled(!TextUtils.isEmpty(text));
        }
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

                if (issue.assignee() != null) {
                    request.assignees(Collections.singletonList(issue.assignee().login()));
                } else {
                    request.assignees(Collections.emptyList());
                }

                if (issue.milestone() != null) {
                    request.milestone(issue.milestone().number());
                }

                List<String> labels = new ArrayList<>();
                if (issue.labels() != null) {
                    for (Label label : issue.labels()) {
                        labels.add(label.name());
                    }
                }
                request.labels(labels);

                IssueService service = ServiceGenerator.createService(this, IssueService.class);
                Single<Response<Issue>> single;
                int message;

                if (issue.number() != null && issue.number() > 0) {
                    single = service.editIssue(repository.owner().login(), repository.name(), issue.number(), request.build());
                    message = R.string.updating_issue;
                } else {
                    single =  service.createIssue(repository.owner().login(), repository.name(), request.build());
                    message = R.string.creating_issue;
                }

                single.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(this.bindToLifecycle())
                        .compose(RxProgress.bindToLifecycle(this, message))
                        .subscribe(response -> {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_ISSUE, response.body());
                            setResult(RESULT_OK, intent);
                            finish();
                        }, e -> {
                            Log.e(TAG, "Exception creating issue", e);
                            ToastUtils.show(this, e.getMessage());
                        });
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
                .compose(this.bindToLifecycle())
                .subscribe(response -> {
                    showMainContent();
                    if (response.code() == 204) {
                        showCollaboratorOptions();
                    }
                }, e -> {
                    /*if(e instanceof RetrofitError && ((RetrofitError) e).getResponse().getStatus() == 403){
                        //403 -> Forbidden
                        //The user is not a collaborator.
                        showMainContent();
                    }*/
                });
    }
}
