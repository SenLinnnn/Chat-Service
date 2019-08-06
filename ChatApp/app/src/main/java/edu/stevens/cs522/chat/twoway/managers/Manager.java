package edu.stevens.cs522.chat.twoway.managers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.twoway.interfaces.IContinue;
import edu.stevens.cs522.chat.twoway.interfaces.IEntityCreator;
import edu.stevens.cs522.chat.twoway.interfaces.IQueryListener;
import edu.stevens.cs522.chat.twoway.interfaces.ISimpleQueryListener;

public abstract class Manager<T> {
	
	protected final Context context;
	private final IEntityCreator<T> creator;
	private final int loaderID;
	private final String tag;
	
	private ContentResolver syncResolver;
    protected AsyncContentResolver asyncResolver;

	protected Manager(Context context,
                     IEntityCreator<T> creator,
					  int loaderID) {
        this.context = context;
        this.creator = creator;
        this.loaderID = loaderID;
        this.tag = this.getClass().getCanonicalName();
    }

	protected ContentResolver getSyncResolver() {
		if (syncResolver == null) 
			syncResolver = context.getContentResolver();
		return syncResolver;
	}
	
	protected AsyncContentResolver getAsyncResolver() {
		if (asyncResolver == null)
			asyncResolver = new AsyncContentResolver(context.getContentResolver());
		return asyncResolver;
	}
	
	protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<T> listener) {
		SimpleQueryBuilder.executeQuery((Activity) context, uri, creator, listener);
	}
	
	protected void executeSimpleQuery(Uri uri,
                                      String[] projection,
                                      String selection,
                                      String[] selectionArgs,
                                      ISimpleQueryListener<T> listener) {
	    SimpleQueryBuilder.executeQuery((Activity) context, uri, projection, selection, selectionArgs, creator, listener);
	}

    protected void executeSimpleInsert(Uri uri,
                                       int token,
                                       ContentValues values,
                                       ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity) context, uri, null, null, null, creator, listener);
    }

	
	protected void executeQuery(Uri uri, IQueryListener<T> listener) {
		QueryBuilder.executeQuery(tag, (Activity) context, uri, loaderID, creator, listener);
	}
	
	protected void executeQuery(Uri uri, 
								String[] projection, 
								String selection, 
								String[] selectionArgs, 
								IQueryListener<T> listener) {
		QueryBuilder.executeQuery(tag, (Activity) context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
	}
	
	protected void reexecuteQuery(Uri uri, 
								String[] projection, 
								String selection, 
								String[] selectionArgs, 
								IQueryListener<T> listener) {
		QueryBuilder.reexecuteQuery(tag, (Activity) context, uri, loaderID, projection, selection, selectionArgs, creator, listener);
	}
	
	protected void getAllAsync(Uri uri, 
								String[] projection, 
								String selection, 
								String[] selectionArgs, 
								String sortOrder, 
								IContinue<Cursor> callback) {
		 asyncResolver.queryAsync(uri, projection, selection, selectionArgs, sortOrder, callback);
		
	}
	
	protected void deleteAsync (Uri uri, String selection, String[] selectionArgs) {
		asyncResolver.deleteAsync(uri, selection, selectionArgs);
	}
}
