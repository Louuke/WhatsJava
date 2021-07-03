package icu.jnet.whatsjava.listener;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.messages.generic.*;

public class ClientActionListener implements ClientActionInterface {

	@Override
	public void onReceiveLoginResponse(int httpCode) {

	}

	@Override
	public void onQRCodeScanRequired(BufferedImage img) {

	}

	@Override
	public void onWAMessage(WAMessage[] waMessage) {

	}

	@Override
	public void onWAChat(WAChat[] chats) {

	}

	@Override
	public void onWAContact(WAContact[] contacts) {

	}

	@Override
	public void onWAStatus(WAStatusMessage[] status) {

	}

	@Override
	public void onWAEmoji(WAEmoji[] emojis) {

	}
}
