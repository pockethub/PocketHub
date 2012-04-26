package com.github.mobile.android.ui.gist;

import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.R.layout;
import com.github.mobile.android.R.string;
import com.github.mobile.android.ui.ItemListAdapter;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.Time;

import java.text.NumberFormat;
import java.util.Arrays;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;

/**
 * Adapter to display a list of {@link Gist} objects
 */
public class GistListAdapter extends ItemListAdapter<Gist, GistView> {

    private static final int PRIVATE_ID_LENGTH = 7;

    /**
     * Find the maximum number of digits in the given Gist ids
     *
     * @param gists
     * @return max digits
     */
    private static int getMaxDigits(Object... gists) {
        int max = 1;
        for (Object item : gists) {
            Gist gist = (Gist) item;
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
     * @param maxDigits
     * @param gistId
     * @return id width
     */
    public static int getIdWidth(final int maxDigits, final TextView gistId) {
        Paint paint = new Paint();
        paint.setTypeface(gistId.getTypeface());
        paint.setTextSize(gistId.getTextSize());
        char[] text = new char[maxDigits];
        Arrays.fill(text, '0');
        return Math.round(paint.measureText(text, 0, text.length));
    }

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private final AvatarHelper avatarHelper;

    private int idWidth;

    private TextView idText;

    /**
     * @param avatarHelper
     * @param inflater
     * @param elements
     */
    public GistListAdapter(AvatarHelper avatarHelper, LayoutInflater inflater, Gist[] elements) {
        super(layout.gist_list_item, inflater, elements);

        this.avatarHelper = avatarHelper;

        idText = (TextView) inflater.inflate(layout.gist_list_item, null).findViewById(id.tv_gist_id);
        if (elements != null)
            computeIdWidth(elements);
    }

    private void computeIdWidth(Object[] items) {
        int digits = getMaxDigits(items);
        idWidth = getIdWidth(digits, idText);
    }

    /**
     * @param avatarHelper
     * @param inflater
     */
    public GistListAdapter(AvatarHelper avatarHelper, LayoutInflater inflater) {
        this(avatarHelper, inflater, null);
    }

    @Override
    public ItemListAdapter<Gist, GistView> setItems(Object[] items) {
        computeIdWidth(items);

        return super.setItems(items);
    }

    @Override
    protected void update(final GistView view, final Gist gist) {
        view.gistId.getLayoutParams().width = idWidth;
        String id = gist.getId();
        CharSequence description = gist.getDescription();
        if (!gist.isPublic() && id.length() > PRIVATE_ID_LENGTH)
            id = id.substring(0, PRIVATE_ID_LENGTH) + "…";
        view.gistId.setText(id);

        if (TextUtils.isEmpty(description))
            description = Html.fromHtml("<i>" + view.title.getContext().getString(string.no_description) + "</i>");
        view.title.setText(description);

        User user = gist.getUser();
        if (avatarHelper != null && user != null) {
            avatarHelper.bind(view.avatar, user);
            view.created
                    .setText(fromHtml("<b>" + user.getLogin() + "</b> " + Time.relativeTimeFor(gist.getCreatedAt())));
        } else {
            view.created.setText(Time.relativeTimeFor(gist.getCreatedAt()));
            view.avatar.setVisibility(GONE);
        }
        view.files.setText(NUMBER_FORMAT.format(gist.getFiles().size()));
        view.comments.setText(NUMBER_FORMAT.format(gist.getComments()));
    }

    @Override
    protected GistView createView(View view) {
        return new GistView(view);
    }
}
