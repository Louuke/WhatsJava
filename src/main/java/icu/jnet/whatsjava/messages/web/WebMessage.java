package icu.jnet.whatsjava.messages.web;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS;

public class WebMessage {
	
	/*
	 * Gets extended by other "WebTypeMessage" classes
	 * 
	 */
	
	private final String remoteJid, id;
	private final boolean fromMe;
	private final long messageTimestamp;
	private final WEB_MESSAGE_INFO_STATUS status;
	
	public WebMessage(WebMessageInfo message) {
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
}
