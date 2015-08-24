package com.android.settings.wifi;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by manojk3 on 8/20/15.
 */
public class SsidProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.android.settings.wifi.SsidProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/cte";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String name = "ssidName";
    static final String ssidPassword = "ssidPassword";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "cte", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "cte/*", uriCode);
    }

    @Override
    public boolean onCreate() {
        Log.d("ssIDProvidor", "Manoj : onCreate");

        return createDb();
    }

    private boolean createDb() {
        Log.d("SsidProvider", "Manoj : creatDB()");
        Context context = getContext();
        if(context == null){
            Log.d("SsidProvider", "Manoj : Context is null");
        }
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        if (db != null) {
            return true;
        }
        return false;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Log.d("SsidProvider", "Manoj : Cursor query");

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TABLE_NAME);

            switch (uriMatcher.match(uri)) {
                case uriCode:
                    qb.setProjectionMap(values);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            if (sortOrder == null || sortOrder == "") {
                sortOrder = name;
            }
            Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
    }

    @Override
    public String getType(Uri uri) {
        Log.d("SsidProvider", "Manoj : String getType");

        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/cte";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (db == null) {
            createDb();
        }
        Log.d("SsidProvider", "Manoj : Values saved in DB");
        long rowID = db.insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.d("SsidProvider", "Manoj : Values saved in DB completed");

//            Set<Map.Entry<String, Object>> s=values.valueSet();
//            Iterator itr = s.iterator();
//
//            Log.d("DatabaseSync", "ContentValue Length :: " +values.size());
//
//            while(itr.hasNext())
//            {
//                Map.Entry me = (Map.Entry)itr.next();
//                String key = me.getKey().toString();
//                Object value =  me.getValue();
//
//                Log.d("DatabaseSync", "Key:"+key+", values:"+(String)(value == null?null:value.toString()));
//            }

            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    private SQLiteDatabase db;
    static final String DATABASE_NAME = "ssidDb";
    static final String TABLE_NAME = "ssidAndPassword";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + " ssidName TEXT NOT NULL, "
            + " ssidPassword TEXT NOT NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
