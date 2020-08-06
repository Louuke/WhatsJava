package icu.jnet.whatsjava.web;

public class WebChat {

	private String jid;
	private String name;
	
	private int unreadMessages;
	private long lastInteraction;
	private boolean muted;
	
	// Contains variables of a chat
	
	
	public WebChat(String jid, String name, int unreadMessages, long lastInteraction, boolean muted) {
		this.jid = jid;
		this.name = name;
		this.unreadMessages = unreadMessages;
		this.lastInteraction = lastInteraction;
		this.muted = muted;
	}
	
	public String getJid() {
		return jid;
	}
	
	public String getName() {
		return name != null ? name : jid;
	}
	
	public int getUnreadMessages() {
		return unreadMessages;
	}
	
	public long getLastInteraction() {
		return lastInteraction;
	}
	
	public boolean isMuted() {
		return muted;
	}
}
