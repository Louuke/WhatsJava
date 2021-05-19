package icu.jnet.whatsjava;

import java.time.Instant;
import java.util.Random;

import com.neovisionaries.ws.client.WebSocket;

import icu.jnet.whatsjava.helper.Utils;

class WAKeepAlive {

	/* Keeps the WebSocket connection alive by sending a ping every 20-30 seconds */
	
	// Timestamp of last server answer
	private long lastPong;
	// Timestamp of last client ping
	private long lastPing;
	
	private boolean running = true;
	private final WebSocket ws;
	
	public WAKeepAlive(WebSocket ws) {
		this.ws = ws;
	}
	
	public void start() {
		new Thread(() -> {
			while(running) {
				Utils.waitMillis(1000);

				long now = Instant.now().getEpochSecond();
				if(now - lastPing > 25) {
					lastPing = now;

					ws.sendText("?,,");
				} else if(now - lastPong > 30) { // Detect server timeout
					stop();
				}
			}
		}).start();
	}

	protected void updatePong() {
		lastPong = Instant.now().getEpochSecond();
	}
	
	protected void stop() {
		running = false;
	}
}
