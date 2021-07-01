package icu.jnet.whatsjava;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.constants.WAFlag;
import icu.jnet.whatsjava.constants.WAMetric;
import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.helper.Utils;
import icu.jnet.whatsjava.messages.WAMessageParser;
import icu.jnet.whatsjava.messages.WAMessageBuilder;
import icu.jnet.whatsjava.messages.web.WebChat;
import icu.jnet.whatsjava.messages.web.WebContact;
import icu.jnet.whatsjava.messages.web.WebMessage;

import javax.imageio.ImageIO;

public class WAClient extends WABackendConnector {

	// Send generic text message
	public void sendMessage(String remoteJid, String messageContent) {
		String request = WAMessageBuilder.generateJson(remoteJid, messageContent, true, Instant.now().getEpochSecond());
		sendBinary(request, new byte[]{WAMetric.message, WAFlag.ignore});
	}
	
	// Delete message only for you
	public void clearMessage(String remoteJid, String messageId, boolean owner) {
		String modTag = String.valueOf(Math.round(Math.random() * 1000000));
		String request = String.format("['action', {epoch: '%s', type: 'set'}, [['chat', {jid: '%s', modify_tag: '%s', type: 'clear'}, [['item', {owner: '%s', index: '%s'}, null]]]]]", Utils.getMessageCount(), remoteJid, modTag, owner, messageId);
		sendBinary(request, new byte[]{WAMetric.group, WAFlag.ignore});
	}

	// Load direct and group chats
	public WebChat[] loadChats() {
		String request = String.format("['query', {type: 'chat', epoch: '%s'}, null]", Utils.getMessageCount());
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.queryChat, WAFlag.ignore}, "\"type\":\"chat\"");
		return (WebChat[]) WAMessageParser.jsonToObjects(jsonMessages);
	}

	// Loads WhatsApp contacts
	public WebContact[] loadContacts() {
		String request = String.format("['query', {type: 'contacts', epoch: '%s'}, null]", Utils.getMessageCount());
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.queryContact, WAFlag.ignore}, "\"type\":\"contacts\"");
		return (WebContact[]) WAMessageParser.jsonToObjects(jsonMessages);
	}

	// Load the last x messages of a direct chat or group
	public WebMessage[] loadChatHistory(String remoteJid, int messageCount) {
		String request = String.format("['query', {type: 'message', epoch: '%s', jid: '%s', kind: 'before', count: '%s'}, null]", Utils.getMessageCount(), remoteJid, messageCount);
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.queryMessages, WAFlag.ignore}, "{\"type\":\"message\"}");
		return (WebMessage[]) WAMessageParser.jsonToObjects(jsonMessages);
	}

	// Load the last x messages of a direct chat or group after the message with the id x2
	public WebMessage[] loadChatHistory(String remoteJid, int messageCount, String lastMessageId, boolean lastOwner) {
		String request = String.format("['query', {type: 'message', epoch: '%s', jid: '%s', kind: 'before', count: '%s', index: '%s', owner: '%s'}, null]", Utils.getMessageCount(), remoteJid, messageCount, lastMessageId, lastOwner);
		String jsonMessages = sendBinary(request, new byte[]{WAMetric.queryMessages, WAFlag.ignore}, "{\"type\":\"message\"}");
		return (WebMessage[]) WAMessageParser.jsonToObjects(jsonMessages);
	}

	public BufferedImage loadChatPicture(String remoteJid) {
		String request = Utils.buildWebSocketJsonRequest(RequestType.QUERY_PROFILE_PICTURE, remoteJid);
		String jsonMessage = sendText(request);
		return jsonMessage.contains("eurl") ? MediaEncryption.convertBytesToImage(Utils.urlToUnencryptedMedia(Utils.encodeValidJson(sendText(request)).get("eurl").getAsString())) : null;
	}
	
	// First binary requests to the backend server
	// Inspired by the original Web implementation
	private void sendPostConnectQueries() {
		sendBinary("[\"query\", {type: \"contacts\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryContact, WAFlag.ignore});
		sendBinary("[\"query\", {type: \"chat\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryChat, WAFlag.ignore});
		sendBinary("[\"query\", {type: \"status\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryStatus, WAFlag.ignore});
		sendBinary("[\"query\", {type: \"quick_reply\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryQuickReply, WAFlag.ignore});
		sendBinary("[\"query\", {type: \"label\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryLabel, WAFlag.ignore});
		sendBinary("[\"query\", {type: \"emoji\", epoch: \"1\"}, null]", new byte[]{WAMetric.queryEmoji, WAFlag.ignore});
		sendBinary("[\"action\", {type: \"set\", epoch: \"1\"}, [[\"presence\", {type: \"available\"}, null]]]", new byte[]{WAMetric.presence, (byte) 160});
	}
}
