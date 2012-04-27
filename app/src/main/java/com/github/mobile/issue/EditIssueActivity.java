package com.github.mobile.issue;

import static com.github.mobile.util.GitHubIntents.EXTRA_ISSUE;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import org.eclipse.egit.github.core.Issue;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to edit the title and description for an issue
 */
public class EditIssueActivity extends RoboSherlockFragmentActivity {

    @InjectView(id.et_issue_title)
    private EditText titleText;

    @InjectView(id.et_issue_body)
    private EditText bodyText;

    @InjectExtra(EXTRA_ISSUE)
    private Issue issue;

    /**
     * Create intent to edit an issue
     *
     * @param issue
     * @return intent
     */
    public static Intent createIntent(final Issue issue) {
        return new Builder("repo.issues.edit.VIEW").issue(issue).toIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.issue_edit);
        setTitle(getString(string.issue_title) + issue.getNumber());

        titleText.setText(issue.getTitle());
        bodyText.setText(issue.getBody());
    }

    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.issue_edit, options);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.issue_edit:
            Intent intent = new Intent();
            Issue edited = new Issue();
            edited.setTitle(titleText.getText().toString());
            edited.setBody(bodyText.getText().toString());
            intent.putExtra(EXTRA_ISSUE, edited);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
