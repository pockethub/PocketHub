package com.github.mobile.android;

import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.github.mobile.android.authenticator.GitHubAccount;
import com.github.mobile.android.gist.GistStore;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.persistence.AllReposForUserOrOrg;
import com.github.mobile.android.util.AvatarHelper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.List;

import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Main module provide services and clients
 */
public class GitHubModule extends AbstractModule {

    private WeakReference<IssueStore> issues;

    private WeakReference<GistStore> gists;

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new FactoryModuleBuilder().build(AllReposForUserOrOrg.Factory.class));
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

    @Provides
    GitHubAccount currentAccount(Account account, AccountManager accountManager) {
        String username = account.name;
        String password = accountManager.getPassword(account);
        return new GitHubAccount(username, password);
    }

    private GitHubClient configureClient(GitHubClient client, GitHubAccount ghAccount) {
        client.setSerializeNulls(false);
        client.setUserAgent("GitHubAndroid/1.0");
        if (ghAccount != null)
            client.setCredentials(ghAccount.username, ghAccount.password);
        return client;
    }

    @Provides
    GitHubClient client(GitHubAccount gitHubAccount) {
        return configureClient(new GitHubClient() {
            protected HttpURLConnection configureRequest(final HttpURLConnection request) {
                super.configureRequest(request);
                request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta.full+json");
                return request;
            }
        }, gitHubAccount);
    }

    @Provides @Named("cacheDir")
    File cacheDir(Context context) {
        return new File(context.getFilesDir(), "cache");
    }

    @Provides
    AvatarHelper avatarHelper(AccountDataManager cache) {
        return new AvatarHelper(cache);
    }

    @Provides
    IRepositorySearch searchService(GitHubAccount ghAccount) {
        GitHubClient client = new GitHubClient(IGitHubConstants.HOST_API_V2);
        configureClient(client, ghAccount);
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

    @Provides
    GistStore gistStore(GistService service) {
        GistStore store = gists != null ? gists.get() : null;
        if (store == null) {
            store = new GistStore(service);
            gists = new WeakReference<GistStore>(store);
        }
        return store;
    }
}
