package icu.jnet.whatsjava;

import java.time.Instant;
import java.util.Random;

import com.neovisionaries.ws.client.WebSocket;

import icu.jnet.whatsjava.helper.Utils;

class WAKeepAlive {

	/* Keeps the Websocket connection alive by sending a ping every 20-38 seconds */
	
	// Timestamp of server answer
	private long lastPong;
	
	private boolean running = false;
	private WebSocket ws;
	
	public WAKeepAlive(WebSocket ws) {
		this.ws = ws;
	}
	
	public void start() {
		running = true;
		
		Random rand = new Random();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(running) {
					long millis = (rand.nextInt(21) + 15) * 1000;
					Utils.waitMillis(millis);
					
					ws.sendText("?,,");
					
					Utils.waitMillis(3000);
					
					// Server timeout detection
					if(Instant.now().getEpochSecond() - lastPong >= 5) {
						stop();
					}
				}
			}
		}).start();
	}
	
	public void updatePong() {
		lastPong = Instant.now().getEpochSecond();
	}
	
	private void stop() {
		running = false;
	}
}
