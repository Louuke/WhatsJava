package icu.jnet.whatsjava;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.web.WebChat;
import icu.jnet.whatsjava.web.WebContact;
import icu.jnet.whatsjava.web.WebConversationMessage;
import icu.jnet.whatsjava.web.WebEmoji;
import icu.jnet.whatsjava.web.WebImageMessage;
import icu.jnet.whatsjava.web.WebStatus;
import icu.jnet.whatsjava.web.WebVideoMessage;

public interface ClientActionInterface {

	
	void onReceiveLoginResponse(int httpCode);
	
	void onQRCodeScanRequired(BufferedImage img);
	
	void onWebConversationMessage(WebConversationMessage conversationMessage);
	
	void onWebImageMessage(WebImageMessage imageMessage);
	
	void onWebVideoMessage(WebVideoMessage videoMessage); 
	
	void onWebChat(WebChat[] chats);
	
	void onWebContact(WebContact[] contacts);
	
	void onWebStatus(WebStatus[] status);
	
	void onWebEmoji(WebEmoji[] emojis);
}
