package com.github.mobile.android.gist;

import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.Time;
import com.madgag.android.listviews.ViewHolder;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.Gist;

/**
 * View holder for a {@link Gist}
 */
public class GistViewHolder implements ViewHolder<Gist> {

    private static final int PRIVATE_ID_LENGTH = 7;

    /**
     * Find the maximum number of digits in the given Gist ids
     *
     * @param gists
     * @return max digits
     */
    public static int computeMaxDigits(Iterable<Gist> gists) {
        int max = 1;
        for (Gist gist : gists) {
            if (gist.isPublic())
                max = Math.max(max, (int) Math.log10(Long.parseLong(gist.getId())) + 1);
            else
                max = Math.max(max, PRIVATE_ID_LENGTH + 1);
        }
        return max;
    }

    /**
     * Measure the width of the given id field
     *
     * @param gists
     * @param gistId
     * @return id width
     */
    public static int computeIdWidth(final Iterable<Gist> gists, final TextView gistId) {
        final int maxDigigts = computeMaxDigits(gists);
        Paint paint = new Paint();
        paint.setTypeface(gistId.getTypeface());
        paint.setTextSize(gistId.getTextSize());
        char[] text = new char[maxDigigts];
        Arrays.fill(text, '0');
        return Math.round(paint.measureText(text, 0, text.length));
    }

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private AvatarHelper avatarHelper;

    private final TextView gistId;

    private final TextView title;

    private final TextView created;

    private final TextView comments;

    private final TextView files;

    private final ImageView avatar;

    private final AtomicReference<Integer> idWidth;

    /**
     * Create view holder for a {@link Gist}
     *
     * @param v
     * @param idWidth
     * @param avatarHelper
     */
    public GistViewHolder(View v, AtomicReference<Integer> idWidth, AvatarHelper avatarHelper) {
        gistId = (TextView) v.findViewById(id.tv_gist_id);
        title = (TextView) v.findViewById(id.tv_gist_title);
        created = (TextView) v.findViewById(id.tv_gist_creation);
        comments = (TextView) v.findViewById(id.tv_gist_comments);
        files = (TextView) v.findViewById(id.tv_gist_files);
        avatar = (ImageView) v.findViewById(id.iv_gravatar);
        this.avatarHelper = avatarHelper;
        this.idWidth = idWidth;
    }

    /**
     * Create view holder for a {@link Gist}
     *
     * @param v
     * @param maxNumberCount
     */
    public GistViewHolder(View v, AtomicReference<Integer> maxNumberCount) {
        this(v, maxNumberCount, null);
    }

    @Override
    public void updateViewFor(final Gist gist) {
        gistId.getLayoutParams().width = idWidth.get();
        String id = gist.getId();
        CharSequence description = gist.getDescription();
        if (!gist.isPublic() && id.length() > PRIVATE_ID_LENGTH)
            id = id.substring(0, PRIVATE_ID_LENGTH) + "…";
        gistId.setText(id);

        if (TextUtils.isEmpty(description))
            description = Html.fromHtml("<i>No description</i>");
        title.setText(description);

        if (avatarHelper != null) {
            avatar.setImageDrawable(null);
            avatarHelper.bind(avatar, gist.getUser());
            created.setText(fromHtml("<b>" + gist.getUser().getLogin() + "</b> "
                    + Time.relativeTimeFor(gist.getCreatedAt())));
        } else {
            created.setText(Time.relativeTimeFor(gist.getCreatedAt()));
            avatar.setVisibility(GONE);
        }
        files.setText(NUMBER_FORMAT.format(gist.getFiles().size()));
        comments.setText(NUMBER_FORMAT.format(gist.getComments()));
    }
}
