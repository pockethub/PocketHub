package com.github.pockethub.android;

import android.content.Context;

import com.meisolsson.githubsdk.core.ServiceGenerator;
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

}
