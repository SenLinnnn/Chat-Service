package edu.stevens.cs522.chat.twoway.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import edu.stevens.cs522.chat.twoway.activities.ChatApp;
import edu.stevens.cs522.chat.twoway.activities.constants;
import edu.stevens.cs522.chat.twoway.interfaces.IChatSendService;

public class ChatSendService extends Service implements IChatSendService{

    private DatagramSocket datagramSocket;
    final static public String TAG = ChatSendService.class.getCanonicalName();
    public static final int MSG_SEND = 1;

    HandlerThread messengerThread;
    MessageHandler handler;
    Messenger messenger;
    Looper messengerLooper;
    InetAddress targetAddr;

    @Override
    public void onCreate() {
        super.onCreate();
        messengerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        messengerThread.start();
        messengerLooper = messengerThread.getLooper();
        handler = new MessageHandler(this, messengerLooper);
        messenger = new Messenger(handler);


    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onBind CHatSendService");

        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        // datagramSocket.close();
    }

    class MessageHandler extends Handler{

        public MessageHandler(Context context, Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msgObj) {
            try
            {
                switch (msgObj.what) {
                    case MSG_SEND:
                        Log.i(TAG, "HandleMessage .what case");
                        Bundle data = msgObj.getData();
                        String message = data.getString(constants.MESSAGE);
                        Log.i(TAG,"bundle message = "+message);
                        String address = data.getString(constants.ADDRESS);
                        int port = data.getInt(constants.PORT);
                        targetAddr = InetAddress.getByName(address);
                        byte[] messageByte = message.getBytes();

                        Log.i(TAG, "ChatSenderService: HandleMessage: Received Messages = "+ messageByte.toString());
                        DatagramPacket sendPacket = new DatagramPacket(messageByte,messageByte.length, targetAddr, port);
                        send(sendPacket);
                        Log.i(TAG, "ChatSenderService: HandleMessage: Datagram Packet send = " + messageByte.toString());

                        sendBroadcast(new Intent(constants.NEW_MESSAGE_BROADCAST));
                        Toast.makeText(getApplicationContext(), "Messages Sent: "+message, Toast.LENGTH_SHORT).show();

                        msgObj.recycle();
                        break;
                    default:
                        super.handleMessage(msgObj);
                }
            }
            catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            }
        }
    }

    public void send(final DatagramPacket p) {
        try{
            datagramSocket = new DatagramSocket(null);
            Log.i(TAG,"datagram socket created");
            datagramSocket.setReuseAddress(true);
            Log.i(TAG, "datagram socket: setReusedAddress");
            datagramSocket.setBroadcast(true);
            Log.i(TAG, "datagram socket: setBroadcast");
            datagramSocket.bind(new InetSocketAddress(Integer.parseInt(constants.PORT_VALUE)));
            Log.i(TAG, "datagram socket:binding to the port");
        } catch (IOException e) {
            Log.e(ChatApp.TAG, "Can't send message!"+e);
        }


        new Thread(new Runnable() {
            public void run() {
                try{
                    Log.i(ChatApp.TAG, "Sending a message..");
                    datagramSocket.send(p);

                    Log.i(ChatApp.TAG, "Datagram message sent");
                } catch (IOException e) {
                    Log.e(ChatApp.TAG, "Can't send message!"+e);
                }
            }
        }).start();
    }
}