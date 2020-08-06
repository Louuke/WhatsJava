package icu.jnet.whatsjava;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.web.WebChat;
import icu.jnet.whatsjava.web.WebContact;
import icu.jnet.whatsjava.web.WebConversationMessage;
import icu.jnet.whatsjava.web.WebEmoji;
import icu.jnet.whatsjava.web.WebImageMessage;
import icu.jnet.whatsjava.web.WebStatus;
import icu.jnet.whatsjava.web.WebVideoMessage;

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
