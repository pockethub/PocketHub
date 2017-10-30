package com.github.pockethub.android.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class OcticonTextView extends AppCompatTextView {

    /**
     * Private repository icon
     */
    public static final String ICON_PRIVATE = "\uf26a";

    /**
     * Public repository icon
     */
    public static final String ICON_PUBLIC = "\uf201";

    /**
     * Fork icon
     */
    public static final String ICON_FORK = "\uf202";

    /**
     * Create icon
     */
    public static final String ICON_CREATE = "\uf203";

    /**
     * Delete icon
     */
    public static final String ICON_DELETE = "\uf204";

    /**
     * Push icon
     */
    public static final String ICON_PUSH = "\uf205";

    /**
     * Wiki icon
     */
    public static final String ICON_WIKI = "\uf207";

    /**
     * Upload icon
     */
    public static final String ICON_UPLOAD = "\uf20C";

    /**
     * Gist icon
     */
    public static final String ICON_GIST = "\uf20E";

    /**
     * Add member icon
     */
    public static final String ICON_ADD_MEMBER = "\uf21A";

    /**
     * Public mirror repository icon
     */
    public static final String ICON_MIRROR_PUBLIC = "\uf224";

    /**
     * Public mirror repository icon
     */
    public static final String ICON_MIRROR_PRIVATE = "\uf225";

    /**
     * Follow icon
     */
    public static final String ICON_FOLLOW = "\uf21C";

    /**
     * Star icon
     */
    public static final String ICON_STAR = "\uf02A";

    /**
     * Pull request icon
     */
    public static final String ICON_PULL_REQUEST = "\uf222";

    /**
     * Issue open icon
     */
    public static final String ICON_ISSUE_OPEN = "\uf226";

    /**
     * Issue reopen icon
     */
    public static final String ICON_ISSUE_REOPEN = "\uf227";

    /**
     * Issue close icon
     */
    public static final String ICON_ISSUE_CLOSE = "\uf228";

    /**
     * Issue comment icon
     */
    public static final String ICON_ISSUE_COMMENT = "\uf229";

    /**
     * Comment icon
     */
    public static final String ICON_COMMENT = "\uf22b";

    /**
     * News icon
     */
    public static final String ICON_NEWS = "\uf234";

    /**
     * Watch icon
     */
    public static final String ICON_WATCH = "\uf04e";

    /**
     * Team icon
     */
    public static final String ICON_TEAM = "\uf019";

    /**
     * Code icon
     */
    public static final String ICON_CODE = "\uf010";

    /**
     * Tag icon
     */
    public static final String ICON_TAG = "\uf015";

    /**
     * Commit icon
     */
    public static final String ICON_COMMIT = "\uf01f";

    /**
     * Merge icon
     */
    public static final String ICON_MERGE = "\uf023";

    /**
     * Key icon
     */
    public static final String ICON_KEY = "\uf049";

    /**
     * Lock icon
     */
    public static final String ICON_LOCK = "\uf06a";

    /**
     * Milestone icon
     */
    public static final String ICON_MILESTONE = "\uf075";

    /**
     * Bookmark icon
     */
    public static final String ICON_BOOKMARK = "\uf07b";

    /**
     * Person icon
     */
    public static final String ICON_PERSON = "\uf218";

    /**
     * Add icon
     */
    public static final String ICON_ADD = "\uf05d";

    /**
     * Broadcast icon
     */
    public static final String ICON_BROADCAST = "\uf030";

    /**
     * Edit icon
     */
    public static final String ICON_EDIT = "\uf058";

    /**
     * Read/check icon
     */
    public static final String ICON_READ = "\uf03a";

    private static Typeface OCTICONS;

    public OcticonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTypeface(getOcticons());
    }

    /**
     * Returns Octicons typeface.
     */
    private Typeface getOcticons() {
        if (OCTICONS == null) {
            OCTICONS = getTypeface(getContext(), "octicons-regular-webfont.ttf");
        }
        return OCTICONS;
    }

    /**
     * Returns typeface from name.
     */
    private static Typeface getTypeface(final Context context, final String name) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }
}
