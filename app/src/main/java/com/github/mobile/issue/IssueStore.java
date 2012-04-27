package com.github.mobile.issue;

import com.github.mobile.ItemStore;
import com.github.mobile.util.HtmlUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Store of loaded issues
 */
public class IssueStore extends ItemStore {

    private final Map<String, ItemReferences<RepositoryIssue>> repos = new HashMap<String, ItemReferences<RepositoryIssue>>();

    private final IssueService service;

    /**
     * Create issue store
     *
     * @param service
     */
    public IssueStore(final IssueService service) {
        this.service = service;
    }

    /**
     * Get issue
     *
     * @param repository
     * @param number
     * @return issue or null if not in store
     */
    public RepositoryIssue getIssue(IRepositoryIdProvider repository, int number) {
        ItemReferences<RepositoryIssue> repoIssues = repos.get(repository.generateId());
        return repoIssues != null ? repoIssues.get(number) : null;
    }

    /**
     * Add issue to store
     *
     * @param issue
     * @return issue
     */
    public RepositoryIssue addIssue(Issue issue) {
        IRepositoryIdProvider repo = null;
        if (issue instanceof RepositoryIssue)
            repo = ((RepositoryIssue) issue).getRepository();
        if (repo == null)
            repo = RepositoryId.createFromUrl(issue.getHtmlUrl());
        return addIssue(repo, issue);
    }

    private RepositoryIssue createRepositoryIssue(Issue issue) {
        if (issue instanceof RepositoryIssue)
            return (RepositoryIssue) issue;

        RepositoryIssue repoIssue = new RepositoryIssue();
        repoIssue.setAssignee(issue.getAssignee());
        repoIssue.setBody(issue.getBody());
        repoIssue.setBodyHtml(issue.getBodyHtml());
        repoIssue.setBodyText(issue.getBodyText());
        repoIssue.setClosedAt(issue.getClosedAt());
        repoIssue.setComments(issue.getComments());
        repoIssue.setCreatedAt(issue.getCreatedAt());
        repoIssue.setHtmlUrl(issue.getHtmlUrl());
        repoIssue.setId(issue.getId());
        repoIssue.setLabels(issue.getLabels());
        repoIssue.setMilestone(issue.getMilestone());
        repoIssue.setNumber(issue.getNumber());
        repoIssue.setPullRequest(issue.getPullRequest());
        repoIssue.setState(issue.getState());
        repoIssue.setTitle(issue.getTitle());
        repoIssue.setUpdatedAt(issue.getUpdatedAt());
        repoIssue.setUrl(issue.getUrl());
        repoIssue.setUser(issue.getUser());
        return repoIssue;
    }

    /**
     * Add issue to store
     *
     * @param repository
     * @param issue
     * @return issue
     */
    public RepositoryIssue addIssue(IRepositoryIdProvider repository, Issue issue) {
        issue.setBodyHtml(HtmlUtils.format(issue.getBodyHtml()).toString());
        RepositoryIssue current = getIssue(repository, issue.getNumber());
        if (current != null) {
            current.setAssignee(issue.getAssignee());
            current.setBody(issue.getBody());
            current.setBodyHtml(issue.getBodyHtml());
            current.setClosedAt(issue.getClosedAt());
            current.setComments(issue.getComments());
            current.setLabels(issue.getLabels());
            current.setMilestone(issue.getMilestone());
            current.setPullRequest(issue.getPullRequest());
            current.setState(issue.getState());
            current.setTitle(issue.getTitle());
            current.setUpdatedAt(issue.getUpdatedAt());
            if (issue instanceof RepositoryIssue)
                current.setRepository(((RepositoryIssue) issue).getRepository());
            return current;
        } else {
            String repoId = repository.generateId();
            ItemReferences<RepositoryIssue> repoIssues = repos.get(repoId);
            if (repoIssues == null) {
                repoIssues = new ItemReferences<RepositoryIssue>();
                repos.put(repoId, repoIssues);
            }
            RepositoryIssue repoIssue = createRepositoryIssue(issue);
            repoIssues.put(issue.getNumber(), repoIssue);
            return repoIssue;
        }
    }

    /**
     * Refresh issue
     *
     * @param repository
     * @param number
     * @return refreshed issue
     * @throws IOException
     */
    public RepositoryIssue refreshIssue(IRepositoryIdProvider repository, int number) throws IOException {
        return addIssue(repository, service.getIssue(repository, number));
    }

    /**
     * Edit issue
     *
     * @param repository
     * @param issue
     * @return edited issue
     * @throws IOException
     */
    public RepositoryIssue editIssue(IRepositoryIdProvider repository, Issue issue) throws IOException {
        return addIssue(repository, service.editIssue(repository, issue));
    }
}
