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
package com.github.mobile.core.issue;

import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUES;
import static org.eclipse.egit.github.core.event.Event.TYPE_ISSUE_COMMENT;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.event.EventPayload;
import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.eclipse.egit.github.core.event.IssuesPayload;

/**
 * Helper to find an issue to open for an event
 */
public class IssueEventMatcher {

    /**
     * Get issue from event
     *
     * @param event
     * @return issue or null if event doesn't apply
     */
    public Issue getIssue(Event event) {
        if (event == null)
            return null;
        EventPayload payload = event.getPayload();
        if (payload == null)
            return null;
        String type = event.getType();
        if (TYPE_ISSUES.equals(type))
            return ((IssuesPayload) payload).getIssue();
        else if (TYPE_ISSUE_COMMENT.equals(type))
            return ((IssueCommentPayload) payload).getIssue();
        else
            return null;
    }
}
