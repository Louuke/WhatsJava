package icu.jnet.whatsjava.encryption;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

import org.whispersystems.curve25519.Curve25519;

import icu.jnet.whatsjava.helper.Utils;

public class EncryptionKeys {

	/* Generates the encryption keys to encrypt and decrypt WhatsApp messages */
	public static EncryptionKeyPair generate(String base64Secret, byte[] privateKey) {
		byte[] secret = Base64.getDecoder().decode(base64Secret);
		
		if(secret.length != 144) {
			System.err.println("Invalid secret length received: " + secret.length);
			return null;
		}
		
		// Generate shared key from our private key and the secret shared by the server
		byte[] publicKey = Arrays.copyOfRange(secret, 0, 32);
		byte[] sharedSecret = Curve25519.getInstance(Curve25519.BEST)
				.calculateAgreement(publicKey, privateKey);
		// Expand the shared key to 80 bytes using HKDF
		byte[] sharedSecretExpanded = Utils.expandUsingHKDF(sharedSecret, 80, null);
		
		// Validate data by HMAC
		boolean valid = hmacValidate(sharedSecretExpanded, secret);
		//System.out.println("Data is valid: " + valid);
		
		if(valid) {
			// sharedSecretExpanded[64:] + secret[64:] are the keys, encrypted using AES, that are 
			// used to encrypt / decrypt the messages recieved from WhatsApp
            // They are encrypted using key: sharedSecretExpanded[0:32]
			byte[] sharedEnc = Arrays.copyOfRange(sharedSecretExpanded, 64, sharedSecretExpanded.length);
			byte[] secretEnc = Arrays.copyOfRange(secret, 64, secret.length);
			
			byte[] keysEncrypted = ByteBuffer.allocate(sharedEnc.length + secretEnc.length)
					.put(sharedEnc)
					.put(secretEnc)
					.array();
			
			byte[] decryptKey = Arrays.copyOfRange(sharedSecretExpanded, 0, 32);
			
			byte[] keysDecrypted = AES.decrypt(keysEncrypted, decryptKey);
			
			// Keys decrypted successfully
			if(keysDecrypted.length == 64) {
				//System.out.println("AES keys decrypted");
				
				// Decrypts / Encrypts messages
				byte[] encKey = Arrays.copyOfRange(keysDecrypted, 0, 32);
				byte[] macKey = Arrays.copyOfRange(keysDecrypted, 32, 64);
				
				return new EncryptionKeyPair(encKey, macKey);
			}
		}
		return null;
	}
	
	private static boolean hmacValidate(byte[] sharedSecretExpanded, byte[] secret) {
		byte[] hmacValidationKey = Arrays.copyOfRange(sharedSecretExpanded, 32, 64);
		byte[] hmacSecretA = Arrays.copyOfRange(secret, 0, 32);
		byte[] hmacSecretB = Arrays.copyOfRange(secret, 64, secret.length);
		
		// Concatenate splitted HMAC secrets
		byte[] hmacValidationMessage = ByteBuffer.allocate(hmacSecretA.length + hmacSecretB.length)
			.put(hmacSecretA)
			.put(hmacSecretB)
			.array();
		
		byte[] hmac = Utils.signHMAC(hmacValidationKey, hmacValidationMessage);
		
		// Computed HMAC should equal secret[32:64], otherwise the data provided by the server is invalid
		return Arrays.equals(hmac, Arrays.copyOfRange(secret, 32, 64));
	}
}
