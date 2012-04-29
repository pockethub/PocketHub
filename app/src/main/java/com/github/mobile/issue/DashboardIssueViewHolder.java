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
package com.github.mobile.issue;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarUtils;
import com.github.mobile.util.TypefaceUtils;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.egit.github.core.Issue;

/**
 * Dashboard issue view holder
 */
public class DashboardIssueViewHolder extends RepoIssueViewHolder {

    private final TextView repoText;

    /**
     * Create dashboard issue view holder
     *
     * @param v
     * @param helper
     * @param numberWidth
     */
    public DashboardIssueViewHolder(View v, AvatarUtils helper, AtomicInteger numberWidth) {
        super(v, helper, numberWidth);
        repoText = (TextView) v.findViewById(id.tv_issue_repo_name);
        TypefaceUtils.setOctocons((TextView) v.findViewById(id.tv_comment_icon));
    }

    @Override
    public void updateViewFor(final Issue issue) {
        super.updateViewFor(issue);

        String[] segments = issue.getUrl().split("/");
        int length = segments.length;
        if (length >= 4)
            repoText.setText(segments[length - 4] + "/" + segments[length - 3]);
        else
            repoText.setText("");
    }
}
