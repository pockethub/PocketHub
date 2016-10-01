package com.github.pockethub.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.StyledText;
import com.github.pockethub.android.util.AvatarLoader;
import com.github.pockethub.android.util.TimeUtils;
import com.google.inject.Inject;

import roboguice.RoboGuice;

public class IssueHeaderView extends LinearLayout {

    private TextView titleText;

    private TextView authorText;

    private TextView createdDateText;

    private ImageView creatorAvatar;

    @Inject
    @SuppressWarnings("unused")
    private AvatarLoader avatars;

    public IssueHeaderView(final Context context) {
        this(context, null);
    }

    public IssueHeaderView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IssueHeaderView(final Context context, final AttributeSet attrs, final int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IssueHeaderView(final Context context, final AttributeSet attrs, final int
            defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);

        if (!isInEditMode()) {
            RoboGuice.getInjector(getContext()).injectMembers(this);
        }

        inflate(getContext(), R.layout.view_issue_header, this);
        titleText = (TextView) findViewById(R.id.tv_issue_title);
        authorText = (TextView) findViewById(R.id.tv_issue_author);
        createdDateText = (TextView) findViewById(R.id.tv_issue_creation_date);
        creatorAvatar = (ImageView) findViewById(R.id.iv_avatar);
    }

    public void updateHeader(final Issue issue) {
        titleText.setText(issue.title);
        authorText.setText(issue.user.login);
        createdDateText.setText(new StyledText().append(
                getContext().getString(R.string.prefix_opened)).append(TimeUtils.stringToDate
                (issue.created_at)));
        avatars.bind(creatorAvatar, issue.user);
    }

}
