package com.github.mobile.android.util;

import static android.view.View.VISIBLE;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.github.kevinsawicki.http.HttpRequest;

import roboguice.util.RoboAsyncTask;

/**
 * Avatar utilities
 */
public class Avatar {

    private static final String TAG = "GHAU";

    private static class AvatarDbHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "avatars";

        private static final String COL_AVATAR = "avatar";

        private static final String COL_LOGIN = "login";

        private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + COL_LOGIN
                + " TEXT PRIMARY KEY, " + COL_AVATAR + " BLOB);";

        private AvatarDbHelper(Context context) {
            super(context, TABLE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        public byte[] getAvatar(String login) {
            SQLiteDatabase db = getReadableDatabase();
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(TABLE_NAME);
            Cursor cursor = builder.query(db, new String[] { COL_AVATAR }, COL_LOGIN + "='" + login + "'", null, null,
                    null, null);
            try {
                if (cursor.moveToFirst())
                    return cursor.getBlob(0);
                else
                    return null;
            } finally {
                cursor.close();
            }
        }

        public void putAvatar(String login, byte[] image) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues(2);
            values.put(COL_LOGIN, login);
            values.put(COL_AVATAR, image);
            db.insert(TABLE_NAME, null, values);
        }
    }

    /**
     * Bind view to image at URL
     *
     * @param context
     * @param view
     * @param login
     * @param avatarUrl
     */
    public static void bind(final Context context, final ImageView view, final String login, final String avatarUrl) {
        if (avatarUrl == null)
            return;

        final AvatarDbHelper helper = new AvatarDbHelper(context);
        byte[] image = helper.getAvatar(login);
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            view.setImageBitmap(Image.roundCorners(bitmap, 6));
            view.setVisibility(VISIBLE);
            helper.close();
            return;
        }
        new RoboAsyncTask<byte[]>(context) {

            public byte[] call() throws Exception {
                HttpRequest request = HttpRequest.get(avatarUrl);
                if (!request.ok())
                    return null;

                byte[] content = request.bytes();
                helper.putAvatar(login, content);
                return content;
            }

            protected void onSuccess(byte[] image) throws Exception {
                if (image == null || image.length == 0)
                    return;
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                view.setImageBitmap(Image.roundCorners(bitmap, 6));
                view.setVisibility(VISIBLE);
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Avatar load failed", e);
            }

            protected void onFinally() throws RuntimeException {
                helper.close();
            };
        }.execute();
    }
}
