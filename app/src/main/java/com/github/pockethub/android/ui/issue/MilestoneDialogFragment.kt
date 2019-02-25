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

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.github.pockethub.android.R
import com.github.pockethub.android.ui.base.BaseActivity
import com.github.pockethub.android.ui.SingleChoiceDialogFragment
import com.github.pockethub.android.ui.item.dialog.MilestoneDialogItem
import com.meisolsson.githubsdk.model.Milestone
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

/**
 * Dialog fragment to select an issue milestone
 */
class MilestoneDialogFragment : SingleChoiceDialogFragment() {

    companion object {

        /**
         * Get selected milestone from results bundle
         */
        @JvmStatic
        fun getSelected(arguments: Bundle): Milestone {
            return arguments.getParcelable(SingleChoiceDialogFragment.ARG_SELECTED)
        }

        /**
         * Confirm message and deliver callback to given activity
         */
        @JvmStatic
        fun show(activity: BaseActivity, requestCode: Int, title: String, message: String?, choices: ArrayList<Milestone>, selectedChoice: Int) {
            SingleChoiceDialogFragment.show(activity, requestCode, title, message, choices, selectedChoice, MilestoneDialogFragment())
        }
    }

    private val choices: List<Milestone>
        get() = arguments!!.getParcelableArrayList(SingleChoiceDialogFragment.ARG_CHOICES)!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val selected = arguments!!.getInt(SingleChoiceDialogFragment.ARG_SELECTED_CHOICE)

        val adapter = GroupAdapter<ViewHolder>()
        val items = choices.map { ref -> MilestoneDialogItem(ref, selected) }
        adapter.addAll(items)
        adapter.setOnItemClickListener(this)

        return createDialogBuilder()
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .neutralText(R.string.clear)
                .onNeutral { _, _ -> onResult(RESULT_OK) }
                .build()
    }

    override fun onItemClick(item: Item<*>, view: View) {
        super.onItemClick(item, view)
        if (item is MilestoneDialogItem) {
            arguments!!.putParcelable(SingleChoiceDialogFragment.ARG_SELECTED, item.milestone)
            onResult(RESULT_OK)
        }
    }
}
