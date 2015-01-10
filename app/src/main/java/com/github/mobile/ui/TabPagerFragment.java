package com.github.mobile.ui;

import static android.widget.TabHost.OnTabChangeListener;
import static android.widget.TabHost.TabContentFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.mobile.R;
import com.github.mobile.util.TypefaceUtils;

public abstract class TabPagerFragment<V extends PagerAdapter & FragmentProvider>
    extends PagerFragment implements OnTabChangeListener, TabContentFactory {


    /**
     * View pager
     */
    protected ViewPager pager;

    /**
     * Tab host
     */
    protected TabHost host;

    /**
     * Pager adapter
     */
    protected V adapter;

    @Override
    public void onPageSelected(final int position) {
        super.onPageSelected(position);

        host.setCurrentTab(position);
    }

    @Override
    public void onTabChanged(String tabId) {
        updateCurrentItem(host.getCurrentTab());
    }

    @Override
    public View createTabContent(String tag) {
        return ViewUtils.setGone(new View(getActivity().getApplication()), true);
    }

    /**
     * Create pager adapter
     *
     * @return pager adapter
     */
    protected abstract V createAdapter();

    /**
     * Get title for position
     *
     * @param position
     * @return title
     */
    protected String getTitle(final int position) {
        return adapter.getPageTitle(position).toString();
    }

    /**
     * Get icon for position
     *
     * @param position
     * @return icon
     */
    protected String getIcon(final int position) {
        return null;
    }

    /**
     * Set tab and pager as gone or visible
     *
     * @param gone
     * @return this activity
     */
    protected TabPagerFragment<V> setGone(boolean gone) {
        ViewUtils.setGone(host, gone);
        ViewUtils.setGone(pager, gone);
        return this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (adapter instanceof FragmentPagerAdapter)
            ((FragmentPagerAdapter) adapter).clearAdapter();
    }

    /**
     * Set current item to new position
     * <p>
     * This is guaranteed to only be called when a position changes and the
     * current item of the pager has already been updated to the given position
     * <p>
     * Sub-classes may override this method
     *
     * @param position
     */
    protected void setCurrentItem(final int position) {
        // Intentionally left blank
    }

    /**
     * Get content view to be used when {@link #onCreate(android.os.Bundle)} is called
     *
     * @return layout resource id
     */
    protected int getContentView() {
        return R.layout.pager_with_tabs;
    }

    private void updateCurrentItem(final int newPosition) {
        if (newPosition > -1 && newPosition < adapter.getCount()) {
            pager.setItem(newPosition);
            setCurrentItem(newPosition);
        }
    }

    private void createPager() {
        adapter = createAdapter();
        getActivity().supportInvalidateOptionsMenu();
        pager.setAdapter(adapter);
    }

    /**
     * Create tab using information from current adapter
     * <p>
     * This can be called when the tabs changed but must be called after an
     * initial call to {@link #configureTabPager()}
     */
    protected void createTabs() {
        if (host.getTabWidget().getTabCount() > 0) {
            // Crash on Gingerbread if tab isn't set to zero since adding a
            // new tab removes selection state on the old tab which will be
            // null unless the current tab index is the same as the first
            // tab index being added
            host.setCurrentTab(0);
            host.clearAllTabs();
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            TabHost.TabSpec spec = host.newTabSpec("tab" + i);
            spec.setContent(this);
            View view = inflater.inflate(R.layout.tab, null);
            TextView icon = (TextView) view.findViewById(R.id.tv_icon);
            String iconText = getIcon(i);
            if (!TextUtils.isEmpty(iconText))
                icon.setText(getIcon(i));
            else
                ViewUtils.setGone(icon, true);
            TypefaceUtils.setOcticons(icon);
            ((TextView) view.findViewById(R.id.tv_tab)).setText(getTitle(i));

            spec.setIndicator(view);
            host.addTab(spec);

            int background;
            if (i == 0)
                background = R.drawable.tab_selector_right;
            else if (i == count - 1)
                background = R.drawable.tab_selector_left;
            else
                background = R.drawable.tab_selector_left_right;
            ((ImageView) view.findViewById(R.id.iv_tab))
                .setImageResource(background);
        }
    }

    /**
     * Configure tabs and pager
     */
    protected void configureTabPager() {
        createPager();
        createTabs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getContentView(),null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        pager = (ViewPager) view.findViewById(R.id.vp_pages);
        pager.setOnPageChangeListener(this);
        host = (TabHost) view.findViewById(R.id.th_tabs);
        host.setup();
        host.setOnTabChangedListener(this);
    }

    @Override
    protected FragmentProvider getProvider() {
        return adapter;
    }
}
