package com.github.mobile.android.comment;

import static com.github.mobile.android.util.GitHubIntents.EXTRA_COMMENT_BODY;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_SUBTITLE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_TITLE;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_USER;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.User;

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
        return createIntent(null, null, null);
    }

    /**
     * Create intent to create a comment
     *
     * @param title
     * @param subtitle
     * @return intent
     */
    public static Intent createIntent(String title, String subtitle) {
        return createIntent(title, subtitle, null);
    }

    /**
     * Create intent to create a comment
     *
     * @param title
     * @param subtitle
     * @param user
     * @return intent
     */
    public static Intent createIntent(String title, String subtitle, User user) {
        Builder builder = new Builder("comment.create.VIEW");
        if (title != null)
            builder.add(EXTRA_TITLE, title);
        if (subtitle != null)
            builder.add(EXTRA_SUBTITLE, subtitle);
        if (user != null)
            builder.add(EXTRA_USER, user);
        return builder.toIntent();
    }

    @Inject
    private AvatarHelper avatarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.comment_create);

        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title != null)
            actionBar.setTitle(title);
        else
            actionBar.setTitle(string.create_comment_title);
        actionBar.setSubtitle(intent.getStringExtra(EXTRA_SUBTITLE));
        User user = (User) intent.getSerializableExtra(EXTRA_USER);
        if (user != null)
            avatarHelper.bind(actionBar, user);
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
