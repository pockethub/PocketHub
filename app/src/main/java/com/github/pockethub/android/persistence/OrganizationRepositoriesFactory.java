package com.github.pockethub.android.persistence;

import android.accounts.Account;
import android.content.Context;
import com.meisolsson.githubsdk.model.User;

import javax.inject.Inject;
import javax.inject.Provider;

public class OrganizationRepositoriesFactory {

    @Inject
    protected Context context;

    @Inject
    protected Provider<Account> accountProvider;

    @Inject
    public OrganizationRepositoriesFactory() {

    }

    public OrganizationRepositories create(User org) {
        return new OrganizationRepositories(org, context, accountProvider);
    }
}
