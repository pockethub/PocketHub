package com.github.mobile.repo;

import com.github.mobile.authenticator.GitHubAccount;
import com.google.inject.Inject;

import java.util.Comparator;

import org.eclipse.egit.github.core.User;

/**
 * Sorts users and orgs in alphabetical order with special handling to put currently authenticated user first.
 */
public class UserComparator implements Comparator<User> {

    private final String login;

    /**
     * Create comparator for given account
     *
     * @param account
     */
    @Inject
    public UserComparator(final GitHubAccount account) {
        login = account.username;
    }

    @Override
    public int compare(final User lhs, final User rhs) {
        final String lhsLogin = lhs.getLogin();
        if (lhsLogin.equals(login))
            return -1;

        final String rhsLogin = rhs.getLogin();
        if (rhsLogin.equals(login))
            return 1;

        return lhsLogin.compareToIgnoreCase(rhsLogin);
    }
}
