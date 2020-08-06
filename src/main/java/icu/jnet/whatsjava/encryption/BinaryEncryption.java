package icu.jnet.whatsjava.encryption;

import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import icu.jnet.whatsjava.helper.Utils;

public class BinaryEncryption {

	public static byte[] decrypt(byte[] message, EncryptionKeyPair keyPair) throws DecoderException {
		// Encode byte array to hex char
		String hexMessage = Hex.encodeHexString(message, true);
		
		// WhatsApp messages have a tag and a comma at the start
		// After that you can find the actual message
		// Hex 0x2c = UTF-8 ,
		int commaIndex = hexMessage.indexOf("2c");
		
		if(commaIndex < 1) {
			// This message is invalid
			System.err.println("Invalid binary message");
			return null;
		}
		
		String strMessageContent = hexMessage.substring(commaIndex + 2, hexMessage.length());
		byte[] messageContent = Hex.decodeHex(strMessageContent);
		
		// The actual message starts with a 32 byte long checksum
		byte[] checksum = Arrays.copyOfRange(messageContent, 0, 32);
		messageContent = Arrays.copyOfRange(messageContent, 32, messageContent.length);
		byte[] hmacComputedChecksum = Utils.signHMAC(keyPair.getMacKey(), messageContent);
		
		if(Arrays.equals(hmacComputedChecksum, checksum)) {
			return AES.decrypt(messageContent, keyPair.getEncKey());
		}
		return null;
	}
	
	public static byte[] encrypt(byte[] message, EncryptionKeyPair keyPair) {
		return AES.encrypt(message, keyPair.getEncKey());
	}
}
