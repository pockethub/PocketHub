package com.github.pockethub.core.repo;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.services.repo.actions.ForkRepoClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.google.inject.Inject;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.bean.dto.response.Repo;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Task to fork a repository
 */
public class ForkRepositoryTask extends ProgressDialogTask<Repo> {

    private static final String TAG = "ForkRepositoryTask";

    @Inject
    private RepositoryService service;

    private final Repo repo;

    /**
     * Create task for context and id provider
     *
     * @param context
     * @param repo
     */
    public ForkRepositoryTask(Context context, Repo repo) {
        super(context);

        this.repo = repo;
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        showIndeterminate(R.string.forking_repository);

        execute();
    }

    @Override
    protected Repo run(Account account) throws Exception {
        return new Repo();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception forking repository", e);
    }
}
