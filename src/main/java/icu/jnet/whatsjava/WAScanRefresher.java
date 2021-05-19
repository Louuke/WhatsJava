package icu.jnet.whatsjava;

import java.awt.image.BufferedImage;

import icu.jnet.whatsjava.helper.QRGen;
import icu.jnet.whatsjava.helper.Utils;

class WAScanRefresher{
	
	private final WAClient client;
	private final String clientId;
	private final byte[] publicKey;
	
	private boolean scanned = false;
	
	public WAScanRefresher(WAClient client, String clientId, byte[] publicKey) {
		this.clientId = clientId;
		this.publicKey = publicKey;
		this.client = client;
	}
	
	public void start() {
		new Thread(() -> {
			int run = 0;

			while(!scanned && run != 5) {
				Utils.waitMillis(20000);

				// Request a new qr code, if the previous one was not scanned
				// during the last 20 seconds. Max 5 times
				if(!scanned) {
					client.requestNewServerId();
				}

				run++;
			}
		}).start();
	}
	
	public void newQRCode(String message) {
		String serverId = Utils.encodeValidJson(message).get("ref").getAsString();
		BufferedImage img = QRGen.generateQRcode(clientId, serverId, publicKey);
		
		// QR code scan required
		client.listener.onQRCodeScanRequired(img);
	}
	
	public void setQRCodeScanned(boolean scanned) {
		this.scanned = scanned;
	}
}

