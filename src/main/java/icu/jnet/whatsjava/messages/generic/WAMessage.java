package icu.jnet.whatsjava.messages.generic;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS;
import icu.jnet.whatsjava.messages.stub.WAStubMessage;

public class WAMessage {
	
	/*
	 * Saves messages from direct chat histories and groups
	 * 
	 */
	
	private final String remoteJid, id;
	private final boolean fromMe;
	private final long messageTimestamp;
	private final WEB_MESSAGE_INFO_STATUS status;

	private WAImageMessage imageMessage = null;
	private WAVideoMessage videoMessage = null;
	private WAConversationMessage conversationMessage = null;
	private WAStubMessage stubMessage = null;
	
	public WAMessage(WebMessageInfo message) {
		this.remoteJid = message.getKey().getRemoteJid();
		this.id = message.getKey().getId();
		this.fromMe = message.getKey().getFromMe();
		this.messageTimestamp = message.getMessageTimestamp();
		this.status = message.getStatus();
	}
	
	public String getRemoteJid() {
		return remoteJid;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean getFromMe() {
		return fromMe;
	}
	
	public long getMessageTimestamp() {
		return messageTimestamp;
	}
	
	public WEB_MESSAGE_INFO_STATUS getStatus() {
		return status;
	}

	public WAMessage setImageMessage(WAImageMessage imageMessage) {
		this.imageMessage = imageMessage;
		return this;
	}

	public WAMessage setVideoMessage(WAVideoMessage videoMessage) {
		this.videoMessage = videoMessage;
		return this;
	}

	public WAMessage setConversationMessage(WAConversationMessage conversationMessage) {
		this.conversationMessage = conversationMessage;
		return this;
	}

	public WAMessage setStubMessage(WAStubMessage stubMessage) {
		this.stubMessage = stubMessage;
		return this;
	}

	public WAImageMessage getImageMessage() {
		return imageMessage;
	}

	public WAConversationMessage getConversationMessage() {
		return conversationMessage;
	}

	public WAVideoMessage getVideoMessage() {
		return videoMessage;
	}

	public WAStubMessage getStubMessage() {
		return stubMessage;
	}

	public boolean hasImageMessage() {
		return imageMessage != null;
	}

	public boolean hasVideoMessage() {
		return videoMessage != null;
	}

	public boolean hasConversationMessage() {
		return conversationMessage != null;
	}

	public boolean hasStubMessage() {
		return stubMessage != null;
	}
}
