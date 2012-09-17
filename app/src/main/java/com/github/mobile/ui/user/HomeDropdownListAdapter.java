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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.drawable;
import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.util.AvatarLoader;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Dropdown list adapter to display orgs. and other context-related activity
 * links
 */
public class HomeDropdownListAdapter extends SingleTypeAdapter<Object> {

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

    private static final int NON_ORG_ITEMS = 3;

    private final AvatarLoader avatars;

    private int selected;

    private final LayoutInflater inflater;

    /**
     * l Create adapter with initial orgs
     *
     * @param context
     * @param orgs
     * @param avatars
     */
    public HomeDropdownListAdapter(final Context context,
            final List<User> orgs, final AvatarLoader avatars) {
        super(context, layout.org_item);

        this.avatars = avatars;
        inflater = LayoutInflater.from(context);
        setOrgs(orgs);
    }

    /**
     * Get number of orgs
     *
     * @return org count
     */
    public int getOrgCount() {
        return getCount() - NON_ORG_ITEMS;
    }

    /**
     * Is the given position an org. selection position?
     *
     * @param position
     * @return true if org., false otherwise
     */
    public boolean isOrgPosition(final int position) {
        return position < getOrgCount();
    }

    /**
     * Get action at given position
     *
     * @param position
     * @return action id
     */
    public int getAction(final int position) {
        return position - getOrgCount();
    }

    /**
     * Set orgs to display
     *
     * @param orgs
     * @return this adapter
     */
    public HomeDropdownListAdapter setOrgs(final List<User> orgs) {
        int orgCount = orgs != null ? orgs.size() : 0;
        if (selected >= orgCount)
            selected = 0;

        List<Object> all = new ArrayList<Object>(orgCount + NON_ORG_ITEMS);
        if (orgCount > 0)
            all.addAll(orgs);

        // Add dummy objects for gists, issue dashboard, and bookmarks
        all.add(new Object());
        all.add(new Object());
        all.add(new Object());
        setItems(all);
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
    public long getItemId(int position) {
        if (isOrgPosition(position))
            return ((User) getItem(position)).getId();
        else
            return super.getItemId(position);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { id.tv_org_name, id.iv_avatar };
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = initialize(inflater.inflate(layout.org_dropdown_item,
                    null));
        update(position, convertView, getItem(position));
        return convertView;
    }

    private void setActionIcon(ImageView image, int drawable) {
        image.setImageResource(drawable);
        image.setTag(id.iv_avatar, null);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!isOrgPosition(position))
            position = selected;
        return super.getView(position, convertView, parent);
    }

    @Override
    protected void update(int position, Object item) {
        switch (getAction(position)) {
        case ACTION_GISTS:
            setText(0, string.gists);
            setActionIcon(imageView(1), drawable.dropdown_gist);
            break;
        case ACTION_DASHBOARD:
            setText(0, string.issue_dashboard);
            setActionIcon(imageView(1), drawable.dropdown_dashboard);
            break;
        case ACTION_BOOKMARKS:
            setText(0, string.bookmarks);
            setActionIcon(imageView(1), drawable.dropdown_bookmark);
            break;
        default:
            User user = (User) item;
            setText(0, user.getLogin());
            avatars.bind(imageView(1), user);
        }
    }
}
