package edu.stevens.cs522.chat.twoway.managers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import edu.stevens.cs522.chat.twoway.contracts.PeerContract;
import edu.stevens.cs522.chat.twoway.interfaces.IEntityCreator;
import edu.stevens.cs522.chat.twoway.interfaces.IQueryListener;
import edu.stevens.cs522.chat.twoway.providers.ChatProvider;

public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private final Context context;
	private final IEntityCreator<T> creator;
	private final int loaderID;
	private final String tag;
	private Uri uri;
	private IQueryListener<T> listener;

	private QueryBuilder(String tag, 
						Context context, 
						Uri uri, 
						int loaderID, 
						IEntityCreator<T> creator, 
						IQueryListener<T> listener) {
		// TODO Auto-generated method stub
		this.tag = tag;
		this.context = context;
		this.uri = uri;
		this.loaderID = loaderID;
		this.creator = creator;
		this.listener = listener;
	}
	
	public static <T> void executeQuery(String tag, 
										Activity context, 
										Uri uri, 
										int loaderID, 
										IEntityCreator<T> creator, 
										IQueryListener<T> listener) {
		QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
		LoaderManager lm = context.getLoaderManager();
		lm.initLoader(loaderID, null, qb);
	}

    private static final int LOADER_ID1 = 0;
    private static final int LOADER_ID2 = 1;
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		/*if (id == loaderID) {
			//return new CursorLoader(context, uri, projection, select, selectArgs, null);
		}*/
        switch (loaderID) {
            case LOADER_ID1:
                return new CursorLoader(context, PeerContract.CONTENT_URI,
                        ChatProvider.projection, null, null, null);
            case LOADER_ID2:
                return new CursorLoader(context,
                        PeerContract.CONTENT_URI_PEER, ChatProvider.projection2,
                        null, null, null);
            default:
                return null;
        }
	}

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // TODO Auto-generated method stub
        if (loader.getId() == loaderID) {
            listener.handleResults(new TypedCursor<T>(cursor, creator));
        } else {
            throw new IllegalStateException ("Unexpected loader callback");
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO Auto-generated method stub
        if (loader.getId() == loaderID) {
            listener.closeResults();
        } else {
            throw new IllegalStateException ("Unexpected loader callback");
        }
    }

	public static <T> void executeQuery(String tag, 
			Activity context, 
			Uri uri, 
			int loaderID, 
			String[] projection, 
			String selection, 
			String[] selectionArgs, 
			IEntityCreator<T> creator, 
			IQueryListener<T> listener) {
		QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
		LoaderManager lm = context.getLoaderManager();
		lm.initLoader(loaderID, null, qb);
	}
	
	public static <T> void reexecuteQuery(String tag, 
			Activity context, 
			Uri uri, 
			int loaderID, 
			IEntityCreator<T> creator, 
			IQueryListener<T> listener) {
		// TODO Auto-generated method stub
	}
	
	public static <T> void reexecuteQuery(String tag, 
			Activity context, 
			Uri uri, 
			int loaderID, 
			String[] projection, 
			String selection, 
			String[] selectionArgs, 
			IEntityCreator<T> creator, 
			IQueryListener<T> listener) {
		// TODO Auto-generated method stub
	}
}
