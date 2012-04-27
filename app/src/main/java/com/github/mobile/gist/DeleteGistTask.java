package com.github.mobile.gist;

import static android.app.Activity.RESULT_OK;
import android.app.Activity;
import android.app.ProgressDialog;

import com.github.mobile.R.string;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtil;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.inject.ContextScopedProvider;

/**
 * Async task to delete a Gist
 */
public class DeleteGistTask extends ProgressDialogTask<Gist> {

    private final String id;

    @Inject
    private ContextScopedProvider<GistService> serviceProvider;

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
    public Gist run() throws Exception {
        serviceProvider.get(getContext()).deleteGist(id);
        return null;
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        super.onSuccess(gist);

        Activity activity = (Activity) getContext();
        activity.setResult(RESULT_OK);
        activity.finish();
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        ToastUtil.show((Activity) getContext(), e.getMessage());
    }
}
