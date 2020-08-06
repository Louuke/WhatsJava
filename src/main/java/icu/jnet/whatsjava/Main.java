package icu.jnet.whatsjava;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import icu.jnet.whatsjava.helper.Utils;
import icu.jnet.whatsjava.web.WebChat;
import icu.jnet.whatsjava.web.WebConversationMessage;
import icu.jnet.whatsjava.web.WebImageMessage;
import icu.jnet.whatsjava.web.WebVideoMessage;

public class Main {
	
	public static void main(String[] args) {
		WAClient client = new WAClient("credentials.json");
		client.openConnection();
		client.addClientActionListener(new ClientActionListener() {

			@Override
			public void onReceiveLoginResponse(int httpCode) {
				if(httpCode == 200) {
					System.out.println("Logged in successfully! Code: " + httpCode);
				} else {
					System.out.println("Restore of previous session failed! Code: " + httpCode);
				}
			}
			
			@Override
			public void onQRCodeScanRequired(BufferedImage img) {
				System.out.println("Authentication required! Please scan the QR code!");
				saveQRCode(img);
			}

			@Override
			public void onWebChat(WebChat[] chats) {
				System.out.println("You have " + chats.length + " chats");
			}
		});
	}
	
	public static void saveQRCode(BufferedImage img) {
		try {
			ImageIO.write(img, "jpg", new File("qr.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
