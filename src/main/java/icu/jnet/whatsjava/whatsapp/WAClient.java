package icu.jnet.whatsjava.whatsapp;

import java.awt.image.BufferedImage;
import java.time.Instant;

import icu.jnet.whatsjava.constants.Presence;
import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.constants.WAFlag;
import icu.jnet.whatsjava.constants.WAMetric;
import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.helper.Utils;
import icu.jnet.whatsjava.messages.WAMessageParser;
import icu.jnet.whatsjava.messages.WAMessageBuilder;
import icu.jnet.whatsjava.messages.generic.WAChat;
import icu.jnet.whatsjava.messages.generic.WAContact;
import icu.jnet.whatsjava.messages.generic.WAEmoji;
import icu.jnet.whatsjava.messages.generic.WAMessage;

public class WAClient extends WABackendConnector {

	// Sends a generic text message
	public void sendMessage(String remoteJid, String messageContent) {
		String reformattedRemoteJid = remoteJid.replace("c.us", "s.whatsapp.net");
		String request = WAMessageBuilder.generateJson(reformattedRemoteJid, messageContent, true, Instant.now().getEpochSecond());
		sendBinary(request, new byte[]{WAMetric.MESSAGE, WAFlag.IGNORE});
	}
	
	// Deletes message only for you
	public void clearMessage(String remoteJid, String messageId, boolean owner) {
		String reformattedRemoteJid = remoteJid.replace("c.us", "s.whatsapp.net");
		String modTag = String.valueOf(Math.round(Math.random() * 1000000));
		String request = String.format("['action', {type: 'set', epoch: '%s'}, [['chat', {jid: '%s', modify_tag: '%s', type: 'clear'}, [['item', {owner: '%s', index: '%s'}, null]]]]]", Utils.getMessageCount(), remoteJid, modTag, owner, messageId);
		sendBinary(request, new byte[]{WAMetric.GROUP, WAFlag.IGNORE});
	}

	// Set your global presence status
	public void updatePresence(String presence) {
		String request = String.format("['action', {type: 'set', epoch: '%s'}, [['presence', {type: '%s'}, null]]]", Utils.getMessageCount(), presence);
		sendBinary(request, new byte[]{WAMetric.PRESENCE, Presence.getCode(presence)});
	}

	// Load direct and group chats
	public WAChat[] loadChats() {
		String request = String.format("['query', {type: 'chat', epoch: '%s'}, null]", Utils.getMessageCount());
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.QUERY_CHAT, WAFlag.IGNORE}, "\"type\":\"chat\"");
		WAChat[] waChats = (WAChat[]) WAMessageParser.jsonToObjects(jsonMessages);
		listener.onWAChat(waChats);
		return waChats;
	}

	// Loads WhatsApp contacts
	public WAContact[] loadContacts() {
		String request = String.format("['query', {type: 'contacts', epoch: '%s'}, null]", Utils.getMessageCount());
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.QUERY_CONTACT, WAFlag.IGNORE}, "\"type\":\"contacts\"");
		WAContact[] waContacts = (WAContact[]) WAMessageParser.jsonToObjects(jsonMessages);
		listener.onWAContact(waContacts);
		return waContacts;
	}

	// Loads used emojis sorted by the frequency of their use
	public WAEmoji[] loadEmojis() {
		String request = String.format("['query', {type: 'emoji', epoch: '%s'}, null]", Utils.getMessageCount());
		String jsonMessage = sendBinary(request, new byte[]{WAMetric.QUERY_STATUS, WAFlag.IGNORE}, "\"type\":\"emoji\"");
		WAEmoji[] emojis = (WAEmoji[]) WAMessageParser.jsonToObjects(jsonMessage);
		listener.onWAEmoji(emojis);
		return emojis;
	}

	// Loads the last x messages of a direct chat or group
	public WAMessage[] loadChatHistory(String remoteJid, int messageCount) {
		String request = String.format("['query', {type: 'message', epoch: '%s', jid: '%s', kind: 'before', count: '%s'}, null]", Utils.getMessageCount(), remoteJid, messageCount - 1);
		String jsonMessage = sendBinary(request, new byte[]{WAMetric.QUERY_MESSAGES, WAFlag.IGNORE}, "{\"type\":\"message\"}");
		WAMessage[] waMessages = (WAMessage[]) WAMessageParser.jsonToObjects(jsonMessage);
		listener.onWAMessage(waMessages);
		return waMessages;
	}

	// Loads the last x messages of a direct chat or group after the message with the id x2
	public WAMessage[] loadChatHistory(String remoteJid, int messageCount, String lastMessageId, boolean lastOwner) {
		String request = String.format("['query', {type: 'message', epoch: '%s', jid: '%s', kind: 'before', count: '%s', index: '%s', owner: '%s'}, null]", Utils.getMessageCount(), remoteJid, messageCount, lastMessageId, lastOwner);
		String jsonMessage = sendBinary(request, new byte[]{WAMetric.QUERY_MESSAGES, WAFlag.IGNORE}, "{\"type\":\"message\"}");
		WAMessage[] waMessages = (WAMessage[]) WAMessageParser.jsonToObjects(jsonMessage);
		listener.onWAMessage(waMessages);
		return waMessages;
	}

	// Loads the profile picture of a person or group
	public BufferedImage getChatPicture(String remoteJid) {
		String request = Utils.buildWebSocketJsonRequest(RequestType.QUERY_PROFILE_PICTURE, remoteJid);
		String jsonMessage = sendText(request);
		return jsonMessage.contains("eurl") ? MediaEncryption.convertBytesToImage(Utils.urlToUnencryptedMedia(Utils.encodeValidJson(jsonMessage).get("eurl").getAsString())) : null;
	}

	// Query the status of a person
	public String getStatus(String remoteJid) {
		String request = Utils.buildWebSocketJsonRequest(RequestType.QUERY_USER_PRESENCE, remoteJid);
		String jsonMessage = sendText(request);
		return Utils.encodeValidJson(jsonMessage).get("status").getAsString();
	}

	// Request an update on the presence of a user
	public String requestPresenceUpdate(String remoteJid) {
		String request = Utils.buildWebSocketJsonRequest(RequestType.PRESENCE_UPDATE, remoteJid);
		String jsonMessage = sendText(request);
		return jsonMessage.contains("type") ? Utils.encodeValidJson(jsonMessage).get("type").getAsString() : Presence.UNAVAILABLE;
	}
}
