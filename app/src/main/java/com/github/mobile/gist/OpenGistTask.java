package com.github.mobile.gist;

import static com.github.mobile.RequestCodes.GIST_VIEW;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.github.mobile.R.string;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.ToastUtil;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;

/**
 * Task to load and open a Gist with an id
 */
public class OpenGistTask extends ProgressDialogTask<Gist> {

    private final String id;

    @Inject
    private GistStore store;

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

    /**
     * Execute the task with a progress dialog displaying.
     * <p>
     * This method must be called from the main thread.
     */
    public void start() {
        dismissProgress();

        Context context = getContext();
        progress = new ProgressDialog(context);
        progress.setIndeterminate(true);
        progress.setMessage(context.getString(string.loading_gist));
        progress.show();

        execute();
    }

    @Override
    protected void onSuccess(Gist gist) throws Exception {
        super.onSuccess(gist);

        ((Activity) getContext()).startActivityForResult(ViewGistsActivity.createIntent(gist), GIST_VIEW);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        ToastUtil.show((Activity) getContext(), e.getMessage());
    }

    @Override
    protected Gist run() throws Exception {
        return store.refreshGist(id);
    }
}
