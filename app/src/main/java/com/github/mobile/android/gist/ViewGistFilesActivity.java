package com.github.mobile.android.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to page through the content of all the files in a Gist
 */
public class ViewGistFilesActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent to show files with an initial selected file
     *
     * @param gist
     * @param position
     * @return intent
     */
    public static Intent createIntent(Gist gist, int position) {
        return new Builder("gist.files.VIEW").add(EXTRA_GIST_ID, gist.getId()).add(EXTRA_POSITION, position).toIntent();
    }

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_GIST_ID)
    private String gistId;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    private Gist gist;

    @Inject
    private GistStore store;

    @Inject
    private AvatarHelper avatarHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.pager_with_title);

        gist = store.getGist(gistId);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(string.gist) + " " + gistId);
        User author = gist.getUser();
        if (author != null) {
            actionBar.setSubtitle(author.getLogin());
            avatarHelper.bind(actionBar, author);
        } else
            actionBar.setSubtitle(string.anonymous);

        pager.setAdapter(new GistFilesPagerAdapter(getSupportFragmentManager(), gist));
        indicator.setViewPager(pager);

        pager.setCurrentItem(initialPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = ViewGistsActivity.createIntent(gist);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
