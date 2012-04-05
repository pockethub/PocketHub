package com.github.mobile.android;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.IOException;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

/**
 * Provide GitHub-API related services
 */
public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    IssueService issueService(GitHubClient client) {
        return new IssueService(client);
    }

    @Provides
    PullRequestService pullRequestService(GitHubClient client) {
        return new PullRequestService(client);
    }

    @Provides
    UserService userService(GitHubClient client) {
        return new UserService(client);
    }

    @Provides
    GistService gistService(GitHubClient client) {
        return new GistService(client);
    }

    @Provides
    OrganizationService orgService(GitHubClient client) {
        return new OrganizationService(client);
    }

    @Provides
    RepositoryService repoService(GitHubClient client) {
        return new RepositoryService(client);
    }

    @Provides
    User currentUser(UserService userService) throws IOException {
        return userService.getUser(); // actually, probably better to cache this
    }

    @Provides
    CollaboratorService collaboratorService(GitHubClient client) {
        return new CollaboratorService(client);
    }

    @Provides
    MilestoneService milestoneService(GitHubClient client) {
        return new MilestoneService(client);
    }

    @Provides
    LabelService labelService(GitHubClient client) {
        return new LabelService(client);
    }

    @Provides
    EventService eventService(GitHubClient client) {
        return new EventService(client);
    }
}
