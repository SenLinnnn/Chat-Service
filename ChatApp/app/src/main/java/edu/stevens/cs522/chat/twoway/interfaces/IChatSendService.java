package edu.stevens.cs522.chat.twoway.interfaces;

import android.content.Intent;
import android.os.Message;

import java.net.DatagramPacket;

public interface IChatSendService {
	void send (DatagramPacket packet);
}
