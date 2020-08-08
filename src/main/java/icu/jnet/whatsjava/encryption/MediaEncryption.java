package icu.jnet.whatsjava.encryption;

import java.nio.ByteBuffer;
import java.util.Arrays;

import icu.jnet.whatsjava.helper.Utils;

public class MediaEncryption {

	/*
	 * Decrypt E2E media
	 * 
	 */
	
	// Depending on the media type a different info parameter is used for the HKDF function
	public static String MEDIA_TYPE_IMAGE = "WhatsApp Image Keys",
			MEDIA_TYPE_VIDEO = "WhatsApp Video Keys",
			MEDIA_TYPE_AUDIO = "WhatsApp Audio Keys",
			MEDIA_TYPE_DOCUMENT = "WhatsApp Document Keys";
			
	
	public static byte[] decrypt(byte[] mediaKey, String url, String mediaType) {
		// Expand mediaKey to 112 bytes and add mediaInfo
		byte[] mediaKeyExpanded = Utils.expandUsingHKDF(mediaKey, 112, mediaType.getBytes());
		
		byte[] iv = Arrays.copyOfRange(mediaKeyExpanded, 0, 16);
		byte[] cipherKey = Arrays.copyOfRange(mediaKeyExpanded, 16, 48);
		byte[] macKey = Arrays.copyOfRange(mediaKeyExpanded, 48, 80);
		// refKey mediaKeyExpanded[80:112] not used
		
		// Download encrypted media
		byte[] encryptedMedia = Utils.urlToEncMedia(url);
		
		if(encryptedMedia != null) {
			byte[] file = Arrays.copyOfRange(encryptedMedia, 0, encryptedMedia.length - 10);
			byte[] mac = Arrays.copyOfRange(encryptedMedia, encryptedMedia.length - 10, encryptedMedia.length);
			
			// Hmac sign message
			byte[] message = ByteBuffer.allocate(iv.length + file.length).put(iv).put(file).array();
			
			// Validate macKey of mediaKeyExpanded with mac key of the encrypted media
			byte[] hmacSign = Utils.signHMAC(macKey, message);
			
			// Media validated
			if(Arrays.equals(mac, Arrays.copyOfRange(hmacSign, 0, 10))) {
				return AES.decrypt(file, cipherKey, iv);
			}
		}
		
		return null;
	}
}
