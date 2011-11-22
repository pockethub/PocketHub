package com.github.mobile.android;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import android.test.AndroidTestCase;
import android.util.Log;

public class GitHubApiTest extends AndroidTestCase {

    private static final String TAG = "GHAT";

    public void testRepoApi() throws Exception {
        RepositoryService service = new RepositoryService();
        for (Repository repo : service.getRepositories("git")) {
            Log.d(TAG, repo.getName() + " Watchers: " + repo.getWatchers());
        }
    }

}
