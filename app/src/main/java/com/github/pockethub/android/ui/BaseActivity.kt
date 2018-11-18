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
package com.github.pockethub.android.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import com.github.pockethub.android.R
import dagger.android.support.DaggerAppCompatActivity

/**
 * Activity that display dialogs
 */
abstract class BaseActivity : DaggerAppCompatActivity(), DialogResultListener {

    var optionsMenuListener: OptionsMenuListener? = null
    var menuCreated = false

    override fun onContentChanged() {
        super.onContentChanged()
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return parcelable
     */
    protected fun <V : Parcelable> getParcelableExtra(name: String): V? {
        return intent.getParcelableExtra(name)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return int
     */
    protected fun getIntExtra(name: String): Int {
        return intent.getIntExtra(name, -1)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return int array
     */
    protected fun getIntArrayExtra(name: String): IntArray? {
        return intent.getIntArrayExtra(name)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return boolean array
     */
    protected fun getBooleanArrayExtra(name: String): BooleanArray? {
        return intent.getBooleanArrayExtra(name)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return string
     */
    protected fun getStringExtra(name: String): String? {
        return intent.getStringExtra(name)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return string array
     */
    protected fun getStringArrayExtra(name: String): Array<String>? {
        return intent.getStringArrayExtra(name)
    }

    /**
     * Get intent extra
     *
     * @param name
     * @return char sequence array
     */
    protected fun getCharSequenceArrayExtra(name: String): Array<CharSequence>? {
        return intent.getCharSequenceArrayExtra(name)
    }

    override fun onDialogResult(requestCode: Int, resultCode: Int, arguments: Bundle) {
        // Intentionally left blank
    }

    @CallSuper
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionsMenuListener?.onCreateOptionsMenu(menu, menuInflater)
        val created = super.onCreateOptionsMenu(menu)
        //menuCreated = true
        return created
    }

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val usedEvent = optionsMenuListener?.onOptionsItemSelected(item)
        if (usedEvent != null && usedEvent) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @CallSuper
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        optionsMenuListener?.onPrepareOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    @CallSuper
    override fun invalidateOptionsMenu() {
        if (menuCreated) {
            super.invalidateOptionsMenu()
        }
    }
}
