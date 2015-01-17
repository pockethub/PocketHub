package com.github.mobile.ui.roboactivities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.inject.Key;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnConfigurationChangedEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnRestartEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnStartEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.event.EventManager;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

/**
 * This is a base activity that adds Roboguice support for AppCompat's ActionBarActivity
 * <p>
 * Based on <a href="https://github.com/mccrajs">@mccrajs's</a> implementation <a href="https://github.com/metova/roboguice-appcompat/blob/master/src/com/metova/roboguice/appcompat/RoboActionBarActivity.java">here</a>.
 */
public class RoboActionBarActivity extends ActionBarActivity implements RoboContext {

    protected EventManager eventManager;
    protected HashMap<Key<?>, Object> scopedObjects = new HashMap<>();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {

        final RoboInjector injector = RoboGuice.getInjector(this);
        eventManager = injector.getInstance( EventManager.class );
        injector.injectMembersWithoutViews( this );
        super.onCreate( savedInstanceState );
        eventManager.fire( new OnCreateEvent( savedInstanceState ) );
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        eventManager.fire( new OnRestartEvent() );
    }

    @Override
    protected void onStart() {

        super.onStart();
        eventManager.fire( new OnStartEvent() );
    }

    @Override
    protected void onResume() {

        super.onResume();
        eventManager.fire( new OnResumeEvent() );
    }

    @Override
    protected void onPause() {

        super.onPause();
        eventManager.fire( new OnPauseEvent() );
    }

    @Override
    protected void onNewIntent( Intent intent ) {

        super.onNewIntent( intent );
        eventManager.fire( new OnNewIntentEvent() );
    }

    @Override
    protected void onStop() {

        try {
            eventManager.fire( new OnStopEvent() );
        }
        finally {
            super.onStop();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            eventManager.fire( new OnDestroyEvent() );
        }
        finally {
            try {
                RoboGuice.destroyInjector( this );
            }
            finally {
                super.onDestroy();
            }
        }
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {

        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged( newConfig );
        eventManager.fire( new OnConfigurationChangedEvent( currentConfig, newConfig ) );
    }

    @Override
    public void onSupportContentChanged() {

        super.onSupportContentChanged();
        RoboGuice.getInjector( this ).injectViewMembers( this );
        eventManager.fire( new OnContentChangedEvent() );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {

        super.onActivityResult( requestCode, resultCode, data );
        eventManager.fire( new OnActivityResultEvent( requestCode, resultCode, data ) );
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {

        return scopedObjects;
    }
}
