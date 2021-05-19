package icu.jnet.whatsjava;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import icu.jnet.whatsjava.constants.ExpectedResponse;
import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.constants.WAFlag;
import icu.jnet.whatsjava.constants.WAMetric;
import icu.jnet.whatsjava.encryption.BinaryDecoder;
import icu.jnet.whatsjava.encryption.BinaryEncryption;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;
import icu.jnet.whatsjava.helper.AuthCredentials;
import icu.jnet.whatsjava.helper.AuthCredentialsHelper;
import icu.jnet.whatsjava.helper.Utils;
import icu.jnet.whatsjava.web.WebChat;
import icu.jnet.whatsjava.web.WebContact;
import icu.jnet.whatsjava.web.WebConversationMessage;
import icu.jnet.whatsjava.web.WebEmoji;
import icu.jnet.whatsjava.web.WebImageMessage;
import icu.jnet.whatsjava.web.WebStatus;
import icu.jnet.whatsjava.web.WebVideoMessage;

public class WAClient extends WebSocketAdapter {
	
	// WhatsApp WebSocket server
	private final String WHATSAPP_SERVER = "wss://web.whatsapp.com/ws";
		
	// WhatsApp rejects requests with different header
	private final String HEADER_ORIGIN = "https://web.whatsapp.com";
	private final String HEADER_USER_AGENT = "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0";
	
	// Spawns a thread to keep the connection alive
	private WAKeepAlive keepAlive;
	// Stores ids, encryption keys...
	protected AuthCredentials credentials;
	// Path to the file where the credentials get stored
	private final String authCredentialsPath;
	// Generates a new qr code, if the last one expired
	private WAScanRefresher refresher;
	// Expected type of the next message the backend sends next
	private byte expectedResponse;
	private boolean loggedIn = false;

	protected WebSocket ws;
	protected ClientActionInterface listener;
	
	public WAClient(String authCredentialsPath) {
		this.authCredentialsPath = authCredentialsPath;
		this.credentials = AuthCredentialsHelper.loadAuthCredentials(authCredentialsPath);
	}
	
	
	// Open WebSocket connection with WhatsApp server
	public WAClient openConnection() {
		try {
			disconnect();
			
			WebSocketFactory factory = new WebSocketFactory();
			ws = factory.createSocket(WHATSAPP_SERVER);
			ws.addHeader("Origin", HEADER_ORIGIN);
			ws.addHeader("User-Agent", HEADER_USER_AGENT);
			ws.addListener(this); // WebSocketAdapter
			ws.connect();
			
			login();
		} catch (IOException | WebSocketException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	// Close WebSocket
	public void disconnect() {
		if(ws != null && ws.isOpen()) {
			keepAlive.stop();
			ws.disconnect();
		}
	}
	
	// Try to initiate a session and login
	private void login() {
		loggedIn = false;
		expectedResponse = ExpectedResponse.LOGIN;
		
		// Random clientId as 16 base64-encoded bytes
		String clientId = credentials.getClientId();
		String loginRequest = Utils.buildWebsocketJsonRequest(RequestType.LOGIN, clientId);
		
		ws.sendText(loginRequest);
	}
	
	// Send restore request to the backend
	private void restoreSession() {
		String clientToken = credentials.getClientToken();
		String serverToken = credentials.getServerToken();
		String clientId = credentials.getClientId();
		
		String restoreRequest = Utils.buildWebsocketJsonRequest(RequestType.RESTORE_SESSION, 
				clientToken, serverToken, clientId);
		
		ws.sendText(restoreRequest);
	}
	
	// Solve login challenge by signing a byte array
	private void solveChallenge(String message) {
		// Extract base64 challenge
		String challenge = Utils.encodeValidJson(message, "\"Cmd\",").get("challenge").getAsString();
		byte[] decodedChallenge = Base64.getDecoder().decode(challenge);
		
		// Generate encryption / decryption and mac key
		EncryptionKeyPair keyPair = credentials.getEncryptionKeyPair();
		
		byte[] signedChallenge = Utils.signHMAC(keyPair.getMacKey(), decodedChallenge);
		
		// Sign challenge, encode it with base64 and send it back to the server
		String signedChallengeBase64 = Base64.getEncoder().encodeToString(signedChallenge);
		String serverToken = credentials.getServerToken();
		String clientId = credentials.getClientId();
		
		String challengeRequest = Utils.buildWebsocketJsonRequest(RequestType.SOLVE_CHALLENGE, 
				signedChallengeBase64, serverToken, clientId);
		
		ws.sendText(challengeRequest);
	}
	
	// QR code expired request a new one
	protected void requestNewServerId() {
		ws.sendText(Utils.buildWebsocketJsonRequest(RequestType.NEW_SERVER_ID, ""));
	}
	
	// Gets called after a successful login
	private void confirmLogin() {
		loggedIn = true;
		
		// Keep session alive by pinging the backend every 20-38 seconds
		keepAlive = new WAKeepAlive(ws);
		keepAlive.start();
		
		sendPostConnectQueries();
		
		expectedResponse = ExpectedResponse.MESSAGE_GENERIC;
		listener.onReceiveLoginResponse(200);
	}
	
	// Send generic text message
	public void sendMessage(String remoteJid, String messageContent) {
		String json = WAMessage.buildJson(remoteJid, messageContent, true, Instant.now().getEpochSecond());
		sendBinary(json, WAMetric.message, WAFlag.ignore);
	}
	
	// Delete the send message only for you
	public void clearMessage(String remoteJid, String messageId, boolean owner) {
		String modTag = Math.round(Math.random() * 1000000) + "";
		sendBinary("[\"action\", {epoch: \"" + Utils.getMessageCount() + "\", type: \"set\"}, "
				+ "[[\"chat\", {jid: \"" + remoteJid + "\", modify_tag: \"" + modTag +"\", type: \"clear\"}, "
				+ "[[\"item\", {owner: \"" + owner + "\", index: \"" + messageId +"\"}, null]]]]]", WAMetric.group, WAFlag.ignore);
	}
	
	// Load the last x messages of a direct chat or group
	public void loadConversation(String remoteJid, int messageCount) {
		sendBinary("[\"query\", {type: \"message\", epoch: \"" + Utils.getMessageCount() + "\", jid: \"" + remoteJid + "\", "
				+ "kind: \"before\", count: \"" + messageCount + "\"}, null]", WAMetric.queryMessages, WAFlag.ignore);
	}
	
	public void loadConversation(String remoteJid, int messageCount, String lastMessageId, boolean lastOwner) {
		sendBinary("[\"query\", {type: \"message\", epoch: \"" + Utils.getMessageCount() + "\", jid: \"" + remoteJid + "\", "
				+ "kind: \"before\", count: \"" + messageCount + "\", index: \"" + lastMessageId + "\", "
						+ "owner: \"" + lastOwner + "\"}, null]", WAMetric.queryMessages, WAFlag.ignore);
	}
	
	// First binary requests to the backend server
	// Inspired by the original Web implementation
	private void sendPostConnectQueries() {
		sendBinary("[\"query\", {type: \"contacts\", epoch: \"1\"}, null]", WAMetric.queryContact, WAFlag.ignore);
		sendBinary("[\"query\", {type: \"chat\", epoch: \"1\"}, null]", WAMetric.queryChat, WAFlag.ignore);
		sendBinary("[\"query\", {type: \"status\", epoch: \"1\"}, null]", WAMetric.queryStatus, WAFlag.ignore);
		sendBinary("[\"query\", {type: \"quick_reply\", epoch: \"1\"}, null]", WAMetric.queryQuickReply, WAFlag.ignore);
		sendBinary("[\"query\", {type: \"label\", epoch: \"1\"}, null]", WAMetric.queryLabel, WAFlag.ignore);
		sendBinary("[\"query\", {type: \"emoji\", epoch: \"1\"}, null]", WAMetric.queryEmoji, WAFlag.ignore);
		sendBinary("[\"action\", {type: \"set\", epoch: \"1\"}, [[\"presence\", {type: \"available\"}, null]]]", WAMetric.presence, (byte) 160);
	}
	
	private void sendBinary(String json, byte... waTags) {
		EncryptionKeyPair keyPair = credentials.getEncryptionKeyPair();
		ws.sendBinary(Utils.buildWebSocketBinaryRequest(keyPair, json, waTags));
	}
	
	public void addClientActionListener(ClientActionInterface listener) {
		this.listener = listener;
	}
	
	/*
	 * Receive messages using WebSocket and process it further
	 * 
	 */
	
	@Override
    public void onTextMessage(WebSocket websocket, String message) {
		// Pong answer
		if(message.startsWith("!")) {
			keepAlive.updatePong();
			return;
		}
		
		// Received text message
		System.out.println(message);
		
		// "Props" marks the last message send by the backend after login
		if(message.contains("\"Props\"") && !loggedIn) {
			confirmLogin();
		}
			
		
		String clientId = credentials.getClientId();
		
        switch(expectedResponse) {
        	// Logging in procedure
        	case ExpectedResponse.LOGIN:
            	// If we can not find any data of a previous session we create a new one
        		
            	if(credentials.getClientToken() == null) {
            		// Request new qr code
            		refresher = new WAScanRefresher(this, clientId, credentials.getCurveKeyPair().getPublicKey());
            		refresher.start();
            		refresher.newQRCode(message);
            		
            		expectedResponse = ExpectedResponse.SCAN_QR_CODE;
            	} else {
            		// Previous session found. Try to restore it...
            		expectedResponse = ExpectedResponse.RESTORE_SESSION;
            		restoreSession();
            	}
        		break;
        	case ExpectedResponse.RESTORE_SESSION:
        		// If WhatsApp send a challenge request to confirm if we still have the encryption keys
        		if(message.contains("challenge")) {
        			solveChallenge(message);
        		} else {
        			int status = Utils.encodeValidJson(message).get("status").getAsInt();
        			
        			if(status != 200) {
        				// A error occurred during authentication
        				// Old session data is invalid so it gets deleted
        				AuthCredentialsHelper.deletePreviousSession(authCredentialsPath);
        				disconnect();
        				listener.onReceiveLoginResponse(status);
        			}
        		}
        		break;
        	case ExpectedResponse.SCAN_QR_CODE:
        		// Logged in successfully
        		if(message.contains("\"Conn\"")) {
        			refresher.setQRCodeScanned(true);
        			// The message contains e.g. a secret, which is required to decrypt and encrypt
        			// binary messages
        			credentials.addAdditionalInformation(message);
        			// Save credentials as file
        			AuthCredentialsHelper.saveAuthCredentials(credentials, authCredentialsPath);
        		} else if(message.contains("ref")) {
        			// We received a message, which contains a new server id to create a new qr code
        			refresher.newQRCode(message);
        		}
        		break;
        }
    }
	
	@Override
	public void onBinaryMessage(WebSocket websocket, byte[] message) throws DecoderException {
		// Decrypt binary message
		byte[] decrypted = BinaryEncryption.decrypt(message, credentials.getEncryptionKeyPair());
		
		// Use protobuf to make messages of the type "message" human readable
		BinaryDecoder decoder = new BinaryDecoder();
		String json = decoder.decode(decrypted);
		
		// Transform strings to objects
		Object[] objects = WAMessage.jsonToObject(json);
		if(objects != null) {
			// Messages of these types are always stored in arrays
			if(objects[0] instanceof WebChat) {
				listener.onWebChat(Arrays.stream(objects).toArray(WebChat[]::new));
			} else if(objects[0] instanceof WebContact) {
				listener.onWebContact(Arrays.stream(objects).toArray(WebContact[]::new));
			} else if(objects[0] instanceof WebStatus) {
				listener.onWebStatus(Arrays.stream(objects).toArray(WebStatus[]::new));
			} else if(objects[0] instanceof WebEmoji) {
				listener.onWebEmoji(Arrays.stream(objects).toArray(WebEmoji[]::new));
			} 
			
			for(Object obj : objects) {
				if(obj instanceof WebImageMessage) {
					listener.onWebImageMessage((WebImageMessage) obj);
				} else if(obj instanceof WebVideoMessage) {
					listener.onWebVideoMessage((WebVideoMessage) obj);
				} else if(obj instanceof WebConversationMessage) {
					listener.onWebConversationMessage((WebConversationMessage) obj);
				}
			}
		}
		
		// Send all other types of messages directly to the active interface
		//listener.onTextMessage(json);
	}
}
