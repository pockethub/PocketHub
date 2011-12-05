package com.github.mobile.android.gist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.google.inject.Inject;

import java.util.Collection;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.fragment.RoboFragment;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Main activity for Gists
 */
public class GistsActivity extends RoboFragment {

    private static final int REQUEST_CREATE = 1;

    @Inject
    private Context context;

    @InjectView(id.createGistButton)
    private Button createButton;

    @InjectView(id.randomGistButton)
    private Button randomButton;

    @Inject
    ContextScopedProvider<GistService> gistServiceProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.gists, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivityForResult(new Intent(context, ShareGistActivity.class), REQUEST_CREATE);
            }
        });

        randomButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(context);
                progress.setMessage("Random Gist...");
                progress.show();
                new RoboAsyncTask<Gist>(context) {

                    public Gist call() throws Exception {
                        GistService service = gistServiceProvider.get(context);
                        PageIterator<Gist> pages = service.pagePublicGists(1);
                        pages.next();
                        int randomPage = 1 + (int) (Math.random() * ((pages.getLastPage() - 1) + 1));
                        Collection<Gist> gists = service.pagePublicGists(randomPage, 1).next();
                        if (gists.isEmpty())
                            throw new IllegalArgumentException("No Gists found");
                        return gists.iterator().next();
                    }

                    protected void onSuccess(Gist gist) throws Exception {
                        progress.cancel();
                        Intent intent = new Intent(context, ViewGistActivity.class);
                        intent.putExtra("gist", gist.getId());
                        startActivity(intent);
                    }

                    protected void onException(Exception e) throws RuntimeException {
                        progress.cancel();
                        Toast.makeText(context, e.getMessage(), 5000).show();
                    }
                }.execute();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE && ShareGistActivity.RESULT_CREATED == resultCode) {
            GistFragment fragment = (GistFragment) getFragmentManager().findFragmentById(id.gist_list);
            fragment.getLoaderManager().restartLoader(0, null, fragment);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
