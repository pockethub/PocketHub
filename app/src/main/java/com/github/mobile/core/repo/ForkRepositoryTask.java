package com.github.mobile.core.repo;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.github.mobile.R;
import com.github.mobile.ui.ProgressDialogTask;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Task to fork a repository
 */
public class ForkRepositoryTask extends ProgressDialogTask<Repository> {

    private static final String TAG = "ForkRepositoryTask";

    @Inject
    private RepositoryService service;

    private final IRepositoryIdProvider repo;

    /**
     * Create task for context and id provider
     *
     * @param context
     * @param repo
     */
    public ForkRepositoryTask(Context context, IRepositoryIdProvider repo) {
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
    protected Repository run(Account account) throws Exception {
        return service.forkRepository(repo);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception forking repository", e);
    }
}
