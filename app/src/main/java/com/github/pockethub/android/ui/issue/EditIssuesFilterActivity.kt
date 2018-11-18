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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.widget.RadioGroup
import com.github.pockethub.android.Intents.Builder
import com.github.pockethub.android.Intents.EXTRA_ISSUE_FILTER
import com.github.pockethub.android.R
import com.github.pockethub.android.core.issue.IssueFilter
import com.github.pockethub.android.ui.BaseActivity
import com.github.pockethub.android.util.AvatarLoader
import com.github.pockethub.android.util.InfoUtils
import kotlinx.android.synthetic.main.activity_issues_filter_edit.*
import javax.inject.Inject

/**
 * Activity to create or edit an issues filter for a repository
 */
class EditIssuesFilterActivity : BaseActivity() {

    @Inject
    lateinit var avatars: AvatarLoader

    private var labelsDialog: LabelsDialog? = null

    private var milestoneDialog: MilestoneDialog? = null

    private var assigneeDialog: AssigneeDialog? = null

    private var filter: IssueFilter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issues_filter_edit)

        if (savedInstanceState != null) {
            filter = savedInstanceState.getParcelable(EXTRA_ISSUE_FILTER)
        }

        if (filter == null) {
            filter = intent.getParcelableExtra(EXTRA_ISSUE_FILTER)
        }

        val repository = filter!!.repository

        val actionBar = supportActionBar!!
        actionBar.setTitle(R.string.filter_issues_title)
        actionBar.subtitle = InfoUtils.createRepoId(repository)
        avatars.bind(actionBar, repository.owner()!!)

        updateAssignee()
        updateMilestone()
        updateLabels()

        val status = findViewById<RadioGroup>(R.id.issue_filter_status)
        val sortOrder = findViewById<RadioGroup>(R.id.issue_sort_order)
        val sortType = findViewById<RadioGroup>(R.id.issue_sort_type)

        status.setOnCheckedChangeListener(this::onStatusChanged)
        sortOrder.setOnCheckedChangeListener(this::onSortOrderChanged)
        sortType.setOnCheckedChangeListener(this::onSortTypeChanged)

        tv_assignee.setOnClickListener { onAssigneeClicked() }
        tv_assignee_label.setOnClickListener { onAssigneeClicked() }

        tv_milestone.setOnClickListener { onMilestoneClicked() }
        tv_milestone_label.setOnClickListener { onMilestoneClicked() }

        tv_labels.setOnClickListener { onLabelsClicked() }
        tv_labels_label.setOnClickListener { onLabelsClicked() }

        if (filter!!.isOpen) {
            status.check(R.id.rb_open)
        } else {
            status.check(R.id.rb_closed)
        }


        if (filter!!.direction == IssueFilter.DIRECTION_ASCENDING) {
            sortOrder.check(R.id.rb_asc)
        } else {
            sortOrder.check(R.id.rb_desc)
        }

        when (filter!!.sortType) {
            IssueFilter.SORT_CREATED -> sortType.check(R.id.rb_created)
            IssueFilter.SORT_UPDATED -> sortType.check(R.id.rb_updated)
            IssueFilter.SORT_COMMENTS -> sortType.check(R.id.rb_comments)
        }
    }

    override fun onCreateOptionsMenu(options: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_issue_filter, options)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.m_apply -> {
                val intent = Intent()
                intent.putExtra(EXTRA_ISSUE_FILTER, filter)
                setResult(RESULT_OK, intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(EXTRA_ISSUE_FILTER, filter)
    }

    fun onAssigneeClicked() {
        if (assigneeDialog == null) {
            assigneeDialog = AssigneeDialog(this, REQUEST_ASSIGNEE, filter!!.repository)
        }
        assigneeDialog!!.show(filter!!.assignee)
    }

    fun onMilestoneClicked() {
        if (milestoneDialog == null) {
            milestoneDialog = MilestoneDialog(this, REQUEST_MILESTONE, filter!!.repository)
        }
        milestoneDialog!!.show(filter!!.milestone)
    }

    fun onLabelsClicked() {
        if (labelsDialog == null) {
            labelsDialog = LabelsDialog(this, REQUEST_LABELS, filter!!.repository)
        }
        labelsDialog!!.show(filter!!.labels)
    }

    private fun onStatusChanged(radioGroup: RadioGroup, checkedId: Int) {
        filter!!.isOpen = checkedId == R.id.rb_open
    }

    private fun onSortOrderChanged(radioGroup: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.rb_asc) {
            filter!!.direction = IssueFilter.DIRECTION_ASCENDING
        } else {
            filter!!.direction = IssueFilter.DIRECTION_DESCENDING
        }
    }

    private fun onSortTypeChanged(radioGroup: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.rb_created -> filter!!.sortType = IssueFilter.SORT_CREATED
            R.id.rb_updated -> filter!!.sortType = IssueFilter.SORT_UPDATED
            R.id.rb_comments -> filter!!.sortType = IssueFilter.SORT_COMMENTS
            else -> {
            }
        }
    }

    private fun updateLabels() {
        val selected = filter!!.labels
        if (selected != null) {
            LabelDrawableSpan.setText(tv_labels, selected)
        } else {
            tv_labels.setText(R.string.none)
        }
    }

    private fun updateMilestone() {
        val selected = filter!!.milestone
        if (selected != null) {
            tv_milestone.text = selected.title()
        } else {
            tv_milestone.setText(R.string.none)
        }
    }

    private fun updateAssignee() {
        val selected = filter!!.assignee
        if (selected != null) {
            avatars.bind(iv_avatar, selected)
            tv_assignee.text = selected.login()
        } else {
            iv_avatar.visibility = GONE
            tv_assignee.setText(R.string.assignee_anyone)
        }
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        if (RESULT_OK != resultCode) {
            return
        }

        when (requestCode) {
            REQUEST_LABELS -> {
                filter!!.setLabels(LabelsDialogFragment.getSelected(arguments))
                updateLabels()
            }
            REQUEST_MILESTONE -> {
                filter!!.milestone = MilestoneDialogFragment.getSelected(arguments)
                updateMilestone()
            }
            REQUEST_ASSIGNEE -> {
                filter!!.assignee = AssigneeDialogFragment.getSelected(arguments)
                updateAssignee()
            }
        }
    }

    companion object {

        /**
         * Create intent for creating an issue filter for the given repository
         *
         * @param filter
         * @return intent
         */
        fun createIntent(filter: IssueFilter): Intent {
            return Builder("repo.issues.filter.VIEW").add(EXTRA_ISSUE_FILTER,
                filter).toIntent()
        }

        private val REQUEST_LABELS = 1

        private val REQUEST_MILESTONE = 2

        private val REQUEST_ASSIGNEE = 3
    }
}
