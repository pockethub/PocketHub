package com.github.mobile.android.gist;

import static android.content.Intent.EXTRA_TEXT;
import static android.widget.Toast.LENGTH_LONG;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.TextWatcherAdapter;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import java.util.Collections;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class ShareGistActivity extends RoboSherlockFragmentActivity {

    /**
     * Gist successfully created
     */
    public static final int RESULT_CREATED = RESULT_FIRST_USER;

    private static final String TAG = "SGA";

    @InjectView(id.gistDescriptionText)
    private EditText descriptionText;

    @InjectView(id.gistNameText)
    private EditText nameText;

    @InjectView(id.gistContentText)
    private EditText contentText;

    @InjectView(id.publicCheck)
    private CheckBox publicCheckBox;

    @Inject
    ContextScopedProvider<GistService> gistServiceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.share_gist);
        setTitle(string.new_gist);

        String text = getIntent().getStringExtra(EXTRA_TEXT);

        if (TextUtils.isEmpty(text))
            contentText.setText(text);

        contentText.addTextChangedListener(new TextWatcherAdapter() {

            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.gist_create, options);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(id.gist_create).setEnabled(!TextUtils.isEmpty(contentText.getText()));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.gist_create:
            createGist();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void createGist() {
        final boolean isPublic = publicCheckBox.isChecked();
        String enteredDescription = descriptionText.getText().toString().trim();
        String enteredName = nameText.getText().toString().trim();
        final String description = enteredDescription.length() > 0 ? enteredDescription
                : getString(string.gist_description_hint);
        final String name = enteredName.length() > 0 ? enteredName : getString(string.gist_file_name_hint);
        final String content = contentText.getText().toString();
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(string.creating_gist));
        progress.show();
        new RoboAsyncTask<Gist>(this) {

            public Gist call() throws Exception {
                Gist gist = new Gist();
                gist.setDescription(description);
                gist.setPublic(isPublic);

                GistFile file = new GistFile();
                file.setContent(content);
                file.setFilename(name);
                gist.setFiles(Collections.singletonMap(name, file));

                return gistServiceProvider.get(ShareGistActivity.this).createGist(gist);
            }

            protected void onSuccess(Gist gist) throws Exception {
                progress.cancel();
                startActivity(ViewGistsActivity.createIntent(gist));
                setResult(RESULT_CREATED);
                finish();
            }

            protected void onException(Exception e) throws RuntimeException {
                progress.cancel();
                Log.d(TAG, e.getMessage(), e);
                Toast.makeText(getApplication(), e.getMessage(), LENGTH_LONG).show();
            }
        }.execute();
    }
}
