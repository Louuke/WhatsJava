package icu.jnet.whatsjava.listener;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.messages.web.WebChat;
import icu.jnet.whatsjava.messages.web.WebContact;
import icu.jnet.whatsjava.messages.web.WebConversationMessage;
import icu.jnet.whatsjava.messages.web.WebEmoji;
import icu.jnet.whatsjava.messages.web.WebImageMessage;
import icu.jnet.whatsjava.messages.web.WebStatus;
import icu.jnet.whatsjava.messages.web.WebVideoMessage;

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
