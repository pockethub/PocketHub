package com.github.pockethub.util;

import com.alorma.github.sdk.bean.dto.request.CommitCommentRequest;
import com.alorma.github.sdk.bean.dto.request.EditGistRequestDTO;
import com.alorma.github.sdk.bean.dto.request.IssueRequest;
import com.alorma.github.sdk.bean.dto.request.RequestMarkdownDTO;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.Label;

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
