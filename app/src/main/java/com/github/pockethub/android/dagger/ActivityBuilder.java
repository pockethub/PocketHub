package com.github.pockethub.android.dagger;

import com.github.pockethub.android.accounts.LoginActivity;
import com.github.pockethub.android.accounts.LoginWebViewActivity;
import com.github.pockethub.android.dagger.MainFragmentProvider;
import com.github.pockethub.android.ui.MainActivity;
import com.github.pockethub.android.ui.commit.CommitCompareViewActivity;
import com.github.pockethub.android.ui.commit.CommitFileViewActivity;
import com.github.pockethub.android.ui.commit.CommitViewActivity;
import com.github.pockethub.android.ui.gist.CreateGistActivity;
import com.github.pockethub.android.ui.gist.GistFilesViewActivity;
import com.github.pockethub.android.ui.gist.GistsPagerFragment;
import com.github.pockethub.android.ui.gist.GistsViewActivity;
import com.github.pockethub.android.ui.issue.EditIssueActivity;
import com.github.pockethub.android.ui.issue.EditIssuesFilterActivity;
import com.github.pockethub.android.ui.issue.FiltersViewActivity;
import com.github.pockethub.android.ui.issue.IssueBrowseActivity;
import com.github.pockethub.android.ui.issue.IssueSearchActivity;
import com.github.pockethub.android.ui.issue.IssuesViewActivity;
import com.github.pockethub.android.ui.notification.NotificationActivity;
import com.github.pockethub.android.ui.ref.BranchFileViewActivity;
import com.github.pockethub.android.ui.repo.RepositoryContributorsActivity;
import com.github.pockethub.android.ui.repo.RepositoryViewActivity;
import com.github.pockethub.android.ui.search.SearchActivity;
import com.github.pockethub.android.ui.user.UriLauncherActivity;
import com.github.pockethub.android.ui.user.UserViewActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface ActivityBuilder {

    @ContributesAndroidInjector(modules = MainFragmentProvider.class)
    MainActivity mainActivity();

    @ContributesAndroidInjector(modules = RepositoryViewFragmentProvider.class)
    RepositoryViewActivity repositoryViewActivity();

    @ContributesAndroidInjector(modules = IssuesViewFragmentProvider.class)
    IssuesViewActivity issuesViewActivity();

    @ContributesAndroidInjector(modules = NotificationFragmentProvider.class)
    NotificationActivity notificationActivity();

    @ContributesAndroidInjector
    CreateGistActivity createGistActivity();

    @ContributesAndroidInjector(modules = IssueBrowseFragmentProvider.class)
    IssueBrowseActivity issueBrowseActivity();

    @ContributesAndroidInjector
    EditIssuesFilterActivity editIssuesFilterActivity();

    @ContributesAndroidInjector
    EditIssueActivity editIssueActivity();

    @ContributesAndroidInjector(modules = SearchActivityFragmentProvider.class)
    SearchActivity searchActivity();

    @ContributesAndroidInjector(modules = FiltersViewFragmentProvider.class)
    FiltersViewActivity filtersViewActivity();

    @ContributesAndroidInjector(modules = GistsViewFragmentProvider.class)
    GistsViewActivity gistsViewActivity();

    @ContributesAndroidInjector(modules = GistFilesViewFragmentProvider.class)
    GistFilesViewActivity gistFilesViewActivity();

    @ContributesAndroidInjector(modules = CreateCommentFragmentProvider.class)
    com.github.pockethub.android.ui.gist.CreateCommentActivity createGistCommentActivity();

    @ContributesAndroidInjector(modules = CreateCommentFragmentProvider.class)
    com.github.pockethub.android.ui.issue.CreateCommentActivity createIssueCommentActivity();

    @ContributesAndroidInjector(modules = CreateCommentFragmentProvider.class)
    com.github.pockethub.android.ui.commit.CreateCommentActivity createCommitCommentActivity();

    @ContributesAndroidInjector(modules = CreateCommentFragmentProvider.class)
    com.github.pockethub.android.ui.gist.EditCommentActivity editGistCommentActivity();

    @ContributesAndroidInjector(modules = CreateCommentFragmentProvider.class)
    com.github.pockethub.android.ui.issue.EditCommentActivity editIssueCommentActivity();

    @ContributesAndroidInjector(modules = RepositoryContributorsFragmentProvider.class)
    RepositoryContributorsActivity repositoryContributorsActivity();

    @ContributesAndroidInjector(modules = UserViewFragmentProvider.class)
    UserViewActivity userViewActivity();

    @ContributesAndroidInjector
    LoginActivity loginActivity();

    @ContributesAndroidInjector
    UriLauncherActivity uriLauncherActivity();

    @ContributesAndroidInjector(modules = IssueSearchFragmentProvider.class)
    IssueSearchActivity issueSearchActivity();

    @ContributesAndroidInjector(modules = CommitCompareViewFragmentProvider.class)
    CommitCompareViewActivity commitCompareViewActivity();

    @ContributesAndroidInjector(modules = CommitViewFragmentProvider.class)
    CommitViewActivity commitViewActivity();

    @ContributesAndroidInjector
    CommitFileViewActivity commitFileViewActivity();

    @ContributesAndroidInjector
    BranchFileViewActivity branchFileViewActivity();

    @ContributesAndroidInjector
    LoginWebViewActivity loginWebViewActivity();
}