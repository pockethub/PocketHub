package com.github.mobile.android.gist;

import static android.content.Intent.EXTRA_TEXT;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mobile.android.R;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.TextWatcherAdapter;
import com.google.inject.Inject;

import java.util.Collections;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class ShareGistActivity extends RoboActivity {

    /**
     * Gist successfully created
     */
    public static final int RESULT_CREATED = 1;

    private static final String TAG = "GHShare";

    @InjectView(id.gistNameText)
    private EditText nameText;

    @InjectView(id.gistContentText)
    private EditText contentText;

    @InjectView(id.publicCheck)
    private CheckBox publicCheckBox;

    @InjectView(id.createGistButton)
    private Button createButton;

    @Inject
    ContextScopedProvider<GistService> gistServiceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.share_gist);

        String text = getIntent().getStringExtra(EXTRA_TEXT);

        if (text != null && text.length() > 0)
            contentText.setText(text);

        contentText.addTextChangedListener(new TextWatcherAdapter() {

            public void afterTextChanged(Editable s) {
                createButton.setEnabled(s.toString().length() > 0);
            }
        });

        createButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                createGist();
            }
        });
    }

    private void createGist() {
        final boolean isPublic = publicCheckBox.isChecked();
        String enteredName = nameText.getText().toString().trim();
        final String name = enteredName.length() > 0 ? enteredName : "file.txt";
        final String content = contentText.getText().toString();
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.creating_gist));
        progress.show();
        new RoboAsyncTask<Gist>(this) {

            public Gist call() throws Exception {
                Gist gist = new Gist();
                gist.setDescription("Android created Gist");
                gist.setPublic(isPublic);
                GistFile file = new GistFile();
                file.setContent(content);
                file.setFilename(name);
                gist.setFiles(Collections.singletonMap(name, file));
                return gistServiceProvider.get(ShareGistActivity.this).createGist(gist);
            }

            protected void onSuccess(Gist gist) throws Exception {
                progress.cancel();
                startActivity(ViewGistActivity.createIntent(ShareGistActivity.this, gist));
                setResult(RESULT_CREATED);
                finish();
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.cancel();
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(ShareGistActivity.this, e.getMessage(), 5000).show();
            }
        }.execute();
    }
}
