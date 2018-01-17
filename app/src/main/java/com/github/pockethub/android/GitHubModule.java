package com.github.pockethub.android;

import android.content.Context;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.meisolsson.githubsdk.service.issues.IssueService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommentService;
import com.meisolsson.githubsdk.service.repositories.RepositoryCommitService;
import com.meisolsson.githubsdk.service.users.UserService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
}
