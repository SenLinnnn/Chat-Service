package edu.stevens.cs522.chat.twoway.activities;

import edu.stevens.cs522.chat.twoway.R;
import edu.stevens.cs522.chat.twoway.adapters.DbAdapter;
import edu.stevens.cs522.chat.twoway.contracts.PeerContract;
import edu.stevens.cs522.chat.twoway.entities.Peer;
import edu.stevens.cs522.chat.twoway.interfaces.IEntityCreator;
import edu.stevens.cs522.chat.twoway.interfaces.IQueryListener;
import edu.stevens.cs522.chat.twoway.managers.QueryBuilder;
import edu.stevens.cs522.chat.twoway.managers.TypedCursor;
import edu.stevens.cs522.chat.twoway.providers.*;
import edu.stevens.cs522.chat.twoway.services.*;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatApp extends ListActivity {
    final static public String TAG = ChatApp.class.getCanonicalName();
    final static private int PREFERENCES = 1;

    private static final int LOADER_ID1 = 0;
    private static final int LOADER_ID2 = 1;
    static final private int LIST_PEERS = 1;

    private String clientName = DEFAULT_CLIENT_NAME;
    private int clientPort = DEFAULT_CLIENT_PORT;

    public String CLIENT_NAME_KEY = "name_key";
    public static final String CLIENT_PORT_KEY = "client_port";

    public static final String DEFAULT_CLIENT_NAME = "client";
    public static final int DEFAULT_CLIENT_PORT = Integer.parseInt(constants.PORT_VALUE);

    private ServiceConnection connection;
    public SimpleCursorAdapter sca;
    private EditText destinationHost, destinationPort, messageText;
    public Cursor cursor;

    Messenger mService = null;
    boolean mBound;

    ChatConnection chatConnection = new ChatConnection();

    private class ChatConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            Log.i(TAG, "Service connected to: " + name);
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Log.i(TAG, "Service disconnected from: " + name);
        }
    }

    Intent chatReceiverIntent,bindIntent;
    Receiver updater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        destinationHost = (EditText)findViewById(R.id.destination_host);
        destinationPort = (EditText)findViewById(R.id.destination_port);
        messageText = (EditText)findViewById(R.id.message_text);

        QueryBuilder.executeQuery(TAG, this, PeerContract.CONTENT_URI, LOADER_ID1,
                new IEntityCreator<Peer>(){
                    public  Peer create(Cursor cursor){
                        return new Peer(cursor);
                    }
                },
                new IQueryListener<Peer>(){
                    public void handleResults(TypedCursor<Peer> results) {
                        sca.swapCursor(results.getCursor());
                    }
                    public void closeResults() {
                        sca.swapCursor(null);
                    }
                });

        //Display Messages - List view
        String[] from = new String[] { DbAdapter.SENDER, DbAdapter.TEXT };
        int[] to = new int[] { R.id.cart_row_sender, R.id.cart_row_message };
        try
        {
            //cursor = getContentResolver().query(PeerContract.CONTENT_URI, ChatProvider.projection, null,null, null);
            QueryBuilder.executeQuery(TAG, this, PeerContract.CONTENT_URI, LOADER_ID1, ChatProvider.projection, null, null,
                    new IEntityCreator<Peer>(){
                        public  Peer create(Cursor cursor){
                            return new Peer(cursor);
                        }
                    },
                    new IQueryListener<Peer>(){
                        public void handleResults(TypedCursor<Peer> results) {
                            sca.swapCursor(results.getCursor());
                        }
                        public void closeResults() {
                            sca.swapCursor(null);
                        }
                    });
            sca= new SimpleCursorAdapter(this, R.layout.message, cursor, from, to);
            getListView().setAdapter(sca);
        }
        catch (Exception e)
        {
            Log.e(e.getClass().getName(), e.getMessage());
        }


        //Binding ChatSendService
        bindIntent = new Intent(this, ChatSendService.class);
        //bindService(bindIntent, chatConnection, Context.BIND_AUTO_CREATE);
        bindService(bindIntent, chatConnection, Context.BIND_AUTO_CREATE);

        //Explicitly Starting receiver service
        chatReceiverIntent = new Intent(getApplicationContext(),ChatReceiverService.class);
        startService(chatReceiverIntent);

        //Register the broadcast receiver
        updater = new Receiver();
        registerReceiver(updater, new IntentFilter(constants.MESSAGE_BROADCAST));

        Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Receiver Service Started");


        //onClick for SEND button
        Button sendButton = (Button) findViewById(R.id.button1);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    Log.i(TAG, "onClickListener sendButton");
                    InetAddress targetAddr = InetAddress.getByName(destinationHost.getText().toString());
                    int targetPort = Integer.parseInt(destinationPort.getText().toString());
                    String newMessage = messageText.getText().toString();
                    String messageString = clientName + constants.MESSAGE_SEPERATOR + newMessage;

                    Bundle b = new Bundle();
                    b.putString(constants.ADDRESS, targetAddr.getHostAddress());
                    b.putInt(constants.PORT, targetPort);
                    b.putString(constants.MESSAGE, messageString);

                    if (!mBound){
                        Log.i(TAG, "onClickListener mBound = false");
                        return;
                    }
                    // Create and send a message to the service, using a supporte 'what' value
                    Message msg = Message.obtain(null, constants.MSG_SEND, 0, 0);
                    msg.setData(b);
                    try {
                        //Using Messenger Obj send Messages obj
                        mService.send(msg);
                        Log.i(TAG,"onClickListener Messenger Send");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, "Packet sent: " + messageString);

                } catch (UnknownHostException e) {
                    Log.e(TAG, "Unknown host exception: " + e.getMessage());
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.show_peers:
                Intent addIntent = new Intent(this, PeerList.class);
                startActivityForResult(addIntent, LIST_PEERS);
                this.onPause();
                return true;
            case R.id.client_name_menu:  // Define Client Name as preference or shared preference
                Intent prefIntent = new Intent(getApplicationContext(), Settings.class);
                startActivityForResult(prefIntent, PREFERENCES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == PREFERENCES){
            if(resultCode == RESULT_OK){
                clientName = (String)intent.getExtras().get(constants.saveClientNamePreferences);
                Log.i(TAG, "client name = "+clientName);
            }else if(resultCode == RESULT_CANCELED){
                clientName = DEFAULT_CLIENT_NAME;
                Toast.makeText(this,"Client Name Set to Default",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Explicitly stopping Recevier Service");
        try {
            unbindService(chatConnection);
            unregisterReceiver(updater);
            stopService(new Intent(this, ChatReceiverService.class));

        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Error while stopping service", Toast.LENGTH_SHORT).show();
        }

    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast receiver");
            sca.getCursor().requery();
            sca.changeCursor(sca.getCursor());
            Log.i(TAG, "BroadCast Receiver : Messages Received");
            Toast.makeText(getApplicationContext(), "Messages Received", Toast.LENGTH_SHORT).show();
        }

    }

}
