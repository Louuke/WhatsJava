package icu.jnet.whatsjava.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.favre.lib.crypto.HKDF;
import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.encryption.BinaryEncoder;
import icu.jnet.whatsjava.encryption.BinaryEncryption;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;

public class Utils {

	// Number of created message tags
	private static int wsRequestCount = 0;
	
	// Binary messages get a different type of message tags and they do not change
	private static String binaryMessageTag = "";
	
	// Generate random byte array of specified length
	public static byte[] randomBytes(int length) {
		Random rand = new Random();
		byte[] clientId = new byte[length];
		rand.nextBytes(clientId);
		
		return clientId;
	}
	
	// WhatsApp adds a tag to most of the json messages. That's why we need to remove it
	public static JsonObject encodeValidJson(String message, String splitStart) {
		String rawSplitMessage = message.replaceFirst(splitStart, "##").split("##")[1];
		String rawMessage = rawSplitMessage.substring(0, rawSplitMessage.length() - 1);
		return JsonParser.parseString(rawMessage).getAsJsonObject();
	}
	
	// Default split char [,]
	public static JsonObject encodeValidJson(String message) {
		String raw = message.replaceFirst("[,]", "##").split("##")[1];
		return JsonParser.parseString(raw).getAsJsonObject();
	}
	
	// WhatsApp needs a message tag at the start of every WebSocket request
	private static String getMessageTag() {
		return Instant.now().getEpochSecond() + ".--" + wsRequestCount++;
	}
	
	// WhatsApp binary message tags look different
	private static String getBinaryMessageTag() {
		if(binaryMessageTag.isEmpty()) {
			binaryMessageTag = String.valueOf(new Random().nextInt(900) + 100);
		}

		return binaryMessageTag + ".--" + wsRequestCount++;
	}
	
	public static int getMessageCount() {
		return wsRequestCount;
	}

	/**
	 * Create a new WebSocket json request string
	 *
	 * @param requestType Specifies the type of the json message
	 * @param content Information required to create the message
	 * @return Correctly formatted json request
	 */
	public static String buildWebSocketJsonRequest(int requestType, String... content) {
		String messageTag = getMessageTag();
		
		String request = "";
		
		switch(requestType) {
			case RequestType.LOGIN:
				request = String.format("[\"admin\", \"init\", [2,2121,6], [\"API\", \"WhatsJava\", \"Unknown\"], \"%s\", true]", content[0]);
				break;
			case RequestType.RESTORE_SESSION:
				request = String.format("[\"admin\", \"login\", \"%s\", \"%s\", \"%s\", \"takeover\"]", content[0], content[1], content[2]);
				break;
			case RequestType.SOLVE_CHALLENGE:
				request = String.format("[\"admin\", \"challenge\", \"%s\", \"%s\", \"%s\"]", content[0], content[1], content[2]);
				break;
			case RequestType.NEW_SERVER_ID:
				request = "[\"admin\",\"Conn\",\"reref\"]";
				break;
			case RequestType.QUERY_PROFILE_PICTURE:
				request = String.format("[\"query\", \"ProfilePicThumb\", \"%s\"]", content[0]);
				break;
		}

		return messageTag + "," + request;
	}
	
	// Create a new WebSocket binary request array
	public static byte[] buildWebSocketBinaryRequest(EncryptionKeyPair keyPair, String json, byte... waTags) {
		String tag = null;
		
		if(json.contains("extendedTextMessage")) {
			tag = json.split(" id: \"")[1].split("\"")[0] + ",";
		}
		
		BinaryEncoder encoder = new BinaryEncoder();
		byte[] encoded = encoder.encode(json);
		
		byte[] encrypted = BinaryEncryption.encrypt(encoded, keyPair);
		byte[] hmacSign = Utils.signHMAC(keyPair.getMacKey(), encrypted);
		byte[] messageTag = tag != null ? tag.getBytes() : (Utils.getBinaryMessageTag() + ",").getBytes();

		// waTags tell WA what the message is about
		return ByteBuffer.allocate(messageTag.length + waTags.length + hmacSign.length + encrypted.length)
				.put(messageTag).put(waTags).put(hmacSign).put(encrypted).array();
	}
	
	// HLDF key expansion
	public static byte[] expandUsingHKDF(byte[] key, int expandedLength, byte[] info) {
		byte[] pseudoRandomKey = HKDF.fromHmacSha256().extract(null, key);
		return HKDF.fromHmacSha256().expand(pseudoRandomKey, info, expandedLength);
	}
	
	// Implementation: https://github.com/danharper/hmac-examples
	public static byte[] signHMAC(byte[] hmacValidationKey, byte[] hmacValidationMessage) {
	    try {
	    	Mac hasher = Mac.getInstance("HmacSHA256");
			hasher.init(new SecretKeySpec(hmacValidationKey, "HmacSHA256"));

			return hasher.doFinal(hmacValidationMessage); // return hash
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Download encrypted media file
	public static byte[] urlToEncryptedMedia(String url) {
		try {
			// Create random temporary file
			Path path = Files.createTempFile(null, ".enc");
			File tmpFile = path.toFile();
			
			byte[] encryptedMedia = null;
			// Download encrypted file
			try {
				FileUtils.copyURLToFile(new URL(url), tmpFile);
				// Convert file to byte array
				encryptedMedia = Files.readAllBytes(path);
			} catch(FileNotFoundException ignored) {}
			
			tmpFile.delete();
			return encryptedMedia;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Download unencrypted media file
	public static byte[] urlToUnencryptedMedia(String url) {
		try {
			// Create random temporary file
			Path path = Files.createTempFile(null, ".jpg");
			File tmpFile = path.toFile();

			byte[] unencryptedMedia = null;
			// Download unencrypted file
			FileUtils.copyURLToFile(new URL(url), tmpFile);
			// Convert file to byte array
			unencryptedMedia = Files.readAllBytes(path);

			tmpFile.delete();
			return unencryptedMedia;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void waitMill(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
