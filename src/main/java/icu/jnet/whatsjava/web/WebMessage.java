package icu.jnet.whatsjava.web;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS;

class WebMessage {
	
	/*
	 * Gets extended by other "WebTypeMessage" classes
	 * 
	 */
	
	private String remoteJid, id;
	private boolean fromMe;
	private long messageTimestamp;
	private WEB_MESSAGE_INFO_STATUS status;
	
	public WebMessage(WebMessageInfo message) {
		remoteJid = message.getKey().getRemoteJid();
		id = message.getKey().getId();
		fromMe = message.getKey().getFromMe();
		messageTimestamp = message.getMessageTimestamp();
		status = message.getStatus();
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
