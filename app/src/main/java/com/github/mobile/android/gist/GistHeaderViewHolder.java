package com.github.mobile.android.gist;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import java.util.Date;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

/**
 * Holder for a Gist header view
 */
public class GistHeaderViewHolder implements ViewHolder<Gist> {

    private final ImageView gravatar;

    private final TextView created;

    private final TextView description;

    private final AvatarHelper avatarHelper;

    /**
     * Create view holder
     *
     * @param view
     * @param avatarHelper
     */
    public GistHeaderViewHolder(final View view, final AvatarHelper avatarHelper) {
        gravatar = (ImageView) view.findViewById(id.iv_gravatar);
        created = (TextView) view.findViewById(id.tv_gist_creation);
        description = (TextView) view.findViewById(id.tv_gist_description);
        this.avatarHelper = avatarHelper;
    }

    public void updateViewFor(Gist gist) {
        User user = gist.getUser();
        Date createdAt = gist.getCreatedAt();
        CharSequence createdTime = createdAt != null ? Time.relativeTimeFor(gist.getCreatedAt()) : "";
        if (user != null) {
            gravatar.setVisibility(VISIBLE);
            gravatar.setImageDrawable(null);
            avatarHelper.bind(gravatar, user);
            created.setText(Html.fromHtml("<b>" + user.getLogin() + "</b> " + createdTime.toString()));
        } else {
            created.setText(createdTime);
            gravatar.setVisibility(GONE);
        }

        String desc = gist.getDescription();
        if (!TextUtils.isEmpty(desc))
            description.setText(desc);
        else
            description.setText(Html.fromHtml("<i>No description</i>"));
    }
}
