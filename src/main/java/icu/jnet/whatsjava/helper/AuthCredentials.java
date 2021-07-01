package icu.jnet.whatsjava.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import icu.jnet.whatsjava.encryption.CurveKeyPair;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;
import icu.jnet.whatsjava.encryption.EncryptionKeys;

public class AuthCredentials {

	private final String clientId;
	private String serverToken;
	private String clientToken;
	private String encKey;
	private String macKey;
	
	private String curvePrivateKey;
	private String curvePublicKey;
	
	// Used when restoring a session
	public AuthCredentials(String clientId, String serverToken, String clientToken, String encKey, String macKey) {
		this.clientId = clientId;
		this.clientToken = clientToken;
		this.serverToken = serverToken;
		this.encKey = encKey;
		this.macKey = macKey;
	}
	
	// Used for new sessions
	public AuthCredentials() {
		// Encode random clientId with base64
		clientId = Base64.getEncoder().encodeToString(Utils.randomBytes(16));

		// Generate key pair to encode a QR code and generate encryption keys 
		Curve25519KeyPair curveKeys = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
		curvePrivateKey = Base64.getEncoder().encodeToString(curveKeys.getPrivateKey());
		curvePublicKey = Base64.getEncoder().encodeToString(curveKeys.getPublicKey());
	}

	// Save session information to restore it in the future
	public void setSessionEncryptionInfo(String clientToken, String serverToken, String secret) {
		EncryptionKeyPair keyPair = EncryptionKeys.generate(secret, getCurveKeyPair().getPrivateKey());

		String encKey = Base64.getEncoder().encodeToString(keyPair.getEncKey());
		String macKey = Base64.getEncoder().encodeToString(keyPair.getMacKey());

		this.clientToken = clientToken;
		this.serverToken = serverToken;
		this.encKey = encKey;
		this.macKey = macKey;
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

	public boolean mayRestore() {
		return clientToken != null & serverToken != null && encKey != null && macKey != null;
	}

	public static AuthCredentials loadAuthCredentials(String authCredentialsPath) {
		// If no previous saved session file exist, we create a new one
		if(!new File(authCredentialsPath).exists())
			return new AuthCredentials();

		try {
			// Load credential file and read it as json string
			List<String> list = Files.readAllLines(Paths.get(authCredentialsPath));

			StringBuilder builder = new StringBuilder();
			for(String line : list) {
				builder.append(line);
			}

			JsonObject jsonCredentials = JsonParser.parseString(builder.toString()).getAsJsonObject();

			String clientId = jsonCredentials.get("clientId").getAsString();
			String serverToken = jsonCredentials.get("serverToken").getAsString();
			String clientToken = jsonCredentials.get("clientToken").getAsString();
			String encKey = jsonCredentials.get("encKey").getAsString();
			String macKey = jsonCredentials.get("macKey").getAsString();

			// Create AuthCredentials object and return it
			return new AuthCredentials(clientId, serverToken, clientToken, encKey, macKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new AuthCredentials();
	}

	public static void saveAuthCredentials(AuthCredentials credentials, String authCredentialsPath) {
		try {
			// Convert credentials object into a json string and save it
			Files.writeString(Paths.get(authCredentialsPath),
					new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(credentials));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deletePreviousSession(String authCredentialsPath) {
		File file = new File(authCredentialsPath);
		if(file.exists()) {
			file.delete();
		}
	}
}
