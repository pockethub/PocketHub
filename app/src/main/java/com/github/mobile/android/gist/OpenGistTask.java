package com.github.mobile.android.gist;

import static android.widget.Toast.LENGTH_LONG;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.github.mobile.android.R.string;
import com.github.mobile.android.RequestCodes;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;

import roboguice.inject.ContextScopedProvider;
import roboguice.util.RoboAsyncTask;

/**
 * Task to load and open a Gist with an id
 */
public class OpenGistTask extends RoboAsyncTask<Gist> {

    private final String id;

    private ProgressDialog progress;

    @Inject
    private ContextScopedProvider<GistStore> storeProvider;

    /**
     * Create task
     *
     * @param context
     * @param gistId
     */
    public OpenGistTask(final Activity context, final String gistId) {
        super(context);
        id = gistId;
    }

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
        progress.setMessage(getContext().getString(string.loading_gist));
        progress.show();

        execute();
    }

    @Override
    public Gist call() throws Exception {
        return storeProvider.get(getContext()).refreshGist(id);
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        dismissProgress();

        ((Activity) getContext()).startActivityForResult(ViewGistsActivity.createIntent(gist), RequestCodes.GIST_VIEW);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        dismissProgress();

        Toast.makeText(getContext(), e.getMessage(), LENGTH_LONG).show();
    }
}
