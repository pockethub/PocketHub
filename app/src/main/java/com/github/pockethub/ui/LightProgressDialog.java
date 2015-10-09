/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.pockethub.R;


/**
 * Progress dialog in Holo Light theme
 */
public class LightProgressDialog {

    /**
     * Create progress dialog
     *
     * @param context
     * @param resId
     * @return dialog
     */
    public static AlertDialog create(Context context, int resId) {
        return create(context, context.getResources().getString(resId));
    }

    /**
     * Create progress dialog
     *
     * @param context
     * @param message
     * @return dialog
     */
    public static AlertDialog create(Context context, CharSequence message) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.progress_dialog, null);

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
        progress.setIndeterminate(true);
        progress.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.spinner));

        final TextView messageView = (TextView) view.findViewById(R.id.message);
        messageView.setText(message);
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                                          .setIcon(R.drawable.spinner)
                                          .setView(view)
                                          .create();
        return new AlertDialog(context) {
            public Button getButton(int whichButton) {
                return alertDialog.getButton(whichButton);
            }

            public ListView getListView() {
                return alertDialog.getListView();
            }

            public void setTitle(CharSequence title) {
                alertDialog.setTitle(title);
            }

            public void setCustomTitle(View customTitleView) {
                alertDialog.setCustomTitle(customTitleView);
            }

            public void setMessage(CharSequence message) {
                messageView.setText(message);
            }

            public void setView(View view) {
                alertDialog.setView(view);
            }

            public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
                alertDialog.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
            }

            public void setButton(int whichButton, CharSequence text, Message msg) {
                alertDialog.setButton(whichButton, text, msg);
            }

            public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
                alertDialog.setButton(whichButton, text, listener);
            }

            public void setIcon(int resId) {
                alertDialog.setIcon(resId);
            }

            public void setIcon(Drawable icon) {
                alertDialog.setIcon(icon);
            }

            public void setIconAttribute(int attrId) {
                alertDialog.setIconAttribute(attrId);
            }

            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return alertDialog.onKeyDown(keyCode, event);
            }

            public boolean onKeyUp(int keyCode, KeyEvent event) {
                return alertDialog.onKeyUp(keyCode, event);
            }

            public ActionBar getSupportActionBar() {
                return alertDialog.getSupportActionBar();
            }

            public void setContentView(int layoutResID) {
                alertDialog.setContentView(layoutResID);
            }

            public void setContentView(View view) {
                alertDialog.setContentView(view);
            }

            public void setContentView(View view, ViewGroup.LayoutParams params) {
                alertDialog.setContentView(view, params);
            }

            public void setTitle(int titleId) {
                alertDialog.setTitle(titleId);
            }

            public void addContentView(View view, ViewGroup.LayoutParams params) {
                alertDialog.addContentView(view, params);
            }

            public boolean supportRequestWindowFeature(int featureId) {
                return alertDialog.supportRequestWindowFeature(featureId);
            }

            public void invalidateOptionsMenu() {
                alertDialog.invalidateOptionsMenu();
            }

            public AppCompatDelegate getDelegate() {
                return alertDialog.getDelegate();
            }

            public void onSupportActionModeStarted(ActionMode mode) {
                alertDialog.onSupportActionModeStarted(mode);
            }

            public void onSupportActionModeFinished(ActionMode mode) {
                alertDialog.onSupportActionModeFinished(mode);
            }

            @Nullable
            public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
                return alertDialog.onWindowStartingSupportActionMode(callback);
            }

            public android.app.ActionBar getActionBar() {
                return alertDialog.getActionBar();
            }

            public boolean isShowing() {
                return alertDialog.isShowing();
            }

            public void create() {
                alertDialog.create();
            }

            public void show() {
                alertDialog.show();
            }

            public void hide() {
                alertDialog.hide();
            }

            public void dismiss() {
                alertDialog.dismiss();
            }

            public Bundle onSaveInstanceState() {
                return alertDialog.onSaveInstanceState();
            }

            public void onRestoreInstanceState(Bundle savedInstanceState) {
                alertDialog.onRestoreInstanceState(savedInstanceState);
            }

            public Window getWindow() {
                return alertDialog.getWindow();
            }

            public View getCurrentFocus() {
                return alertDialog.getCurrentFocus();
            }

            public View findViewById(int id) {
                return alertDialog.findViewById(id);
            }

            public boolean onKeyLongPress(int keyCode, KeyEvent event) {
                return alertDialog.onKeyLongPress(keyCode, event);
            }

            public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
                return alertDialog.onKeyMultiple(keyCode, repeatCount, event);
            }

            public void onBackPressed() {
                alertDialog.onBackPressed();
            }

            public boolean onKeyShortcut(int keyCode, KeyEvent event) {
                return alertDialog.onKeyShortcut(keyCode, event);
            }

            public boolean onTouchEvent(MotionEvent event) {
                return alertDialog.onTouchEvent(event);
            }

            public boolean onTrackballEvent(MotionEvent event) {
                return alertDialog.onTrackballEvent(event);
            }

            public boolean onGenericMotionEvent(MotionEvent event) {
                return alertDialog.onGenericMotionEvent(event);
            }

            public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
                alertDialog.onWindowAttributesChanged(params);
            }

            public void onContentChanged() {
                alertDialog.onContentChanged();
            }

            public void onWindowFocusChanged(boolean hasFocus) {
                alertDialog.onWindowFocusChanged(hasFocus);
            }

            public void onAttachedToWindow() {
                alertDialog.onAttachedToWindow();
            }

            public void onDetachedFromWindow() {
                alertDialog.onDetachedFromWindow();
            }

            public boolean dispatchKeyEvent(KeyEvent event) {
                return alertDialog.dispatchKeyEvent(event);
            }

            public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                return alertDialog.dispatchKeyShortcutEvent(event);
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                return alertDialog.dispatchTouchEvent(ev);
            }

            public boolean dispatchTrackballEvent(MotionEvent ev) {
                return alertDialog.dispatchTrackballEvent(ev);
            }

            public boolean dispatchGenericMotionEvent(MotionEvent ev) {
                return alertDialog.dispatchGenericMotionEvent(ev);
            }

            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                return alertDialog.dispatchPopulateAccessibilityEvent(event);
            }

            public View onCreatePanelView(int featureId) {
                return alertDialog.onCreatePanelView(featureId);
            }

            public boolean onCreatePanelMenu(int featureId, Menu menu) {
                return alertDialog.onCreatePanelMenu(featureId, menu);
            }

            public boolean onPreparePanel(int featureId, View view, Menu menu) {
                return alertDialog.onPreparePanel(featureId, view, menu);
            }

            public boolean onMenuOpened(int featureId, Menu menu) {
                return alertDialog.onMenuOpened(featureId, menu);
            }

            public boolean onMenuItemSelected(int featureId, MenuItem item) {
                return alertDialog.onMenuItemSelected(featureId, item);
            }

            public void onPanelClosed(int featureId, Menu menu) {
                alertDialog.onPanelClosed(featureId, menu);
            }

            public boolean onCreateOptionsMenu(Menu menu) {
                return alertDialog.onCreateOptionsMenu(menu);
            }

            public boolean onPrepareOptionsMenu(Menu menu) {
                return alertDialog.onPrepareOptionsMenu(menu);
            }

            public boolean onOptionsItemSelected(MenuItem item) {
                return alertDialog.onOptionsItemSelected(item);
            }

            public void onOptionsMenuClosed(Menu menu) {
                alertDialog.onOptionsMenuClosed(menu);
            }

            public void openOptionsMenu() {
                alertDialog.openOptionsMenu();
            }

            public void closeOptionsMenu() {
                alertDialog.closeOptionsMenu();
            }

            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                alertDialog.onCreateContextMenu(menu, v, menuInfo);
            }

            public void registerForContextMenu(View view) {
                alertDialog.registerForContextMenu(view);
            }

            public void unregisterForContextMenu(View view) {
                alertDialog.unregisterForContextMenu(view);
            }

            public void openContextMenu(View view) {
                alertDialog.openContextMenu(view);
            }

            public boolean onContextItemSelected(MenuItem item) {
                return alertDialog.onContextItemSelected(item);
            }

            public void onContextMenuClosed(Menu menu) {
                alertDialog.onContextMenuClosed(menu);
            }

            public boolean onSearchRequested(SearchEvent searchEvent) {
                return alertDialog.onSearchRequested(searchEvent);
            }

            public boolean onSearchRequested() {
                return alertDialog.onSearchRequested();
            }

            public android.view.ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback) {
                return alertDialog.onWindowStartingActionMode(callback);
            }

            public android.view.ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback, int type) {
                return alertDialog.onWindowStartingActionMode(callback, type);
            }

            public void onActionModeStarted(android.view.ActionMode mode) {
                alertDialog.onActionModeStarted(mode);
            }

            public void onActionModeFinished(android.view.ActionMode mode) {
                alertDialog.onActionModeFinished(mode);
            }

            public void takeKeyEvents(boolean get) {
                alertDialog.takeKeyEvents(get);
            }

            public LayoutInflater getLayoutInflater() {
                return alertDialog.getLayoutInflater();
            }

            public void setCancelable(boolean flag) {
                alertDialog.setCancelable(flag);
            }

            public void setCanceledOnTouchOutside(boolean cancel) {
                alertDialog.setCanceledOnTouchOutside(cancel);
            }

            public void cancel() {
                alertDialog.cancel();
            }

            public void setOnCancelListener(OnCancelListener listener) {
                alertDialog.setOnCancelListener(listener);
            }

            public void setCancelMessage(Message msg) {
                alertDialog.setCancelMessage(msg);
            }

            public void setOnDismissListener(OnDismissListener listener) {
                alertDialog.setOnDismissListener(listener);
            }

            public void setOnShowListener(OnShowListener listener) {
                alertDialog.setOnShowListener(listener);
            }

            public void setDismissMessage(Message msg) {
                alertDialog.setDismissMessage(msg);
            }

            public void setOnKeyListener(OnKeyListener onKeyListener) {
                alertDialog.setOnKeyListener(onKeyListener);
            }
        };
    }
}
