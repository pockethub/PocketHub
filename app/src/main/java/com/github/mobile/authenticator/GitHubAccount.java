package com.github.mobile.authenticator;

public class GitHubAccount {
    public final String username;
    public final String password;

    public GitHubAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + username + "]";
    }
}
