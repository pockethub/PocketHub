package com.github.mobile.android;

import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;
import static org.eclipse.egit.github.core.client.IGitHubConstants.HOST_API_V2;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.github.mobile.android.authenticator.GitHubAccount;
import com.github.mobile.android.gist.GistStore;
import com.github.mobile.android.guice.GitHubAccountScope;
import com.github.mobile.android.issue.IssueStore;
import com.github.mobile.android.persistence.AllReposForUserOrOrg;
import com.github.mobile.android.sync.SyncCampaign;
import com.github.mobile.android.util.LateAuthenticatedGitHubClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.madgag.android.lazydrawables.BitmapFileStore;
import com.madgag.android.lazydrawables.ImageResourceStore;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.List;

import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import roboguice.inject.ContextSingleton;

/**
 * Main module provide services and clients
 */
public class GitHubModule extends AbstractModule {

    private WeakReference<IssueStore> issues;

    private WeakReference<GistStore> gists;

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new FactoryModuleBuilder().build(SyncCampaign.Factory.class));
        install(new FactoryModuleBuilder().build(AllReposForUserOrOrg.Factory.class));
        install(GitHubAccountScope.module());
    }

    private GitHubClient configureClient(GitHubClient client) {
        client.setSerializeNulls(false);
        client.setUserAgent("GitHubAndroid/1.0");
        return client;
    }

    @Provides
    GitHubClient client(Provider<GitHubAccount> gitHubAccountProvider) {
        return configureClient(new LateAuthenticatedGitHubClient(gitHubAccountProvider) {
            @Override
            protected HttpURLConnection configureRequest(HttpURLConnection request) {
                super.configureRequest(request);
                request.setRequestProperty(HEADER_ACCEPT, "application/vnd.github.beta.full+json");
                return request;
            }
        });
    }

    @Provides
    @Named("cacheDir")
    File cacheDir(Context context) {
        return new File(context.getFilesDir(), "cache");
    }

    @Provides
    @ContextSingleton
    @Named("gravatarStore")
    ImageResourceStore<String, Bitmap> gravatarStore(Context context) {
        return new BitmapFileStore<String>(new File(context.getCacheDir(), "gravatars"));
    }

    @Provides
    IRepositorySearch searchService(final Provider<GitHubAccount> ghAccountProvider, final Context context) {
        GitHubClient client = configureClient(new LateAuthenticatedGitHubClient(HOST_API_V2, ghAccountProvider));

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

    @Provides
    @Named("accountUsername")
    String accountUsername(final Context context) {
        final Account[] accounts = AccountManager.get(context).getAccountsByType(GITHUB_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0].name : null;
    }
}
