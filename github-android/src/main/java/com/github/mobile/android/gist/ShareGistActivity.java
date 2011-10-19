package com.github.mobile.android.gist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.github.mobile.android.TextWatcherAdapter;
import com.google.inject.Inject;

import java.util.Collections;

import com.google.inject.Provider;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Activity to share a text selection as a public or private Gist
 */
public class ShareGistActivity extends RoboActivity {

	private static final String TAG = "GHShare";

	@InjectView(R.id.gistNameText)
	private EditText nameText;

	@InjectView(R.id.gistContentText)
	private EditText contentText;

	@InjectView(R.id.publicCheck)
	private CheckBox publicCheckBox;

	@InjectView(R.id.createGistButton)
	private Button createButton;

	@Inject Provider<GistService> gistServiceProvider;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_gist);

		String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);

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
		progress.setMessage("Creating Gist...");
		progress.show();
		new RoboAsyncTask<Gist>() {

			public Gist call() throws Exception {
				Gist gist = new Gist();
				gist.setDescription("Created from my Android device");
				gist.setPublic(isPublic);
				GistFile file = new GistFile();
				file.setContent(content);
				file.setFilename(name);
				gist.setFiles(Collections.singletonMap(name, file));
				return gistServiceProvider.get().createGist(gist);
			}

			protected void onSuccess(Gist gist) throws Exception {
				progress.cancel();
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(gist
						.getHtmlUrl())));
				finish();
			}

			protected void onException(Exception e) throws RuntimeException {
				progress.cancel();
				Log.e(TAG, e.getMessage(), e);
				Toast.makeText(ShareGistActivity.this, e.getMessage(), 5000)
						.show();
			}
		}.execute();
	}
}
