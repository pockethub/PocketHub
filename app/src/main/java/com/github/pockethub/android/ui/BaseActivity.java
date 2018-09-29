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
package com.github.pockethub.android.ui;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.pockethub.android.R;
import dagger.android.support.DaggerAppCompatActivity;

/**
 * Activity that display dialogs
 */
public abstract class BaseActivity extends DaggerAppCompatActivity implements DialogResultListener {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return parcelable
     */
    @SuppressWarnings("unchecked")
    protected <V extends Parcelable> V getParcelableExtra(final String name) {
        return (V) getIntent().getParcelableExtra(name);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return int
     */
    protected int getIntExtra(final String name) {
        return getIntent().getIntExtra(name, -1);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return int array
     */
    protected int[] getIntArrayExtra(final String name) {
        return getIntent().getIntArrayExtra(name);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return boolean array
     */
    protected boolean[] getBooleanArrayExtra(final String name) {
        return getIntent().getBooleanArrayExtra(name);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return string
     */
    protected String getStringExtra(final String name) {
        return getIntent().getStringExtra(name);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return string array
     */
    protected String[] getStringArrayExtra(final String name) {
        return getIntent().getStringArrayExtra(name);
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return char sequence array
     */
    protected CharSequence[] getCharSequenceArrayExtra(final String name) {
        return getIntent().getCharSequenceArrayExtra(name);
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        // Intentionally left blank
    }
}
