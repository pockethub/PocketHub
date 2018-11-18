package com.github.pockethub.android.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

interface OptionsMenuListener {
    fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?): Boolean
    fun onOptionsItemSelected(item: MenuItem?): Boolean
    fun onPrepareOptionsMenu(menu: Menu?): Boolean
    fun invalidateOptionsMenu()
}
