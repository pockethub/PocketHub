package com.github.mobile.android.comment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.EditText;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.menu;
import com.github.mobile.android.util.GitHubIntents;
import com.github.mobile.android.util.GitHubIntents.Builder;

import roboguice.activity.RoboFragmentActivity;

/**
 * Activity to create a comment
 */
public class CreateCommentActivity extends RoboFragmentActivity {

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu options) {
        getMenuInflater().inflate(menu.comment, options);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.apply:
            String comment = ((EditText) findViewById(id.et_comment)).getText().toString();
            Intent intent = new Intent();
            intent.putExtra(GitHubIntents.EXTRA_COMMENT_BODY, comment);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
