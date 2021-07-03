package icu.jnet.whatsjava.messages;

import java.util.Base64;
import java.util.Set;

import com.google.gson.*;
import icu.jnet.whatsjava.messages.generic.*;

import com.google.protobuf.InvalidProtocolBufferException;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.messages.stub.WAStubMessage;

public class WAMessageParser {

	private static WAChat[] storedWAChats;
	private static WAContact[] storedWAContacts;
	
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
							objects = !keys.contains("duplicate") ? chatToObject(childrenArray) : storedWAChats;
							break;
						case "contacts":
							objects = !keys.contains("duplicate") ? contactToObject(childrenArray) : storedWAContacts;
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

	// Convert json message of the type "message" into a WAMessage object array
	private static WAMessage[] messageToObject(JsonArray childrenArray) {
		WAMessage[] messages = new WAMessage[childrenArray.size()];

		for(int i = 0; i < childrenArray.size(); i++) {
			// WebMessageInfo objects are encoded with base64 and need to be decoded
			String base64Message = childrenArray.get(i).getAsJsonArray().get(2).getAsJsonArray().get(0).getAsString();
			byte[] byteMessage = Base64.getDecoder().decode(base64Message);

			try {
				WebMessageInfo webMessageInfo = WebMessageInfo.parseFrom(byteMessage);
				WAMessage message = new WAMessage(webMessageInfo);

				// Create new message objects depending on the message type
				if(webMessageInfo.getMessage().hasImageMessage()) {
					message.setImageMessage(new WAImageMessage(webMessageInfo));
				} else if(webMessageInfo.getMessage().hasVideoMessage()) {
					message.setVideoMessage(new WAVideoMessage(webMessageInfo));
				} else if(webMessageInfo.getMessage().hasConversation() || webMessageInfo.getMessage().hasExtendedTextMessage()) {
					message.setConversationMessage(new WAConversationMessage(webMessageInfo));
				} else if(webMessageInfo.hasMessageStubType()) {
					message.setStubMessage(new WAStubMessage(webMessageInfo));
				}
				messages[i] = message;
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return messages;
	}
	
	// Convert json message of the type "chat" into WAChat
	private static WAChat[] chatToObject(JsonArray childrenArray) {
		WAChat[] WAChats = new WAChat[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString() : null;
			int unreadMessages = chatAttributes.get("count").getAsInt();
			long lastInteraction = chatAttributes.get("t").getAsLong();
			boolean muted = chatAttributes.get("mute").getAsBoolean();

			WAChats[i] = new WAChat(jid, name, unreadMessages, lastInteraction, muted);
		}
		storedWAChats = WAChats;
		return WAChats;
	}
	
	// Convert json message of the type "contacts" into WebChat
	private static WAContact[] contactToObject(JsonArray childrenArray) {
		WAContact[] contacts = new WAContact[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString()
					: chatAttributes.keySet().contains("notify") ? chatAttributes.get("notify").getAsString() : null;

			contacts[i] = new WAContact(jid, name);
		}
		storedWAContacts = contacts;
		return contacts;
	}

	// Convert json message of the type "status" into WebStatus
	private static WAStatus[] statusToObject(JsonArray childrenArray) {
		JsonArray messageArray = childrenArray.get(0).getAsJsonArray().get(2).getAsJsonArray();
		WAStatus[] waStatuses = new WAStatus[messageArray.size()];
		
		for(int i = 0; i < messageArray.size(); i++) {
			String base64Message = messageArray.get(i).getAsJsonArray().get(2).getAsJsonArray().get(0).getAsString();
			
			try {
				WebMessageInfo message = WebMessageInfo.parseFrom(Base64.getDecoder().decode(base64Message));
				
				if(message.getMessage().hasImageMessage()) {
					// WebMessageInfo to WebImageMessage object
					waStatuses[i] = new WAStatus(new WAMessage(message).setImageMessage(new WAImageMessage(message)));
				} else if(message.getMessage().hasVideoMessage()) {
					// WebMessageInfo to WebVideoMessage object
					waStatuses[i] = new WAStatus(new WAVideoMessage(message));
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return waStatuses;
	}
	
	// Convert json message of the type "status" into WebEmoji
	private static WAEmoji[] emojiToObject(JsonArray childrenArray) {
		WAEmoji[] waEmojis = new WAEmoji[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject emojiAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String code = emojiAttributes.get("code").getAsString();
			double value = Double.parseDouble(emojiAttributes.get("value").getAsString());

			waEmojis[i] = new WAEmoji(code, value);
		}
		return waEmojis;
	}
}
