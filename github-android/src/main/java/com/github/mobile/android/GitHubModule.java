package com.github.mobile.android;

import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.List;

import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

/**
 * Main module provide services and clients
 */
public class GitHubModule extends AbstractModule {

    private WeakReference<IssueStore> issues = null;

    @Override
    protected void configure() {
    }

    @Provides
    Account currentAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType(GITHUB_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            return accounts[0]; // at some point, support more than one github
            // account, ie vanilla and fi
        }
        return null;
    }

    private GitHubClient configureClient(GitHubClient client, Account account, AccountManager manager) {
        client.setSerializeNulls(false);
        client.setUserAgent("GitHubAndroid/1.0");
        if (account != null)
            client.setCredentials(account.name, manager.getPassword(account));
        return client;
    }

    @Provides
    GitHubClient client(Account account, AccountManager accountManager) {
        return configureClient(new GitHubClient() {
            protected HttpURLConnection configureRequest(final HttpURLConnection request) {
                super.configureRequest(request);
                request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta.html+json");
                return request;
            }
        }, account, accountManager);
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
    AccountDataManager dataManager(Context context, UserService users, OrganizationService orgs, RepositoryService repos) {
        File cache = new File(context.getFilesDir(), "cache");
        return new AccountDataManager(context, cache, users, orgs, repos);
    }

    @Provides
    AvatarHelper avatarHelper(AccountDataManager cache) {
        return new AvatarHelper(cache);
    }

    @Provides
    IRepositorySearch searchService(Account account, AccountManager accountManager) {
        GitHubClient client = new GitHubClient(IGitHubConstants.HOST_API_V2);
        configureClient(client, account, accountManager);
        final RepositoryService service = new RepositoryService(client);
        return new IRepositorySearch() {

            public List<SearchRepository> search(String query) throws IOException {
                return service.searchRepositories(query);
            }
        };
    }

    @Provides
    IssueStore issueStore(IssueService service) {
        IssueStore store = issues != null ? issues.get() : null;
        if (store == null) {
            store = new IssueStore(service);
            issues = new WeakReference<IssueStore>(store);
        }
        return store;
    }
}
