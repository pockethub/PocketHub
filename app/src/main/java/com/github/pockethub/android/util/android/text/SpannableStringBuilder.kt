package com.github.pockethub.android.util.android.text

import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.core.text.inSpans
import com.github.pockethub.android.util.TimeUtils
import java.util.*

fun SpannableStringBuilder.monospace(builderAction: SpannableStringBuilder.() -> Unit) {
    inSpans(TypefaceSpan("monospace"), builderAction = builderAction)
}

fun SpannableStringBuilder.clickable(
    onClick: (View) -> Unit,
    builderAction: SpannableStringBuilder.() -> Unit
) = inSpans(object : ClickableSpan() {
    override fun onClick(widget: View) {
        onClick(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
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
