package com.github.pockethub.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAccountManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Implements(AccountManager.class)
public class AccountManagerShadow extends ShadowAccountManager {

    public static AccountManager mockManager;
    public static Account[] accounts;

    static {
        String accountType = ApplicationProvider.getApplicationContext().getString(R.string.account_type);
        Account firstGitHubAccount = new Account("GitHubAccount", accountType);
        Account secondGitHubAccount = new Account("GitHubAccount2", accountType);
        accounts = new Account[]{firstGitHubAccount, secondGitHubAccount};
        mockManager = mock(AccountManager.class);

        when(mockManager.getAccountsByType(accountType)).thenReturn(accounts);
    }

    @Implementation
    public static AccountManager get(Context context) {
        return mockManager;
    }
}