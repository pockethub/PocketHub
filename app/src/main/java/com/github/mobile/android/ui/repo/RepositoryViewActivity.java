package com.github.mobile.android.ui.repo;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_REPOSITORY;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.ui.user.UserViewActivity;
import com.github.mobile.android.util.GitHubIntents.Builder;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import org.eclipse.egit.github.core.Repository;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to view a repository
 */
public class RepositoryViewActivity extends RoboSherlockFragmentActivity {

    /**
     * Create intent for this activity
     *
     * @param repository
     * @return intent
     */
    public static Intent createIntent(Repository repository) {
        return new Builder("repo.VIEW").repo(repository).toIntent();
    }

    @InjectExtra(EXTRA_REPOSITORY)
    private Repository repository;

    @InjectView(id.tpi_header)
    private TitlePageIndicator indicator;

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout.pager_with_title);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.getName());
        actionBar.setSubtitle(repository.getOwner().getLogin());
        actionBar.setDisplayHomeAsUpEnabled(true);

        pager.setAdapter(new RepositoryPagerAdapter(getSupportFragmentManager()));
        indicator.setViewPager(pager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = UserViewActivity.createIntent(repository.getOwner());
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
