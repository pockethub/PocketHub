/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.android.ui.issue

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface.DEFAULT_BOLD
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.PaintDrawable
import android.text.style.DynamicDrawableSpan
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.github.pockethub.android.R
import com.github.pockethub.android.util.ServiceUtils
import com.meisolsson.githubsdk.model.Label
import java.lang.Integer.MIN_VALUE
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.util.*
import java.util.Locale.US


/**
 * Span that draws a [Label]
 *
 * @constructor Create background span for label
 */
class LabelDrawableSpan(private val resources: Resources, private val textSize: Float, color: String, private val paddingLeft: Float, private val textHeight: Float, private val bounds: Rect, private val name: String) : DynamicDrawableSpan() {

    private val color = Color.parseColor("#$color")

    /**
     * @constructor Create drawable for labels
     */
    private class LabelDrawable(private val paddingLeft: Float, private val textHeight: Float, bounds: Rect, resources: Resources, textSize: Float, private val name: String, bg: Int) : PaintDrawable() {

        private val height = bounds.height().toFloat()

        private val textColor: Int

        private val layers: LayerDrawable

        init {
            val hsv = FloatArray(3)
            Color.colorToHSV(bg, hsv)
            if (hsv[2] > 0.6 && hsv[1] < 0.4 || hsv[2] > 0.7 && hsv[0] > 40 && hsv[0] < 200) {
                hsv[2] = 0.4f
                textColor = Color.HSVToColor(hsv)
            } else {
                textColor = WHITE
            }

            layers = resources.getDrawable(R.drawable.label_background) as LayerDrawable
            ((layers
                    .findDrawableByLayerId(R.id.item_outer_layer) as LayerDrawable)
                    .findDrawableByLayerId(R.id.item_outer) as GradientDrawable).setColor(bg)
            ((layers
                    .findDrawableByLayerId(R.id.item_inner_layer) as LayerDrawable)
                    .findDrawableByLayerId(R.id.item_inner) as GradientDrawable).setColor(bg)
            (layers.findDrawableByLayerId(R.id.item_bg) as GradientDrawable)
                    .setColor(bg)

            paint.apply {
                isAntiAlias = true
                color = resources.getColor(android.R.color.transparent)
                typeface = DEFAULT_BOLD
                this.textSize = textSize
            }

            layers.bounds = bounds
            setBounds(bounds)
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)

            layers.draw(canvas)

            val paint = paint
            val original = paint.color

            paint.color = textColor
            canvas.drawText(name, paddingLeft, height - (height - textHeight) / 2, paint)

            paint.color = original
        }
    }

    override fun getDrawable(): Drawable =
            LabelDrawable(paddingLeft, textHeight, bounds, resources, textSize, name, color)

    companion object {

        /**
         * Set text on view to be given labels
         *
         * @param view
         * @param labels
         */
        @JvmStatic
        fun setText(view: TextView, labels: Collection<Label>) {
            val sortedLabels = labels.toTypedArray()
            Arrays.sort(sortedLabels) { lhs, rhs -> CASE_INSENSITIVE_ORDER.compare(lhs.name(), rhs.name()) }
            setText(view, sortedLabels)
        }

        /**
         * Set text on view to be given label
         *
         * @param view
         * @param label
         */
        @JvmStatic
        fun setText(view: TextView, label: Label) {
            setText(view, arrayOf(label))
        }

        private fun setText(view: TextView, labels: Array<Label>) {
            val resources = view.resources
            val paddingTop = resources.getDimension(R.dimen.label_padding_top)
            val paddingLeft = resources.getDimension(R.dimen.label_padding_left)
            val paddingRight = resources.getDimension(R.dimen.label_padding_right)
            val paddingBottom = resources.getDimension(R.dimen.label_padding_bottom)

            val p = Paint()
            p.typeface = DEFAULT_BOLD
            p.textSize = view.textSize

            val textBounds = Rect()
            val names = arrayOfNulls<String>(labels.size)
            val nameWidths = IntArray(labels.size)
            var textHeight = MIN_VALUE
            for (i in labels.indices) {
                val name = labels[i].name()!!.toUpperCase(US)
                textBounds.setEmpty()
                p.getTextBounds(name, 0, name.length, textBounds)
                names[i] = name
                textHeight = Math.max(textBounds.height(), textHeight)
                nameWidths[i] = textBounds.width()
            }

            val textSize = view.textSize
            view.text = buildSpannedString {
                for (i in labels.indices) {
                    val bounds = Rect()
                    bounds.right = Math.round(nameWidths[i].toFloat() + paddingLeft + paddingRight + 0.5f)
                    bounds.bottom = Math.round(textHeight.toFloat() + paddingTop + paddingBottom + 0.5f)

                    inSpans(LabelDrawableSpan(resources, textSize, labels[i].color()!!, paddingLeft, textHeight.toFloat(), bounds, names[i]!!)) {
                        append('\uFFFC')
                    }

                    if (i + 1 < labels.size) {
                        append(' ')
                    }
                }
            }
        }
    }
}
