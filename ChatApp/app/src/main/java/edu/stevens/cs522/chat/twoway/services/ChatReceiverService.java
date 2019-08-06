package edu.stevens.cs522.chat.twoway.services;

import android.app.Service;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import edu.stevens.cs522.chat.twoway.activities.constants;
import edu.stevens.cs522.chat.twoway.contracts.PeerContract;
import edu.stevens.cs522.chat.twoway.entities.Messages;
import edu.stevens.cs522.chat.twoway.entities.Peer;

public class ChatReceiverService extends Service {

    private DatagramSocket datagramSocket;
    private static final String MESSAGE_SEPARATOR = ",";
    final static public String TAG = ChatReceiverService.class.getCanonicalName();
    public static final int MSG_SEND = 1;

    private HandlerThread messengerThread = null;
    Messenger messenger = null;
    private MessageHandler handler = null;

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            Log.i(TAG, "ChatReceiverService : oncreate");
            messengerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            messengerThread.start();
            Looper messengerLooper = messengerThread.getLooper();
            handler = new MessageHandler(messengerLooper);
            messenger = new Messenger(handler);
            datagramSocket = new DatagramSocket(Integer.parseInt(constants.PORT_VALUE));

        } catch (IOException e) {
            Log.e( TAG, "Cannot create socket." + e);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"ChatReceiverService : onDestroy");
        datagramSocket.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i( TAG, "Started Chat service, running task for receiving messages.");
        ReceiveMessageTask recvTask = new ReceiveMessageTask();
        recvTask.execute((Void[]) null);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "ChatRecevierService : onBind");
        return messenger.getBinder();
    }

    private class ReceiveMessageTask extends AsyncTask<Void, Messages, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i(TAG, "Chat Receiver Service = doInBackgroung");
                while (true) {
                    messageHandler();
                    publishProgress();
                }
            } catch (IOException e) {
                Log.e( TAG, "Problem receiving a message: " + e);
            }
            return ((Void) null);
        }

        @Override
        protected void onProgressUpdate(Messages... values) {
            Log.i(TAG, "onProgressUpdate");
        }

        @Override
        protected void onPostExecute(Void result) {
            stopSelf();
        }
    }

    private void messageHandler() throws IOException {
        byte[] receiveData = new byte[1024];

        Log.i(TAG, "Waiting for a message.");

        DatagramPacket receivePacket = new DatagramPacket(receiveData,
                receiveData.length);

        datagramSocket.receive(receivePacket);
        Log.i(TAG, "Received a packet.");

        InetAddress sourceIPAddress = receivePacket.getAddress();
        Log.d(TAG, "Source IP Address: " + sourceIPAddress);

        String[] res;

        receiveData = receivePacket.getData();
        res = (new String(receiveData, 0, receivePacket.getLength())).split(",");
        Messages m = new Messages(1, res[1], res[0]);
        Peer p = new Peer(res[0], sourceIPAddress, receivePacket.getPort());

        //update messages content edu.stevens.cs522.chat.twoway.providers
        if (!addMessage(m)) {
            Log.e("addMessge", "fail");
        }
        ;
        if (!addPeer(p)) {
            Log.e("addPeer", "fail");
        }

        sendBroadcast(msgUpdateBroadcast);
    }

    public static final String MESSAGE_BROADCAST = "cs522.stevens.edu.chat.MessageBroadcast";
    private Intent msgUpdateBroadcast = new Intent(MESSAGE_BROADCAST);

    public boolean addMessage(Messages b) {
        Log.i(TAG, "addMessage");
        ContentValues contentValues = new ContentValues();
        b.writeToProvider(contentValues);
        getContentResolver().insert(PeerContract.CONTENT_URI, contentValues);
        return true;
    }

    public boolean addPeer(Peer b) {
        Log.i(TAG, "addPeer");
        ContentValues contentValues = new ContentValues();
        b.writeToProvider(contentValues);
        getContentResolver().delete(
                PeerContract.CONTENT_URI_PEER,
                "name ='" + b.name + "' and " + "address ='"
                        + b.address.getHostAddress() + "'", null);
        getContentResolver().insert(PeerContract.CONTENT_URI_PEER,
                contentValues);
        return true;
    }

    class MessageHandler extends Handler {

        public MessageHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG,"ChatREceiverService : handleMessage");
            try {
                switch (msg.what) {
                    case MSG_SEND:
                        Log.i(TAG, "Recevier server : handleMessgae Method");
                        messageHandler();

                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "Can't send message!" + e);
            }
        }
    }
}
