package com.github.mobile.android.persistence;

import static com.google.common.collect.Lists.newArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.List;

/**
 * Given a PersistableResource, this class will take support loading/storing it's data or requesting
 * fresh data, as appropriate.
 */
public class DBCache {
    
    @Inject
    Provider<AccountDataManager.CacheHelper> helperProvider;

    <E> List<E> loadOrRequest(PersistableResource<E> persistableResource) throws IOException {
        SQLiteOpenHelper helper = helperProvider.get();
        try {
            List<E> items = loadFromDB(helper, persistableResource);

            return items == null ? requestAndStore(helper, persistableResource) : items;
        } finally {
            helper.close();
        }
    }

    public <E> List<E> requestAndStore(PersistableResource<E> persistableResource) {
        SQLiteOpenHelper helper = helperProvider.get();
        try {
            return requestAndStore(helper, persistableResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    private <E> List<E> requestAndStore(SQLiteOpenHelper helper, PersistableResource<E> persistableResource) throws
            IOException {
        List<E> items = persistableResource.request();

        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();

            persistableResource.store(db, items);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return items;
    }

    private <E> List<E> loadFromDB(SQLiteOpenHelper helper, PersistableResource<E> persistableResource) {
        Cursor cursor = persistableResource.getCursor(helper.getReadableDatabase());
        try {
            if (cursor.moveToFirst()) {
                List<E> cached = newArrayList();
                do {
                    cached.add(persistableResource.loadFrom(cursor));
                } while (cursor.moveToNext());
                return cached;
            }
        } finally {
            cursor.close();
        }
        return null;
    }

}
