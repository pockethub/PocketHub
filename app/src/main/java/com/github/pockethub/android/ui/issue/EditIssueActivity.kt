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
package com.github.pockethub.android.ui.issue

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.github.pockethub.android.Intents.Builder
import com.github.pockethub.android.Intents.EXTRA_ISSUE
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY_NAME
import com.github.pockethub.android.Intents.EXTRA_REPOSITORY_OWNER
import com.github.pockethub.android.Intents.EXTRA_USER
import com.github.pockethub.android.R
import com.github.pockethub.android.RequestCodes.ISSUE_ASSIGNEE_UPDATE
import com.github.pockethub.android.RequestCodes.ISSUE_LABELS_UPDATE
import com.github.pockethub.android.RequestCodes.ISSUE_MILESTONE_UPDATE
import com.github.pockethub.android.accounts.AccountUtils
import com.github.pockethub.android.core.issue.IssueUtils
import com.github.pockethub.android.rx.AutoDisposeUtils
import com.github.pockethub.android.rx.RxProgress
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.ui.TextWatcherAdapter
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.ImageBinPoster
import com.github.pockethub.android.util.InfoUtils
import com.github.pockethub.android.util.PermissionsUtils
import com.github.pockethub.android.util.ToastUtils
import com.meisolsson.githubsdk.core.ServiceGenerator
import com.meisolsson.githubsdk.model.Issue
import com.meisolsson.githubsdk.model.Repository
import com.meisolsson.githubsdk.model.User
import com.meisolsson.githubsdk.model.request.issue.IssueRequest
import com.meisolsson.githubsdk.service.issues.IssueService
import com.meisolsson.githubsdk.service.repositories.RepositoryCollaboratorService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_issue_edit.*
import kotlinx.android.synthetic.main.milestone.*
import retrofit2.Response
import java.util.ArrayList
import javax.inject.Inject

/**
 * Activity to edit or create an issue
 */
class EditIssueActivity : BaseActivity() {

    @Inject
    lateinit var avatars: AvatarLoader

    private var issue: Issue? = null

    private var repository: Repository? = null

    private var saveItem: MenuItem? = null

    private var milestoneDialog: MilestoneDialog? = null

    private var assigneeDialog: AssigneeDialog? = null

    private var labelsDialog: LabelsDialog? = null

    private val titleTextWatcher = object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateSaveMenu(s)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_edit)

        val intent = intent

        if (savedInstanceState != null) {
            issue = savedInstanceState.getParcelable(EXTRA_ISSUE)
        }
        if (issue == null) {
            issue = intent.getParcelableExtra(EXTRA_ISSUE)
        }
        if (issue == null) {
            issue = Issue.builder().build()
        }

        repository = InfoUtils.createRepoFromData(
                intent.getStringExtra(EXTRA_REPOSITORY_OWNER),
                intent.getStringExtra(EXTRA_REPOSITORY_NAME)
        )

        checkCollaboratorStatus()

        val actionBar = supportActionBar
        if (issue!!.number() != null && issue!!.number()!! > 0) {
            if (IssueUtils.isPullRequest(issue)) {
                actionBar!!.title = getString(R.string.pull_request_title) + issue!!.number()!!
            } else {
                actionBar!!.title = getString(R.string.issue_title) + issue!!.number()!!
            }
        } else {
            actionBar!!.setTitle(R.string.new_issue)
        }
        actionBar.subtitle = InfoUtils.createRepoId(repository)
        avatars.bind(actionBar, intent.getParcelableExtra<Parcelable>(EXTRA_USER) as User)

        updateSaveMenu()
        et_issue_title.setText(issue!!.title())
        et_issue_body.setText(issue!!.body())

        fab_add_image.setOnClickListener { onAddImageClicked() }
        et_issue_title.addTextChangedListener(titleTextWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()
        et_issue_title.removeTextChangedListener(titleTextWatcher)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PERMISSION_REQUEST) {

            var result = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    result = false
                }
            }

            if (result) {
                startImagePicker()
            }
        }
    }

    private fun startImagePicker() {
        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (Activity.RESULT_OK != resultCode) {
            return
        }

        when (requestCode) {
            ISSUE_MILESTONE_UPDATE -> {
                issue = issue!!.toBuilder()
                        .milestone(MilestoneDialogFragment.getSelected(arguments))
                        .build()
                updateMilestone()
            }
            ISSUE_ASSIGNEE_UPDATE -> {
                var assignee: User? = AssigneeDialogFragment.getSelected(arguments)
                if (assignee == null) {
                    assignee = User.builder().login("").build()
                }
                issue = issue!!.toBuilder().assignee(assignee).build()
                updateAssignee()
            }
            ISSUE_LABELS_UPDATE -> {
                issue = issue!!.toBuilder()
                        .labels(LabelsDialogFragment.getSelected(arguments))
                        .build()
                updateLabels()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            ImageBinPoster.post(this, data?.data!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(RxProgress.bindToLifecycle(this, R.string.loading))
                    .`as`(AutoDisposeUtils.bindToLifecycle(this))
                    .subscribe({ response ->
                        if (response.isSuccessful) {
                            insertImage(ImageBinPoster.getUrl(response.body()!!.string()))
                        } else {
                            showImageError()
                        }
                    }, { _ -> showImageError() })
        }
    }

    fun onAddImageClicked() {
        val activity = this@EditIssueActivity
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(activity, permission) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            PermissionsUtils.askForPermission(
                    activity,
                    READ_PERMISSION_REQUEST,
                    permission,
                    R.string.read_permission_title,
                    R.string.read_permission_content
            )
        } else {
            startImagePicker()
        }
    }

    private fun showImageError() {
        ToastUtils.show(this, R.string.error_image_upload)
    }

    private fun insertImage(url: String?) {
        runOnUiThread { et_issue_body.append("![]($url)") }
    }

    private fun showMainContent() {
        findViewById<View>(R.id.sv_issue_content).visibility = View.VISIBLE
        findViewById<View>(R.id.pb_loading).visibility = View.GONE
    }

    private fun showCollaboratorOptions() {
        val milestone = findViewById<View>(R.id.ll_milestone)
        val labels = findViewById<View>(R.id.ll_labels)
        val assignee = findViewById<View>(R.id.ll_assignee)

        findViewById<View>(R.id.tv_milestone_label).visibility = View.VISIBLE
        milestone.visibility = View.VISIBLE
        findViewById<View>(R.id.tv_labels_label).visibility = View.VISIBLE
        labels.visibility = View.VISIBLE
        findViewById<View>(R.id.tv_assignee_label).visibility = View.VISIBLE
        assignee.visibility = View.VISIBLE

        milestone.setOnClickListener { _ ->
            if (milestoneDialog == null) {
                milestoneDialog = MilestoneDialog(this, ISSUE_MILESTONE_UPDATE, repository)
            }
            milestoneDialog!!.show(issue!!.milestone())
        }

        assignee.setOnClickListener { _ ->
            if (assigneeDialog == null) {
                assigneeDialog = AssigneeDialog(this, ISSUE_ASSIGNEE_UPDATE, repository)
            }
            assigneeDialog!!.show(issue!!.assignee())
        }

        labels.setOnClickListener { _ ->
            if (labelsDialog == null) {
                labelsDialog = LabelsDialog(this, ISSUE_LABELS_UPDATE, repository)
            }
            labelsDialog!!.show(issue!!.labels())
        }

        updateAssignee()
        updateLabels()
        updateMilestone()
    }

    private fun updateMilestone() {
        val milestone = issue!!.milestone()
        if (milestone != null) {
            tv_milestone.text = milestone.title()
            val closed = milestone.closedIssues()!!.toFloat()
            val total = closed + milestone.openIssues()!!
            if (total > 0) {
                (v_closed.layoutParams as LayoutParams).weight = closed / total
                v_closed.visibility = VISIBLE
            } else {
                v_closed.visibility = GONE
            }
            ll_milestone_graph.visibility = VISIBLE
        } else {
            tv_milestone.setText(R.string.none)
            ll_milestone_graph.visibility = GONE
        }
    }

    private fun updateAssignee() {
        val assignee = issue!!.assignee()
        val login = assignee?.login()
        if (!TextUtils.isEmpty(login)) {
            tv_assignee_name.text = buildSpannedString {
                bold {
                    append(login)
                }
            }
            iv_assignee_avatar.visibility = VISIBLE
            avatars.bind(iv_assignee_avatar, assignee)
        } else {
            iv_assignee_avatar.visibility = GONE
            tv_assignee_name.setText(R.string.unassigned)
        }
    }

    private fun updateLabels() {
        val labels = issue!!.labels()
        if (labels != null && !labels.isEmpty()) {
            LabelDrawableSpan.setText(tv_labels, labels)
        } else {
            tv_labels.setText(R.string.none)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState!!.putParcelable(EXTRA_ISSUE, issue)
    }

    private fun updateSaveMenu(text: CharSequence = et_issue_title.text) {
        saveItem?.isEnabled = text.isNotEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_issue_edit, menu)
        saveItem = menu.findItem(R.id.m_apply)
        updateSaveMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_apply -> {
                val request = IssueRequest.builder()
                        .body(et_issue_body.text.toString())
                        .title(et_issue_title.text.toString())
                        .state(issue!!.state())

                if (issue!!.assignee() != null) {
                    request.assignees(listOf(issue!!.assignee()!!.login()!!))
                } else {
                    request.assignees(emptyList())
                }

                if (issue!!.milestone() != null) {
                    request.milestone(issue!!.milestone()!!.number()!!.toLong())
                }

                val labels = ArrayList<String>()
                if (issue!!.labels() != null) {
                    for (label in issue!!.labels()!!) {
                        labels.add(label.name()!!)
                    }
                }
                request.labels(labels)

                val service = ServiceGenerator.createService(this, IssueService::class.java)
                val single: Single<Response<Issue>>
                val message: Int

                if (issue!!.number() != null && issue!!.number()!! > 0) {
                    single = service.editIssue(
                            repository!!.owner()!!.login(),
                            repository!!.name(),
                            issue!!.number()!!.toLong(),
                            request.build()
                    )
                    message = R.string.updating_issue
                } else {
                    single = service.createIssue(
                            repository!!.owner()!!.login(),
                            repository!!.name(),
                            request.build()
                    )
                    message = R.string.creating_issue
                }

                single.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(RxProgress.bindToLifecycle(this, message))
                        .`as`(AutoDisposeUtils.bindToLifecycle(this))
                        .subscribe({ response ->
                            val intent = Intent()
                            intent.putExtra(EXTRA_ISSUE, response.body())
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }, { e ->
                            Log.e(TAG, "Exception creating issue", e)
                            ToastUtils.show(this, e.message)
                        })
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun checkCollaboratorStatus() {
        ServiceGenerator.createService(this, RepositoryCollaboratorService::class.java)
                .isUserCollaborator(
                        repository!!.owner()!!.login(),
                        repository!!.name(),
                        AccountUtils.getLogin(this)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDisposeUtils.bindToLifecycle(this))
                .subscribe({ response ->
                    showMainContent()
                    if (response.code() == 204) {
                        showCollaboratorOptions()
                    }
                }, { e ->
                    showMainContent()
                    Log.e(TAG, "Exception creating issue", e)
                    ToastUtils.show(this, e.message)
                })
    }

    companion object {

        private const val TAG = "EditIssueActivity"

        private const val REQUEST_CODE_SELECT_PHOTO = 0
        private const val READ_PERMISSION_REQUEST = 1

        /**
         * Create intent to create an issue
         *
         * @param repository
         * @return intent
         */
        fun createIntent(repository: Repository?): Intent {
            return createIntent(
                    null,
                    repository!!.owner()!!.login(),
                    repository.name(),
                    repository.owner()
            )
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
        fun createIntent(
                issue: Issue?,
                repositoryOwner: String?,
                repositoryName: String?,
                user: User?
        ): Intent {
            val builder = Builder("repo.issues.edit.VIEW")
            if (user != null) {
                builder.add(EXTRA_USER, user)
            }
            builder.add(EXTRA_REPOSITORY_NAME, repositoryName)
            builder.add(EXTRA_REPOSITORY_OWNER, repositoryOwner)
            if (issue != null) {
                builder.issue(issue)
            }
            return builder.toIntent()
        }
    }
}
