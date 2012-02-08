package com.github.mobile.android.util;

import android.content.Context;
import android.widget.Toast;

import org.eclipse.egit.github.core.client.RequestException;

/**
 * Helpers to display errors to the user
 */
public class ErrorHelper {

    /**
     * Default duration to show {@link Toast}
     */
    public static final int DEFAULT_DURATION = 5000;

    /**
     * Show {@link Toast} for exception
     * <p>
     * This given default message will be used if an message can not be derived from the given {@link Exception}
     * <p>
     * This method shows the {@link Toast} for 5 seconds
     *
     * @see #show(Context, Exception, int, int)
     * @param context
     * @param e
     * @param defaultMessage
     */
    public static void show(final Context context, final Exception e, final int defaultMessage) {
        show(context, e, defaultMessage, DEFAULT_DURATION);
    }

    /**
     * Show {@link Toast} for exception
     * <p>
     * This given default message will be used if an message can not be derived from the given {@link Exception}
     *
     * @param context
     * @param e
     * @param defaultMessage
     * @param duration
     */
    public static void show(final Context context, final Exception e, final int defaultMessage, final int duration) {
        String message = null;
        if (e instanceof RequestException)
            message = ((RequestException) e).formatErrors();

        if (message == null || message.length() == 0)
            message = context.getString(defaultMessage);

        if (message != null && message.length() > 0)
            Toast.makeText(context, message, duration).show();
    }
}
