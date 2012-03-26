package com.github.mobile.android.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.List;

/**
 * Describes how to store, load or request-an-update-for a particular set of data.
 */
public interface PersistableResource<E> {

    /**
     * @return a cursor capable of reading the required information out of the database.
     */
    Cursor getCursor(SQLiteDatabase readableDatabase);

    /**
     * @return a single item, read from this row of the cursor
     */
    E loadFrom(Cursor cursor);

    /**
     * Store supplied items in DB, removing or updating prior entries
     */
    void store(SQLiteDatabase writableDatabase, List<E> items);

    /**
     * Request the data directly from the GitHub API, rather than
     * attempting to load it from the DB cache.
     */
    List<E> request() throws IOException;
}
