package icu.jnet.whatsjava.messages;

import java.util.Base64;
import java.util.Set;

import icu.jnet.whatsjava.messages.web.*;
import org.apache.commons.codec.binary.Hex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.ExtendedTextMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.Message;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.MessageKey;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS;
import icu.jnet.whatsjava.helper.Utils;

public class WAMessageParser {

	private static WebChat[] storedWebChats;
	private static WebContact[] storedWebContacts;
	
	/*
	 * Transforms json messages to objects to make it easier to work with them
	 */
	public static Object[] jsonToObjects(String json) {
		Object[] objects = null;

		if(json != null) {
			JsonArray node = JsonParser.parseString(json).getAsJsonArray();

			if(!node.get(1).isJsonNull() && !node.get(2).isJsonNull()) {
				JsonObject attributes = node.get(1).getAsJsonObject();
				// Attributes key values
				Set<String> keys = attributes.keySet();

				// Contains node content
				JsonArray childrenArray = node.get(2).getAsJsonArray();

				if (keys.contains("type")) {
					String typeValue = attributes.get("type").getAsString();

					switch (typeValue) {
						case "message":
							objects = messageToObject(childrenArray);
							break;
						case "chat":
							objects = !keys.contains("duplicate") ? chatToObject(childrenArray) : storedWebChats;
							break;
						case "contacts":
							objects = !keys.contains("duplicate") ? contactToObject(childrenArray) : storedWebContacts;
							break;
						case "status":
							objects = statusToObject(childrenArray);
							break;
						case "emoji":
							objects = emojiToObject(childrenArray);
							break;
					}
				}
			}
		}

		return objects;
	}

	// Convert json message of the type "message" into a WebMessageInfo object array
	private static WebMessage[] messageToObject(JsonArray childrenArray) {
		WebMessage[] messages = new WebMessage[childrenArray.size()];

		for(int i = 0; i < childrenArray.size(); i++) {
			// WebMessageInfo objects are encoded with base64 and need to be decoded
			String base64Message = childrenArray.get(i).getAsJsonArray().get(2).getAsJsonArray().get(0).getAsString();
			byte[] byteMessage = Base64.getDecoder().decode(base64Message);

			try {
				WebMessageInfo message = WebMessageInfo.parseFrom(byteMessage);

				// Create new message objects depending on the message type
				if(message.getMessage().hasImageMessage()) {
					messages[i] = new WebImageMessage(message);
				} else if(message.getMessage().hasVideoMessage()) {
					messages[i] = new WebVideoMessage(message);
				} else if(message.getMessage().hasConversation() || message.getMessage().hasExtendedTextMessage()) {
					messages[i] = new WebConversationMessage(message);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return messages;
	}
	
	// Convert json message of the type "chat" > WebChat
	private static WebChat[] chatToObject(JsonArray childrenArray) {
		WebChat[] webChats = new WebChat[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString() : null;
			int unreadMessages = chatAttributes.get("count").getAsInt();
			long lastInteraction = chatAttributes.get("t").getAsLong();
			boolean muted = chatAttributes.get("mute").getAsBoolean();

			webChats[i] = new WebChat(jid, name, unreadMessages, lastInteraction, muted);
		}
		storedWebChats = webChats;
		return webChats;
	}
	
	// Convert json message of the type "contacts" > WebChat
	private static WebContact[] contactToObject(JsonArray childrenArray) {
		WebContact[] contacts = new WebContact[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString()
					: chatAttributes.keySet().contains("notify") ? chatAttributes.get("notify").getAsString() : null;

			contacts[i] = new WebContact(jid, name);
		}
		storedWebContacts = contacts;
		return contacts;
	}

	// Convert json message of the type "status" > WebStatus
	private static Object[] statusToObject(JsonArray childrenArray) {
		JsonArray messageArray = childrenArray.get(0).getAsJsonArray().get(2).getAsJsonArray();
		Object[] objects = new Object[messageArray.size()];
		
		for(int i = 0; i < messageArray.size(); i++) {
			String base64Message = messageArray.get(i).getAsJsonArray().get(2).getAsJsonArray()
					.get(0).getAsString();
			
			try {
				WebMessageInfo message = WebMessageInfo.parseFrom(Base64.getDecoder().decode(base64Message));
				
				if(message.getMessage().hasImageMessage()) {
					// WebMessageInfo to WebImageMessage object
					objects[i] = new WebStatus(new WebImageMessage(message));
				} else if(message.getMessage().hasVideoMessage()) {
					// WebMessageInfo to WebVideoMessage object
					objects[i] = new WebStatus(new WebVideoMessage(message));
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return objects;
	}
	
	// Convert json message of the type "status" > WebEmoji
	private static Object[] emojiToObject(JsonArray childrenArray) {
		Object[] objects = new Object[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject emojiAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String code = emojiAttributes.get("code").getAsString();
			double value = Double.parseDouble(emojiAttributes.get("value").getAsString());
			
			objects[i] = new WebEmoji(code, value);
		}
		return objects;
	}
}
