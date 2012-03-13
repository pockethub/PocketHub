package com.github.mobile.android.comment;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;

/**
 * Activity to create a comment on a {@link Gist} or {@link Issue}
 */
public class CreateCommentActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to create a comment
     *
     * @return intent
     */
    public static Intent createIntent() {
        return new Builder("comment.create.VIEW").toIntent();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.comment_create);
        setTitle(string.create_comment_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getSupportMenuInflater().inflate(menu.comment, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.apply:
            String comment = ((EditText) findViewById(id.et_comment)).getText().toString();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_COMMENT_BODY, comment);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
