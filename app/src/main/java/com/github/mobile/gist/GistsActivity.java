package com.github.mobile.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.RequestCodes.GIST_CREATE;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.HomeActivity;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.menu;
import com.github.mobile.R.string;
import com.github.mobile.util.AccountUtils;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import roboguice.inject.InjectView;

/**
 * Activity to display view pagers of different Gist queries
 */
public class GistsActivity extends RoboSherlockFragmentActivity {

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string.gists_title);
        actionBar.setSubtitle(AccountUtils.getLogin(this));
        actionBar.setDisplayHomeAsUpEnabled(true);

        pager.setAdapter(new GistQueriesPagerAdapter(getResources(), getSupportFragmentManager()));
        indicator.setViewPager(pager);
    }

    private void randomGist() {
        new RandomGistTask(this).start();
    }

    private void openGist() {
        Builder prompt = new Builder(this);

        prompt.setTitle(getString(string.open_gist_title));
        prompt.setMessage(getString(string.enter_id_message));

        final EditText id = new EditText(this);
        prompt.setView(id);

        prompt.setPositiveButton(string.open, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                new OpenGistTask(GistsActivity.this, id.getText().toString().trim()).start();
            }
        });
        prompt.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu optionsMenu) {
        getSupportMenuInflater().inflate(menu.gists, optionsMenu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case id.open_gist:
            openGist();
            return true;
        case id.random_gist:
            randomGist();
            return true;
        case id.create_gist:
            startActivityForResult(new Intent(this, ShareGistActivity.class), GIST_CREATE);
            return true;
        case android.R.id.home:
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
