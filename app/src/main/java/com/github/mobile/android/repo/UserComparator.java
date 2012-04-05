package com.github.mobile.android.repo;

import com.github.mobile.android.authenticator.GitHubAccount;
import com.google.inject.Inject;

import java.util.Comparator;

import org.eclipse.egit.github.core.User;

/**
 * Sorts users and orgs in alphabetical order by username, but overriding this
 * to put the currently authenticated user at the top of the list.
 */
class UserComparator implements Comparator<User> {

    private final String currentUserLogin;

    @Inject
    public UserComparator(GitHubAccount gitHubAccount) {
        currentUserLogin = gitHubAccount.username;
    }

    @Override
    public int compare(User lhs, User rhs) {
        String lhsLogin = lhs.getLogin(), rhsLogin = rhs.getLogin();
        if (lhsLogin.equals(currentUserLogin))
            return -1;
        if (rhsLogin.equals(currentUserLogin))
            return 1;

        return lhsLogin.compareToIgnoreCase(rhsLogin);
    }
}
