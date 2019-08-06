package edu.stevens.cs522.chat.twoway.providers;

import java.util.Arrays;
import java.util.HashSet;

import edu.stevens.cs522.chat.twoway.contracts.PeerContract;
import edu.stevens.cs522.chat.twoway.adapters.DbAdapter;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ChatProvider extends ContentProvider {

    private DatabaseHelper database;
    private Context context;
    private SQLiteDatabase db;

    public ChatProvider(){}
    public static String[] projection = new String[] {
            PeerContract.ID, PeerContract.TEXT, PeerContract.SENDER
    };
    public static String[] projection2 = new String[] {
            PeerContract.ID,"name","address","port"
    };
    public static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
                              int _newVersion) {
            // Log the version upgrade.
            Log.w("TaskDBAdapter",
                    "Upgrading from version " +  _oldVersion
                            + " to " +  _newVersion);
            // Upgrade: drop the old table and create a new one.

            _db.execSQL("DROP TABLE IF EXISTS " + PeerContract.DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS "+ PeerContract.DATABASE_TABLE_PEER);
            onCreate(_db);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DbAdapter.DATABASE_CREATE);
            db.execSQL(DbAdapter.DATABASE_CREATE_PEER);
            db.execSQL("PRAGMA foreign_keys=ON;");

        }
    }

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.DATABASE_TABLE, PeerContract.ALL_ROWS);
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.DATABASE_TABLE + "/#", PeerContract.SINGLE_ROW);
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.DATABASE_TABLE_PEER, PeerContract.ALL_ROWS_PEER);
        uriMatcher.addURI(PeerContract.AUTHORITY, PeerContract.DATABASE_TABLE_PEER + "/#", PeerContract.SINGLE_ROW_PEER);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Null");
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        database = new DatabaseHelper(context, PeerContract.DATABASE_NAME, null, PeerContract.DATABASE_VERSION);
        db = database.getWritableDatabase();
        return false;
    }

    public ChatProvider(Context c) {
        context=c;
        database=new DatabaseHelper(c, PeerContract.DATABASE_NAME, null, PeerContract.DATABASE_VERSION);
        db=database.getWritableDatabase();
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor=null;
        switch (uriMatcher.match(uri)) {
            case PeerContract.ALL_ROWS :
                cursor = db.query(PeerContract.DATABASE_TABLE,
                        new String[] {PeerContract.ID, PeerContract.TEXT, PeerContract.SENDER},
                        selection, selectionArgs, null, null, null);
                break;
            // query the database
            case PeerContract.SINGLE_ROW :
                selection = PeerContract.ID + "=?";
                selectionArgs[0] = uri.getLastPathSegment();
                break;
            case PeerContract.ALL_ROWS_PEER:
                cursor = db.query(PeerContract.DATABASE_TABLE_PEER,
                        new String[] {PeerContract.ID, "name","address","port"},
                        selection, selectionArgs, null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        long id = 0;

        try{
            switch (uriType) {
                case PeerContract.ALL_ROWS:
                    id = db.insert(PeerContract.DATABASE_TABLE, null, values);
                    return Uri.parse("content://" + PeerContract.AUTHORITY
                            + "/" + PeerContract.DATABASE_TABLE + "/" + id);
                case PeerContract.ALL_ROWS_PEER:
                    id = db.insert(PeerContract.DATABASE_TABLE_PEER, null, values);
                    return Uri.parse("content://" + PeerContract.AUTHORITY
                            + "/" + PeerContract.DATABASE_TABLE_PEER + "/" + id);
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }catch(Exception e){
            Log.e("insert fail", "fail to insert");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (uriType) {
            case PeerContract.ALL_ROWS:
                rowsDeleted = db.delete(PeerContract.DATABASE_TABLE, selection,
                        selectionArgs);
                break;
            case PeerContract.ALL_ROWS_PEER:
                rowsDeleted = db.delete(PeerContract.DATABASE_TABLE_PEER, selection,
                        selectionArgs);
                break;
            case PeerContract.SINGLE_ROW:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(PeerContract.DATABASE_TABLE, PeerContract.ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(PeerContract.DATABASE_TABLE, PeerContract.ID + "=" + id  + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = uriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (uriType) {
            case PeerContract.ALL_ROWS:
                rowsUpdated = db.update(PeerContract.DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PeerContract.SINGLE_ROW:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(PeerContract.DATABASE_TABLE,
                            values,
                            PeerContract.ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = db.update(PeerContract.DATABASE_TABLE,
                            values,
                            PeerContract.ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return rowsUpdated;
    }

    @SuppressWarnings("unused")
    private void checkColumns(String[] projection) {
        String[] available = { PeerContract.ID, PeerContract.TEXT, PeerContract.SENDER };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }





}
