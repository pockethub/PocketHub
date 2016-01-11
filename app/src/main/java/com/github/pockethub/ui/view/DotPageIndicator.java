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

package com.github.pockethub.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.github.pockethub.R;
import com.github.pockethub.ui.ViewPager;

public class DotPageIndicator extends View implements android.support.v4.view.ViewPager.OnPageChangeListener {

    private static final Paint PAINT_SELECTED = new Paint();
    private static final Paint PAINT_NOT_SELECTED = new Paint();
    /**
     * Radius of a dot
     */
    private float dotRadius;

    /**
     * Spacing between dots, can be set by app:spacing in the xml
     */
    private float dotSpacing;

    /**
     * The width without the padding, set in onSizedChanged
     */
    private float currentWidth;

    /**
     * The height without the padding, set in onSizedChanged
     */
    private float currentHeight;

    /**
     * The ViewPager which is currently set
     */
    private ViewPager viewPager;

    /**
     * If the drawing values needs to change (Only if size changed)
     */
    private boolean updateValues;

    /**
     * Number of dots per row
     */
    private int dotColumns;

    /**
     * Number of rows in the view
     */
    private int dotRows;

    /**
     * The standard alpha from the selected dot color
     */
    private int dotAlpha;

    /**
     * Fields for when the dot is moving
     */
    private float amount;
    private int currentPos;
    private int nextPos;


    /**
     * Instantiates the view and it's values
     * @param context
     * @param attrs
     */
    public DotPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode())
            return;

        Resources res = getResources();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DotPageIndicator,
                0, 0);

        try {
            PAINT_SELECTED.setColor(a.getColor(R.styleable.DotPageIndicator_dotColorSelected,
                    getColor(res, android.R.color.secondary_text_light , context.getTheme())));

            PAINT_NOT_SELECTED.setColor(a.getColor(R.styleable.DotPageIndicator_dotColor,
                    getColor(res, android.R.color.secondary_text_dark , context.getTheme())));

            dotSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
            dotRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, res.getDisplayMetrics());

            dotRadius = a.getDimensionPixelSize(R.styleable.DotPageIndicator_dotRadius, (int) dotRadius);
            dotSpacing = a.getDimensionPixelSize(R.styleable.DotPageIndicator_dotSpacing, (int) dotSpacing);
        }finally {
            a.recycle();
        }
        dotAlpha = PAINT_SELECTED.getAlpha();
    }

    public void setDotColor(@ColorInt int color) {
        PAINT_NOT_SELECTED.setColor(color);
    }

    public void setSelectedDotColor(@ColorInt int color) {
        PAINT_SELECTED.setColor(color);
    }

    /**
     * Sets up the size of the view and saves it for further use
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(viewPager != null)
            createOnDrawValues();

        int desiredWidth = (int) getWidthOfDots(dotColumns);
        int desiredHeight = (int) getHeightOfDots(dotRows);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);

        float horiPadding = getPaddingLeft() + getPaddingRight();
        float vertPadding = getPaddingBottom() + getPaddingTop();

        currentWidth = width - horiPadding;
        currentHeight = height - vertPadding;
        updateValues = true;
    }


    /**
     * Updates values for drawing
     */
    private void createOnDrawValues(){
        int pageAmount = getNumberOfPages();
        float w = getWidthOfDots(pageAmount);

        dotRows = 1;
        dotColumns = pageAmount;

        if (currentWidth > 0 && w > currentWidth) {
            for (int i = 1; i < pageAmount; i++) {
                float tempWidth = currentWidth - getWidthOfDots(i);
                if (tempWidth <= 0) {
                    dotRows = (int) Math.ceil(pageAmount / i - 1);
                    dotColumns = i - 1;
                    break;
                }
            }

            if(getHeightOfDots(dotRows) > currentHeight)
                throw new IllegalStateException("Can't fit DotPageIndicator");
        }
        updateValues = false;
    }

    /**
     * Draws the dots
     *
     * @param canvas The canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (updateValues)
            createOnDrawValues();

        for (int i = 0; i < dotRows; i++) {
            int dotDiff = getNumberOfPages() - ((i + 1) * dotColumns);
            int dots = dotDiff < 0 ? dotDiff + dotColumns : dotColumns;
            float rowCenterY = (i * dotRadius * 2) + (i * dotSpacing) + dotRadius;

            drawRow(canvas, dots, currentWidth, rowCenterY);
        }

        drawSelectedDot(canvas);
    }

    /**
     * Draw the selected dot and when it moves
     *
     * @param canvas The canvas to draw on
     */

    private void drawSelectedDot(Canvas canvas) {
        float distance = (dotRadius * 2 + dotSpacing) * amount;

        float fromPosX = getDotCenterX(currentPos);
        float fromPosY = getDotCenterY(currentPos);

        float toPosX = getDotCenterX(nextPos);
        float toPosY = getDotCenterY(nextPos);

        if (toPosY != fromPosY) {
            PAINT_SELECTED.setAlpha(Math.round(dotAlpha * amount));
            canvas.drawCircle(toPosX - ((dotRadius * 2 + dotSpacing) - distance), toPosY, dotRadius, PAINT_SELECTED);
            PAINT_SELECTED.setAlpha(Math.round(dotAlpha * (1 - amount)));
        }

        canvas.drawCircle(fromPosX + distance, fromPosY, dotRadius, PAINT_SELECTED);
        PAINT_SELECTED.setAlpha(dotAlpha);
    }

    /**
     * Draws a row of dots
     *
     * @param canvas The canvas to draw on
     * @param columns Number of dots in the row
     * @param w Width of the view to draw on
     * @param y Y position of the row (center)
     */
    private void drawRow(Canvas canvas, int columns, float w, float y) {
        float center = w / 2;
        float start = center - getWidthOfDots(columns / 2);
        start -= columns % 2 == 0 ? dotSpacing / 2 : dotRadius + dotSpacing;

        for (int i = 0; i < columns; i++) {
            canvas.drawCircle(start + dotRadius, y, dotRadius, PAINT_NOT_SELECTED);
            start += (dotRadius * 2) + dotSpacing;
        }
    }

    /**
     * A helper method for getting colors from resource id
     *
     * @param res A reference to resources
     * @param id Resource id to the color
     * @param theme A theme reference
     * @return The color specified
     */
    private int getColor(Resources res, int id, Resources.Theme theme){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return res.getColor(id, theme);
        else
            return res.getColor(id);
    }

    /**
     * Gets the center x position of the dot in position {@param index}
     *
     * @param index The dots index
     * @return The x position
     */
    private float getDotCenterX(int index) {
        int column = index % dotColumns;
        int row = (index - column) / dotColumns;
        int columns = dotColumns;

        if(dotColumns * (row + 1) > getNumberOfPages())
            columns = dotColumns - ((dotColumns * (row + 1)) - getNumberOfPages());

        float center = currentWidth / 2;
        float start = center - getWidthOfDots(columns / 2);

        start -= columns % 2 == 0 ? dotSpacing / 2 : dotRadius + dotSpacing;
        start += ((dotRadius * 2) + dotSpacing) * column;

        return start + dotRadius;
    }

    /**
     * Gets the center y position of the dot in position {@param index}
     *
     * @param index The dots index
     * @return The y position
     */
    private float getDotCenterY(int index) {
        int column = index % dotColumns;
        int row = (index - column) / dotColumns;

        return (row * dotRadius * 2) + (row * dotSpacing) + dotRadius;
    }

    /**
     * Gets the width of some dots
     *
     * @param amount The amount of dots to measure
     * @return The calculated width
     */
    private float getWidthOfDots(int amount) {
        return (amount * dotRadius * 2) + ((amount - 1) * dotSpacing);
    }

    /**
     * Gets the height of some rows of dots
     *
     * @param rows The rows of dots to measure
     * @return The calculated height
     */
    private float getHeightOfDots(int rows){
        return (rows * dotRadius * 2) + ((rows-1) * dotSpacing);
    }

    /**
     * The amount of pages in the {@link ViewPager}
     *
     * @return Number of pages
     */
    private int getNumberOfPages() {
        return viewPager != null ? viewPager.getAdapter().getCount(): -1;
    }


    /**
     * Sets the viewpager to use
     *
     * @param pager ViewPager
     */
    public void setViewPager(ViewPager pager) {
        if (pager == null)
            return;

        if(viewPager != null)
            viewPager.removeOnPageChangeListener(this);

        viewPager = pager;
        pager.addOnPageChangeListener(this);
    }

    /**
     * Animates the dot
     *
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        amount = positionOffset;
        currentPos = positionOffsetPixels >= 0 ? position : position + 1;
        nextPos = positionOffsetPixels >= 0 ? position + 1 : position;

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        //Not used
    }

    /**
     * Sets current item
     *
     * @param state
     */
    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
            currentPos = viewPager.getCurrentItem();

        }
    }
}
