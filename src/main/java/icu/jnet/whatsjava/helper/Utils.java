package icu.jnet.whatsjava.helper;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.encryption.BinaryEncoder;
import icu.jnet.whatsjava.encryption.BinaryEncryption;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;

public class Utils {

	// Number of created message tags
	private static int wsRequestCount = 0;
	
	// Binary messages get a different kind of message tags and it does not change
	private static String binaryMessageTag = "";
	
	
	public static void waitMillis(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/* Generate random byte array of specified length */
	public static byte[] randomBytes(int length) {
		Random rand = new Random();
		byte[] clientId = new byte[length];
		rand.nextBytes(clientId);
		
		return clientId;
	}
	
	/* WhatsApp adds a tag to most of the json messages. That's why we need to remove it */
	public static JsonObject encodeValidJson(String message, String splitStart) {
		String rawSplittedMessage = message.replaceFirst(splitStart, "##").split("##")[1];
		String rawMessage = rawSplittedMessage.substring(0, rawSplittedMessage.length() - 1);
		return JsonParser.parseString(rawMessage).getAsJsonObject();
	}
	
	/* Default split char [,] */
	public static JsonObject encodeValidJson(String message) {
		String raw = message.replaceFirst("[,]", "##").split("##")[1];
		return JsonParser.parseString(raw).getAsJsonObject();
	}
	
	/* WhatsApp needs a message tag at the start of every Websocket request */
	private static String getMessageTag() {
		String messageTag = Instant.now().getEpochSecond() + ".--" + wsRequestCount++;
		return messageTag;
	}
	
	/* WhatsApp binary message tags look different */
	private static String getBinaryMessageTag() {
		if(binaryMessageTag.equals("")) {
			binaryMessageTag = (new Random().nextInt(900) + 100) + "";
		}
		String messageTag = binaryMessageTag + ".--" + wsRequestCount++;
		
		return messageTag;
	}
	
	public static int getMessageCount() {
		return wsRequestCount;
	}
	
	/* Create a new websocket json request string  */
	public static String buildWebsocketJsonRequest(int requestType, String... content) {
		String messageTag = getMessageTag();
		
		String request = "";
		
		switch(requestType) {
			case RequestType.LOGIN:
				request = "[\"admin\",\"init\",[2,2029,4],[\"Ubuntu\",\"Firefox\",\"Unknown\"],\""
						+ "" + content[0] + "\",true]";
				break;
			case RequestType.RESTORE_SESSION:
				request = "[\"admin\",\"login\","
						+ "\"" + content[0] + "\","
						+ "\"" + content[1] + "\","
						+ "\"" + content[2] + "\",\"takeover\"]";
				break;
			case RequestType.SOLVE_CHALLENGE:
				request = "[\"admin\",\"challenge\","
						+ "\"" + content[0] + "\","
						+ "\"" + content[1] + "\","
						+ "\"" + content[2] + "\"]";
				break;
			case RequestType.NEW_SERVER_ID:
				request = "[\"admin\",\"Conn\",\"reref\"]";
				break;
		}
		
		request = messageTag + "," + request;
		return request;
	}
	
	/* Create a new websocket binary request array  */
	public static byte[] buildWebsocketBinaryRequest(EncryptionKeyPair keyPair, String json, byte... waTags) {
		String tag = null;
		
		if(json.contains("extendedTextMessage")) {
			tag = json.split(" id: \"")[1].split("\"")[0] + ",";
		}
		
		// waTags tells WA what the message is about
		
		BinaryEncoder encoder = new BinaryEncoder();
		byte[] encoded = encoder.encode(json);
		
		byte[] encrypted = BinaryEncryption.encrypt(encoded, keyPair);
		byte[] hmacSign = Utils.signHMAC(keyPair.getMacKey(), encrypted);
		byte[] messageTag = tag != null ? tag.getBytes() : (Utils.getBinaryMessageTag() + ",").getBytes();
		
		return ByteBuffer.allocate(
				messageTag.length + waTags.length + hmacSign.length + encrypted.length)
				.put(messageTag).put(waTags).put(hmacSign).put(encrypted).array();
	}
	
	/* Implementation: https://github.com/danharper/hmac-examples */
	public static byte[] signHMAC(byte[] hmacValidationKey, byte[] hmacValidationMessage) {
	    try {
	    	Mac hasher = Mac.getInstance("HmacSHA256");
			hasher.init(new SecretKeySpec(hmacValidationKey, "HmacSHA256"));
			
			byte[] hash = hasher.doFinal(hmacValidationMessage);
			return hash;
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
