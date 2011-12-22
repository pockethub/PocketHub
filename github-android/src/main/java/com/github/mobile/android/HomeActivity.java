package com.github.mobile.android;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.gist.GistsActivity;
import com.github.mobile.android.repo.RepoBrowseActivity;
import com.github.mobile.android.ui.WelcomeActivity;
import com.github.mobile.android.util.Avatar;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.User;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContextScopedProvider;
import roboguice.inject.InjectView;

/**
 * Home screen activity
 */
public class HomeActivity extends RoboActivity {

    private static final String TAG = "HA";
    private static final int CODE_LOGIN = 1;

    private class LinksListAdapter extends ArrayAdapter<String> {

        /**
         * @param objects
         */
        public LinksListAdapter(List<String> objects) {
            super(HomeActivity.this, R.layout.home_link_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) getLayoutInflater().inflate(layout.home_link_item, null);
            String name = getItem(position);
            ((TextView) view.findViewById(R.id.tv_home_link)).setText(name);
            ((ImageView) view.findViewById(R.id.iv_home_link)).setBackgroundResource(linkViews.get(name));
            return view;
        }
    }

    private class OrgListAdapter extends ArrayAdapter<User> {

        /**
         * @param objects
         */
        public OrgListAdapter(List<User> objects) {
            super(HomeActivity.this, R.layout.org_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) getLayoutInflater().inflate(layout.org_item, null);
            User user = getItem(position);
            ((TextView) view.findViewById(R.id.tv_org_name)).setText(user.getLogin());
            Avatar.bind(HomeActivity.this, ((ImageView) view.findViewById(R.id.iv_gravatar)), user);
            return view;
        }
    }

    private Map<String, Integer> linkViews = new LinkedHashMap<String, Integer>();

    @Inject
    private ContextScopedProvider<Account> accountProvider;

    @Inject
    private ContextScopedProvider<AccountDataManager> cache;

    @InjectView(R.id.lv_orgs)
    private ListView orgsList;

    @InjectView(R.id.lv_links)
    private ListView linksList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        linkViews.put("Dashboard", R.drawable.dashboard_icon);
        linkViews.put("Gists", R.drawable.gist_icon);

        linksList.setAdapter(new LinksListAdapter(new ArrayList<String>(linkViews.keySet())));
        linksList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                switch (position) {
                case 1:
                    startActivity(new Intent(HomeActivity.this, GistsActivity.class));
                    break;

                default:
                    break;
                }
            }
        });

        orgsList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                User user = (User) view.getItemAtPosition(position);
                startActivity(RepoBrowseActivity.createIntent(user));
            }
        });
    }

    private void loadOrgs() {
        cache.get(this).getOrgs(new RequestFuture<List<User>>() {

            public void success(List<User> response) {
                orgsList.setAdapter(new OrgListAdapter(response));
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CODE_LOGIN == requestCode && resultCode == RESULT_OK)
            loadOrgs();
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accountProvider.get(this) == null) {
            Log.d(TAG, "No account currently available, starting Welcome activity");
            startActivityForResult(new Intent(this, WelcomeActivity.class), CODE_LOGIN);
        } else {
            loadOrgs();
        }
    }
}
