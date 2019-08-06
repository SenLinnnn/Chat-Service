package edu.stevens.cs522.chat.twoway.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;

import edu.stevens.cs522.chat.twoway.contracts.PeerContract;
import edu.stevens.cs522.chat.twoway.entities.Peer;
import edu.stevens.cs522.chat.twoway.interfaces.IContinue;
import edu.stevens.cs522.chat.twoway.interfaces.IEntityCreator;

public class PeerManager extends Manager<Peer> {

    public PeerManager(Context context, IEntityCreator<Peer> creator,
                       int loaderID) {
        super(context, creator, loaderID);
    }

    AsyncContentResolver asyncContentResolver = new AsyncContentResolver(context.getContentResolver());

    public void persistAsync(Peer peer, IContinue<Uri> callback) {
        ContentValues peerValues = new ContentValues();
        peer.writeToProvider(peerValues);
        asyncContentResolver.insertAsync(PeerContract.CONTENT_URI, peerValues, callback);
    }

    public void getAllAsync(Uri uri,
                            String[] projection,
                            String selection,
                            String[] selectionArgs,
                            String sortOrder,
                            IContinue<Cursor> callback) {
        super.getAllAsync(uri, projection, selection, selectionArgs, sortOrder, callback);
    }

    public void deleteAsync (Uri uri, String selection, String[] selectionArgs) {
        super.deleteAsync(uri, selection, selectionArgs);
    }
}
