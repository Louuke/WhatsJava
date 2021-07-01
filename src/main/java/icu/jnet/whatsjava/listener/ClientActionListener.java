package icu.jnet.whatsjava.listener;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.messages.web.WebChat;
import icu.jnet.whatsjava.messages.web.WebContact;
import icu.jnet.whatsjava.messages.web.WebConversationMessage;
import icu.jnet.whatsjava.messages.web.WebEmoji;
import icu.jnet.whatsjava.messages.web.WebImageMessage;
import icu.jnet.whatsjava.messages.web.WebStatus;
import icu.jnet.whatsjava.messages.web.WebVideoMessage;

public class ClientActionListener implements ClientActionInterface {

	public ClientActionListener() {
		super();
	}
	
	@Override
	public void onReceiveLoginResponse(int httpCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQRCodeScanRequired(BufferedImage img) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebChat(WebChat[] chats) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebContact(WebContact[] contacts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebStatus(WebStatus[] status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebEmoji(WebEmoji[] emojis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebImageMessage(WebImageMessage imageMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebVideoMessage(WebVideoMessage videoMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebConversationMessage(WebConversationMessage conversationMessage) {
		// TODO Auto-generated method stub
		
	}

}
