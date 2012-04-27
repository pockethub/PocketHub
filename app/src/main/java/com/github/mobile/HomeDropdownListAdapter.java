package com.github.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.R.layout;
import com.github.mobile.R.string;
import com.github.mobile.repo.OrgViewHolder;
import com.github.mobile.util.AvatarUtils;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewFactory;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.madgag.android.listviews.ViewInflator;

import java.util.List;

import org.eclipse.egit.github.core.User;

/**
 * Dropdown list adapter to display orgs. and other context-related activity links
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
     * Action for issues filter
     */
    public static final int ACTION_FILTERS = 2;

    private int selected;

    private final Context context;

    private final ViewHoldingListAdapter<User> orgAdapter;

    /**
     * Create adapter with initial orgs
     *
     * @param context
     * @param orgs
     * @param avatarHelper
     */
    public HomeDropdownListAdapter(final Context context, final List<User> orgs, final AvatarUtils avatarHelper) {
        this.context = context;

        ViewHolderFactory<User> userViewHolderFactory = ReflectiveHolderFactory.reflectiveFactoryFor(
                OrgViewHolder.class, avatarHelper);
        ViewFactory<User> selectedUserViewFactory = new ViewFactory<User>(ViewInflator.viewInflatorFor(context,
                layout.org_item), userViewHolderFactory);
        ViewFactory<User> dropDownViewFactory = new ViewFactory<User>(ViewInflator.viewInflatorFor(context,
                layout.org_item_dropdown), userViewHolderFactory);
        orgAdapter = new ViewHoldingListAdapter<User>(orgs, selectedUserViewFactory, dropDownViewFactory);
    }

    /**
     * Is the given position an org. selection position?
     *
     * @param position
     * @return true if org., false otherwise
     */
    public boolean isOrgPosition(final int position) {
        return position < orgAdapter.getCount();
    }

    /**
     * Get action at given position
     *
     * @param position
     * @return action id
     */
    public int getAction(final int position) {
        return position - orgAdapter.getCount();
    }

    /**
     * Set orgs to display
     *
     * @param orgs
     * @return this adapter
     */
    public HomeDropdownListAdapter setOrgs(final List<User> orgs) {
        orgAdapter.setList(orgs);
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
        return orgAdapter.getCount() > 0 ? orgAdapter.getCount() + 3 : 0;
    }

    @Override
    public Object getItem(int position) {
        switch (getAction(position)) {
        case ACTION_GISTS:
            return context.getString(string.gists);
        case ACTION_DASHBOARD:
            return context.getString(string.issue_dashboard);
        case ACTION_FILTERS:
            return context.getString(string.issue_filters);
        default:
            return orgAdapter.getItem(position);
        }
    }

    @Override
    public long getItemId(int position) {
        switch (getAction(position)) {
        case ACTION_GISTS:
        case ACTION_DASHBOARD:
        case ACTION_FILTERS:
            return getItem(position).hashCode();
        default:
            return orgAdapter.getItemId(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getAction(position)) {
        case ACTION_GISTS:
        case ACTION_DASHBOARD:
        case ACTION_FILTERS:
            return orgAdapter.getView(selected, null, parent);
        default:
            return orgAdapter.getView(position, null, parent);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        switch (getAction(position)) {
        case ACTION_GISTS:
        case ACTION_DASHBOARD:
        case ACTION_FILTERS:
            Object item = getItem(position);
            View root = LayoutInflater.from(context).inflate(layout.context_item_dropdown, null);
            ((TextView) root.findViewById(id.tv_item_name)).setText(item.toString());
            return root;
        default:
            return orgAdapter.getDropDownView(position, null, parent);
        }
    }

}
