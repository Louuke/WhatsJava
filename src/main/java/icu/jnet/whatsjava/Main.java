package icu.jnet.whatsjava;

import icu.jnet.whatsjava.listener.ClientActionListener;
import icu.jnet.whatsjava.whatsapp.WAClient;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {
	
	public static void main(String[] args) {
		WAClient client = new WAClient();
		client.setPrintQRCode(true);
		client.addClientActionListener(new ClientActionListener() {
			@Override
			public void onQRCodeScanRequired(BufferedImage img) {
				System.out.println("Authentication required! Please scan the QR code!");
				saveQRCode(img, "qr.jpg");
			}
		});
		int httpCode = client.openConnection();
		if(httpCode == 200) {
			System.out.println("Logged in successfully!");
			System.out.println("You have " + client.loadChats().length + " chats");
		} else {
			System.out.println("Login failed! Code: " + httpCode);
		}
	}
	
	public static void saveQRCode(BufferedImage img, String name) {
		try {
			ImageIO.write(img, "jpg", new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
