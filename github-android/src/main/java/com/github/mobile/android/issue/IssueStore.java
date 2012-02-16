package com.github.mobile.android.issue;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Store of loaded issues
 */
public class IssueStore {

    private static class RepositoryIssues {

        private static class IssueReference extends WeakReference<Issue> {

            private int number;

            /**
             * Create issue reference
             *
             * @param issue
             * @param queue
             */
            public IssueReference(Issue issue, ReferenceQueue<? super Issue> queue) {
                super(issue, queue);
                number = issue.getNumber();
            }
        }

        private final ReferenceQueue<Issue> queue = new ReferenceQueue<Issue>();

        private final Map<Integer, IssueReference> issues = new HashMap<Integer, IssueReference>();

        private void expungeEntries() {
            IssueReference ref;
            while ((ref = (IssueReference) queue.poll()) != null)
                issues.remove(ref.number);
        }

        private Issue get(final int number) {
            expungeEntries();
            WeakReference<Issue> ref = issues.get(number);
            return ref != null ? ref.get() : null;
        }

        private void put(Issue issue) {
            expungeEntries();
            issues.put(issue.getNumber(), new IssueReference(issue, queue));
        }
    }

    private final Map<String, RepositoryIssues> repos = new HashMap<String, RepositoryIssues>();

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
    public Issue getIssue(IRepositoryIdProvider repository, int number) {
        RepositoryIssues repoIssues = repos.get(repository.generateId());
        return repoIssues != null ? repoIssues.get(number) : null;
    }

    /**
     * Add issue to store
     *
     * @param issue
     * @return issue
     */
    public Issue addIssue(Issue issue) {
        RepositoryId repo = RepositoryId.createFromUrl(issue.getHtmlUrl());
        return addIssue(repo, issue);
    }

    /**
     * Add issue to store
     *
     * @param repository
     * @param issue
     * @return issue
     */
    public Issue addIssue(IRepositoryIdProvider repository, Issue issue) {
        Issue current = getIssue(repository, issue.getNumber());
        if (current != null) {
            current.setAssignee(issue.getAssignee());
            current.setBody(issue.getBody());
            current.setBodyHtml(issue.getBodyHtml());
            current.setBodyText(issue.getBodyText());
            current.setClosedAt(issue.getClosedAt());
            current.setComments(issue.getComments());
            current.setLabels(issue.getLabels());
            current.setMilestone(issue.getMilestone());
            current.setPullRequest(issue.getPullRequest());
            current.setState(issue.getState());
            current.setTitle(issue.getTitle());
            current.setUpdatedAt(issue.getUpdatedAt());
            return current;
        } else {
            String repoId = repository.generateId();
            RepositoryIssues repoIssues = repos.get(repoId);
            if (repoIssues == null) {
                repoIssues = new RepositoryIssues();
                repos.put(repoId, repoIssues);
            }
            repoIssues.put(issue);
            return issue;
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
    public Issue refreshIssue(IRepositoryIdProvider repository, int number) throws IOException {
        return addIssue(repository, service.getIssue(repository, number));
    }

    /**
     * Refresh issue
     *
     * @param repository
     * @param issue
     * @return edited issue
     * @throws IOException
     */
    public Issue editIssue(IRepositoryIdProvider repository, Issue issue) throws IOException {
        return addIssue(repository, service.editIssue(repository, issue));
    }
}
