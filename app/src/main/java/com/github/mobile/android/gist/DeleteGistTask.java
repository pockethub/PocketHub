package com.github.mobile.android.gist;

import static android.widget.Toast.LENGTH_LONG;
import static com.github.mobile.android.ResultCodes.GIST_DELETE;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.github.mobile.android.R.string;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.inject.ContextScopedProvider;
import roboguice.util.RoboAsyncTask;

/**
 * Async task to delete a Gist
 */
public class DeleteGistTask extends RoboAsyncTask<Gist> {

    private final String id;

    private ProgressDialog progress;

    /**
     * Create task
     *
     * @param context
     * @param gistId
     */
    public DeleteGistTask(final Activity context, final String gistId) {
        super(context);
        id = gistId;
    }

    @Inject
    private ContextScopedProvider<GistService> serviceProvider;

    private void dismissProgress() {
        if (progress != null)
            progress.hide();
    }

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        dismissProgress();

        progress = new ProgressDialog(getContext());
        progress.setIndeterminate(true);
        progress.setMessage(getContext().getString(string.deleting_gist));
        progress.show();

        execute();
    }

    @Override
    public Gist call() throws Exception {
        serviceProvider.get(getContext()).deleteGist(id);
        return null;
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        dismissProgress();

        Activity activity = (Activity) getContext();
        activity.setResult(GIST_DELETE);
        activity.finish();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        dismissProgress();

        Toast.makeText(getContext(), e.getMessage(), LENGTH_LONG).show();
    }
}
