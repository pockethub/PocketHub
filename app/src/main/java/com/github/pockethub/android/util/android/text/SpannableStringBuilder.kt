package com.github.pockethub.android.util.android.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.view.View
import androidx.text.inSpans
import com.github.pockethub.android.util.TimeUtils
import java.util.*

fun SpannableStringBuilder.monospace(builderAction: SpannableStringBuilder.() -> Unit) {
    inSpans(TypefaceSpan("monospace"), builderAction = builderAction)
}

fun SpannableStringBuilder.url(text: String, listener: (View) -> Unit) {
    setSpan(object : URLSpan(text) {
        override fun onClick(widget: View) {
            listener(widget)
        }
    }, length, length + text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

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
