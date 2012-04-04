package com.github.mobile.android.gist;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GISTS;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_GIST_ID;
import static com.github.mobile.android.util.GitHubIntents.EXTRA_POSITION;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.view.MenuItem;
import com.github.mobile.android.ConfirmDialogFragment;
import com.github.mobile.android.DialogFragmentActivity;
import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.util.GitHubIntents.Builder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.eclipse.egit.github.core.Gist;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

/**
 * Activity to display a collection of Gists in a pager
 */
public class ViewGistsActivity extends DialogFragmentActivity implements OnPageChangeListener {

    private static final int REQUEST_CONFIRM_DELETE = 1;

    /**
     * Create an intent to show a single gist
     *
     * @param gist
     * @return intent
     */
    public static Intent createIntent(Gist gist) {
        return createIntent(Arrays.asList(gist), 0);
    }

    /**
     * Create an intent to show gists with an initial selected filed
     *
     * @param gists
     * @param position
     * @return intent
     */
    public static Intent createIntent(List<Gist> gists, int position) {
        return new Builder("gists.VIEW").add(EXTRA_GISTS, (Serializable) gists).add(EXTRA_POSITION, position)
                .toIntent();
    }

    @InjectView(id.vp_pages)
    private ViewPager pager;

    @InjectExtra(EXTRA_GISTS)
    private List<Gist> gists;

    @InjectExtra(EXTRA_POSITION)
    private int initialPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.pager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pager.setAdapter(new GistsPagerAdapter(getSupportFragmentManager(), gists.toArray(new Gist[gists.size()])));
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(initialPosition);
        onPageSelected(initialPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, GistsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        case id.gist_delete:
            String gistId = gists.get(pager.getCurrentItem()).getId();
            Bundle args = new Bundle();
            args.putString(EXTRA_GIST_ID, gistId);
            ConfirmDialogFragment.show(this, REQUEST_CONFIRM_DELETE, getString(string.confirm_gist_delete_title),
                    getString(string.confirm_gist_delete_message), args);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        if (REQUEST_CONFIRM_DELETE == requestCode && RESULT_OK == resultCode) {
            final String gistId = arguments.getString(EXTRA_GIST_ID);
            new DeleteGistTask(this, gistId).start();
            return;
        }

        super.onDialogResult(requestCode, resultCode, arguments);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Intentionally left blank
    }

    public void onPageSelected(int position) {
        setTitle(getString(string.gist) + " " + gists.get(position).getId());
    }

    public void onPageScrollStateChanged(int state) {
        // Intentionally left blank
    }
}
