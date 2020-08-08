package icu.jnet.whatsjava.encryption;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import icu.jnet.whatsjava.helper.Utils;

class AES {
	
	// Decrypt encrypted keys with AES and CBC mode
	// The initialization vector iv can be found at the start of the encrypted keys array
	static byte[] decrypt(byte[] encrypted, byte[] secretKey) {
	    try 
	    {
	    	byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
	    	
	    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // PKCS5PADDING required
	    	SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
	    	cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

	    	byte[] encryptedMessage = Arrays.copyOfRange(encrypted, 16, encrypted.length);
	    	
	    	return cipher.doFinal(encryptedMessage);
	    } catch (Exception e) {
	        System.out.println("Error while decrypting: " + e.toString());
	    }
	    return null;
	}
	
	// Used for E2E media decryption in the MediaEncryption class
	static byte[] decrypt(byte[] encrypted, byte[] secretKey, byte[] iv) {
	    try 
	    {
	    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // PKCS5PADDING required
	    	SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
	    	cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
	    	
	    	return cipher.doFinal(encrypted);
	    } catch (Exception e) {
	        System.out.println("Error while decrypting: " + e.toString());
	    }
	    return null;
	}
	
	// Encrypt using AES and encKey
	static byte[] encrypt(byte[] decrypted, byte[] encKey) {
	    try 
	    {
	    	byte[] iv = Utils.randomBytes(16);
	    	
	    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // PKCS5PADDING required
	    	SecretKeySpec secretKeySpec = new SecretKeySpec(encKey, "AES");
	    	cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
	    	byte[] encrypted = cipher.doFinal(decrypted);
	    	
	    	// Prefix iv
	    	return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
	    } catch (Exception e) {
	        System.out.println("Error while decrypting: " + e.toString());
	    }
	    return null;
	}
}
