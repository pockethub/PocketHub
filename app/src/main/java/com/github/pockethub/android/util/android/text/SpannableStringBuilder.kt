package com.github.pockethub.android.util.android.text

import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.text.inSpans
import com.github.pockethub.android.util.TimeUtils
import java.util.*

fun SpannableStringBuilder.monospace(builderAction: SpannableStringBuilder.() -> Unit) {
    inSpans(TypefaceSpan("monospace"), builderAction = builderAction)
}

fun SpannableStringBuilder.url(
        url: String,
        onClick: (View) -> Unit,
        builderAction: SpannableStringBuilder.() -> Unit
) = inSpans(object : URLSpan(url) {
    override fun onClick(widget: View) {
        onClick(widget)
    }
}, builderAction = builderAction)

fun SpannableStringBuilder.append(date: Date) {
    val time = TimeUtils.getRelativeTime(date)
    // Un-capitalize time string if there is already a prefix.
    // So you get "opened in 5 days" instead of "opened In 5 days".
    val timeLength = time.length
    if (length > 0 && timeLength > 0 && Character.isUpperCase(time[0])) {
        append(time.subSequence(0, 1).toString().toLowerCase(Locale.getDefault()))
        append(time.subSequence(1, timeLength))
    } else {
        append(time)
    }
}
