package icu.jnet.whatsjava.listener;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.messages.generic.*;

public interface ClientActionInterface {

	
	void onReceiveLoginResponse(int httpCode);
	
	void onQRCodeScanRequired(BufferedImage img);
	
	void onWAMessage(WAMessage[] waMessage);
	
	void onWAChat(WAChat[] chats);
	
	void onWAContact(WAContact[] contacts);
	
	void onWAStatus(WAStatus[] status);
	
	void onWAEmoji(WAEmoji[] emojis);
}
