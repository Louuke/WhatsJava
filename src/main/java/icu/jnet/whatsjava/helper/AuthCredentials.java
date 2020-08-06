package icu.jnet.whatsjava.helper;

import java.util.Base64;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import com.google.gson.JsonObject;

import icu.jnet.whatsjava.encryption.CurveKeyPair;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;
import icu.jnet.whatsjava.encryption.EncryptionKeys;

public class AuthCredentials {

	private String clientId;
	private String serverToken;
	private String clientToken;
	private String encKey;
	private String macKey;
	
	private String curvePrivateKey;
	private String curvePublicKey;
	
	// Used when restoring a session
	public AuthCredentials(String clientId, String serverToken, String clientToken,
			String encKey, String macKey) {
		
		this.clientId = clientId;
		this.clientToken = clientToken;
		this.serverToken = serverToken;
		this.encKey = encKey;
		this.macKey = macKey;
	}
	
	// Used for new sessions
	public AuthCredentials() {
		// Generate key pair to encode a QR code and generate encryption keys 
		Curve25519KeyPair curveKeys = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
		curvePrivateKey = Base64.getEncoder().encodeToString(curveKeys.getPrivateKey());
		curvePublicKey = Base64.getEncoder().encodeToString(curveKeys.getPublicKey());
		
		// Encode random clientId with base64
		clientId = Base64.getEncoder().encodeToString(Utils.randomBytes(16));
	}
	
	// Save session information to restore it in the future
	public AuthCredentials addAdditionalInformation(String message) {
				
		// Trim start and end of the message to convert it into a valid json format
		JsonObject jsonEncoded = Utils.encodeValidJson(message, "\"Conn\",");
				
		String serverToken = jsonEncoded.get("serverToken").getAsString();
		String clientToken = jsonEncoded.get("clientToken").getAsString();
				
		String secret = jsonEncoded.get("secret").getAsString();
		EncryptionKeyPair keyPair = EncryptionKeys.generate(secret, getCurveKeyPair().getPrivateKey());
				
		String encKey = Base64.getEncoder().encodeToString(keyPair.getEncKey());
		String macKey = Base64.getEncoder().encodeToString(keyPair.getMacKey());
				
		// Save information
		this.clientToken = clientToken;
		this.serverToken = serverToken;
		this.encKey = encKey;
		this.macKey = macKey;
		
		return this;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getServerToken() {
		return serverToken;
	}
	
	public String getClientToken() {
		return clientToken;
	}
	
	public EncryptionKeyPair getEncryptionKeyPair() {
		return new EncryptionKeyPair(Base64.getDecoder().decode(encKey), 
				Base64.getDecoder().decode(macKey));
	}
	
	public CurveKeyPair getCurveKeyPair() {
		return new CurveKeyPair(Base64.getDecoder().decode(curvePrivateKey),
				Base64.getDecoder().decode(curvePublicKey));
	}
}
