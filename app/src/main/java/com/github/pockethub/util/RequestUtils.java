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

package com.github.pockethub.util;

import com.alorma.github.sdk.bean.dto.request.EditGistRequestDTO;
import com.alorma.github.sdk.bean.dto.request.IssueRequest;
import com.alorma.github.sdk.bean.dto.request.RequestMarkdownDTO;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.Issue;

public class RequestUtils {
    public static EditGistRequestDTO editGist(Gist gist) {
        EditGistRequestDTO editGistRequestDTO = new EditGistRequestDTO();
        editGistRequestDTO.description = gist.description;
        editGistRequestDTO.files = gist.files;
        return editGistRequestDTO;
    }

    public static RequestMarkdownDTO markdown(String raw) {
        RequestMarkdownDTO requestMarkdownDTO = new RequestMarkdownDTO();
        requestMarkdownDTO.text = raw;
        return requestMarkdownDTO;
    }

    public static IssueRequest issueFull(Issue issue, String body, String title) {
        IssueRequest request = new IssueRequest();
        request.body = body;
        request.title = title;
        if (issue.assignee != null) {
            request.assignee = issue.assignee.login;
        }
        if (issue.milestone != null) {
            request.milestone = issue.milestone.number;
        }
        request.state = issue.state;
        int nrLabels = 0;
        if (issue.labels != null) {
            nrLabels = issue.labels.size();
        }
        request.labels = new String[nrLabels];
        for (int i = 0; i < nrLabels; i++) {
            request.labels[i] = issue.labels.get(i).name;
        }
        return request;
    }

}
