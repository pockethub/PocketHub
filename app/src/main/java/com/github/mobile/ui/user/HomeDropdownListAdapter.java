/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.ui.user;

import static android.view.View.GONE;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.drawable;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.AvatarLoader;

import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Dropdown list adapter to display orgs. and other context-related activity
 * links
 */
public class HomeDropdownListAdapter extends BaseAdapter {

    /**
     * Action for Gists
     */
    public static final int ACTION_GISTS = 0;

    /**
     * Action for the issues dashboard
     */
    public static final int ACTION_DASHBOARD = 1;

    /**
     * Action for bookmarks
     */
    public static final int ACTION_BOOKMARKS = 2;

    private static class OrgItemView extends ItemView {

        public final TextView nameText;

        public final ImageView avatarView;

        public OrgItemView(View view) {
            super(view);

            nameText = (TextView) view.findViewById(id.tv_org_name);
            avatarView = (ImageView) view.findViewById(id.iv_avatar);
        }
    }

    private static class OrgListAdapter extends
            ItemListAdapter<User, OrgItemView> {

        private final AvatarLoader avatars;

        public OrgListAdapter(final int viewId, final LayoutInflater inflater,
                final User[] elements, final AvatarLoader avatars) {
            super(viewId, inflater, elements);

            this.avatars = avatars;
        }

        @Override
        protected void update(final int position, final OrgItemView view,
                final User user) {
            view.nameText.setText(user.getLogin());
            avatars.bind(view.avatarView, user);
        }

        @Override
        protected OrgItemView createView(final View view) {
            return new OrgItemView(view);
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).getId();
        }
    }

    private int selected;

    private final Context context;

    private final OrgListAdapter listAdapter;

    private final OrgListAdapter dropdownAdapter;

    /**
     * Create adapter with initial orgs
     *
     * @param context
     * @param orgs
     * @param avatarHelper
     */
    public HomeDropdownListAdapter(final Context context,
            final List<User> orgs, final AvatarLoader avatarHelper) {
        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        User[] orgItems = orgs.toArray(new User[orgs.size()]);

        listAdapter = new OrgListAdapter(layout.org_item, inflater, orgItems,
                avatarHelper);
        dropdownAdapter = new OrgListAdapter(layout.org_dropdown_item,
                inflater, orgItems, avatarHelper);
    }

    /**
     * Is the given position an org. selection position?
     *
     * @param position
     * @return true if org., false otherwise
     */
    public boolean isOrgPosition(final int position) {
        return position < listAdapter.getCount();
    }

    /**
     * Get action at given position
     *
     * @param position
     * @return action id
     */
    public int getAction(final int position) {
        return position - listAdapter.getCount();
    }

    /**
     * Set orgs to display
     *
     * @param orgs
     * @return this adapter
     */
    public HomeDropdownListAdapter setOrgs(final List<User> orgs) {
        User[] orgItems = orgs.toArray(new User[orgs.size()]);
        listAdapter.setItems(orgItems);
        dropdownAdapter.setItems(orgItems);
        notifyDataSetChanged();
        return this;
    }

    /**
     * @param selected
     * @return this adapter
     */
    public HomeDropdownListAdapter setSelected(int selected) {
        this.selected = selected;
        return this;
    }

    /**
     * @return selected
     */
    public int getSelected() {
        return selected;
    }

    @Override
    public int getCount() {
        return listAdapter.getCount() > 0 ? listAdapter.getCount() + 3 : 0;
    }

    @Override
    public Object getItem(int position) {
        switch (getAction(position)) {
        case ACTION_GISTS:
            return context.getString(string.gists);
        case ACTION_DASHBOARD:
            return context.getString(string.issue_dashboard);
        case ACTION_BOOKMARKS:
            return context.getString(string.bookmarks);
        default:
            return listAdapter.getItem(position);
        }
    }

    @Override
    public long getItemId(int position) {
        switch (getAction(position)) {
        case ACTION_GISTS:
        case ACTION_DASHBOARD:
        case ACTION_BOOKMARKS:
            return getItem(position).hashCode();
        default:
            return listAdapter.getItemId(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getAction(position)) {
        case ACTION_GISTS:
        case ACTION_DASHBOARD:
        case ACTION_BOOKMARKS:
            return listAdapter.getView(selected, null, parent);
        default:
            return listAdapter.getView(position, null, parent);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        switch (getAction(position)) {
        case ACTION_GISTS:
            View gistsRoot = LayoutInflater.from(context).inflate(
                    layout.org_dropdown_item, null);
            gistsRoot.findViewById(id.iv_avatar).setVisibility(GONE);
            ((TextView) gistsRoot.findViewById(id.tv_org_name))
                    .setText(getItem(position).toString());
            return gistsRoot;
        case ACTION_DASHBOARD:
            View dashboardRoot = LayoutInflater.from(context).inflate(
                    layout.org_dropdown_item, null);
            dashboardRoot.findViewById(id.iv_avatar).setVisibility(GONE);
            ((TextView) dashboardRoot.findViewById(id.tv_org_name))
                    .setText(getItem(position).toString());
            return dashboardRoot;
        case ACTION_BOOKMARKS:
            View bookmarksRoot = LayoutInflater.from(context).inflate(
                    layout.org_dropdown_item, null);
            ((ImageView) bookmarksRoot.findViewById(id.iv_avatar))
                    .setBackgroundResource(drawable.action_bookmark);
            ((TextView) bookmarksRoot.findViewById(id.tv_org_name))
                    .setText(getItem(position).toString());
            return bookmarksRoot;
        default:
            return dropdownAdapter.getDropDownView(position, null, parent);
        }
    }
}
