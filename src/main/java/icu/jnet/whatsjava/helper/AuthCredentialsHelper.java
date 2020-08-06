package icu.jnet.whatsjava.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuthCredentialsHelper {
	
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
			
			// Create AuthCredentials object and return it to WASession
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
