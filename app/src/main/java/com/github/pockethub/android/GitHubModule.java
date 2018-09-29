package com.github.pockethub.android;

import android.content.Context;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.service.activity.NotificationService;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;
import com.meisolsson.githubsdk.service.search.SearchService;
import com.meisolsson.githubsdk.service.users.UserService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class GitHubModule {

    @Provides
    @Singleton
    RepositoryCommitService providesRepositoryCommitService(Context context) {
        return ServiceGenerator.createService(context, RepositoryCommitService.class);
    }

    @Provides
    @Singleton
    UserService providesUserService(Context context) {
        return ServiceGenerator.createService(context, UserService.class);
    }

    @Provides
    @Singleton
    RepositoryCommentService providesRepositoryCommentService(Context context) {
        return ServiceGenerator.createService(context, RepositoryCommentService.class);
    }

    @Provides
    @Singleton
    GistService providesGistService(Context context) {
        return ServiceGenerator.createService(context, GistService.class);
    }

    @Provides
    @Singleton
    IssueService providesIssueService(Context context) {
        return ServiceGenerator.createService(context, IssueService.class);
    }

    @Provides
    @Singleton
    RepositoryService providesRepositoryService(Context context) {
        return ServiceGenerator.createService(context, RepositoryService.class);
    }

    @Provides
    @Singleton
    NotificationService providesNotificationService(Context context) {
        return ServiceGenerator.createService(context, NotificationService.class);
    }

    @Provides
    @Singleton
    SearchService providesSearchService(Context context) {
        return ServiceGenerator.createService(context, SearchService.class);
    }
}
