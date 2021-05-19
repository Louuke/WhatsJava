package icu.jnet.whatsjava;

import java.util.Base64;
import java.util.Set;

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
import icu.jnet.whatsjava.web.WebChat;
import icu.jnet.whatsjava.web.WebContact;
import icu.jnet.whatsjava.web.WebConversationMessage;
import icu.jnet.whatsjava.web.WebEmoji;
import icu.jnet.whatsjava.web.WebImageMessage;
import icu.jnet.whatsjava.web.WebStatus;
import icu.jnet.whatsjava.web.WebVideoMessage;

public class WAMessage {
	
	// Create a new message object and encode it with base64 later on
	// It gets send to the WhatsApp backend
	public static String buildJson(String remoteJid, String messageContent, boolean fromMe, long timestamp) {
		WebMessageInfo message = WebMessageInfo.newBuilder()
				.setMessage(Message.newBuilder().setExtendedTextMessage(
						ExtendedTextMessage.newBuilder().setText(messageContent).build()))
				.setKey(MessageKey.newBuilder().setFromMe(fromMe).setRemoteJid(remoteJid)
						.setId(generateMessageID()).build())
				.setMessageTimestamp(timestamp)
				.setStatus(WEB_MESSAGE_INFO_STATUS.PENDING).build();
		
		String base64Message = Base64.getEncoder().encodeToString(message.toByteArray());
		
		return "[\"action\", {epoch: \""  + Utils.getMessageCount() 
				+ "\", type: \"relay\"}, [[\"message\", null, "
				+ "{ \"webmessage\": \"" + base64Message + "\"}]]]";
	}
	
	// Generate random byte id to mark a message with it
	private static String generateMessageID() {
		return Hex.encodeHexString(Utils.randomBytes(10), false);
	}
	
	/*
	 * Transforms json messages to objects to make it easier to work with them
	 * 
	 */
	public static Object[] jsonToObject(String json) {
		JsonArray node = JsonParser.parseString(json).getAsJsonArray();
		
		JsonObject attributes = node.get(1).getAsJsonObject();
		// Attributes key values
		Set<String> keys = attributes.keySet();
		
		// Contains node content
		JsonArray childrenArray = node.get(2).getAsJsonArray();
		
		Object[] objects = null;
		
		if(keys.contains("add")) { // Generic message
			objects = messageToObject(childrenArray);
		} else if(!keys.contains("duplicate") && keys.contains("type")) {
			
			String typeValue = attributes.get("type").getAsString();
			
			switch(typeValue) {
				case "message":
					// Also generic message but gets called as response of the loadConversation() method
					objects = messageToObject(childrenArray);
					break;
				case "chat":
					objects = chatToObject(childrenArray);
					break;
				case "contacts":
					objects = contactToObject(childrenArray);
					break;
				case "status":
					objects = statusToObject(childrenArray);
					break;
				case "emoji":
					objects = emojiToObject(childrenArray);
					break;
			}
		}
		return objects;
	}
	
	// Convert json message of the type "message" > WebImageMessage, WebVideoMessage or WebConversationMessage
	private static Object[] messageToObject(JsonArray childrenArray) {
		Object[] objects = new Object[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			// WebMessageInfo objects are encoded with base64 and need to be decoded
			String base64Message = childrenArray.get(i).getAsJsonArray().get(2).getAsJsonArray().get(0).getAsString();
			
			byte[] byteMessage = Base64.getDecoder().decode(base64Message);
			
			try {
				WebMessageInfo message = WebMessageInfo.parseFrom(byteMessage);
				
				// Create new message objects depending on the message type
				if(message.getMessage().hasImageMessage()) {
					objects[i] = new WebImageMessage(message);
				} else if(message.getMessage().hasVideoMessage()) {
					objects[i] = new WebVideoMessage(message);
				} else if(message.getMessage().hasConversation() || message.getMessage().hasExtendedTextMessage()) {
					objects[i] = new WebConversationMessage(message);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return objects;
	}
	
	// Convert json message of the type "chat" > WebChat
	private static Object[] chatToObject(JsonArray childrenArray) {
		Object[] objects = new Object[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString() : null;
			int unreadMessages = chatAttributes.get("count").getAsInt();
			long lastInteraction = chatAttributes.get("t").getAsLong();
			boolean muted = chatAttributes.get("mute").getAsBoolean();
			
			objects[i] = new WebChat(jid, name, unreadMessages, lastInteraction, muted);
		}
		return objects;
	}
	
	// Convert json message of the type "contacts" > WebChat
	private static Object[] contactToObject(JsonArray childrenArray) {
		Object[] objects = new Object[childrenArray.size()];
		
		for(int i = 0; i < childrenArray.size(); i++) {
			JsonObject chatAttributes = childrenArray.get(i).getAsJsonArray().get(1).getAsJsonObject();
			String jid = chatAttributes.get("jid").getAsString();
			String name = chatAttributes.keySet().contains("name") ? chatAttributes.get("name").getAsString()
					: chatAttributes.keySet().contains("notify") ? chatAttributes.get("notify").getAsString() : null;
			
			objects[i] = new WebContact(jid, name);
		}
		return objects;
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
