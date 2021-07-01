package icu.jnet.whatsjava.messages.web;

public class WebContact {

	private final String jid;
	private final String name;
	
	public WebContact(String jid, String name) {
		this.jid = jid;
		this.name = name;
	}
	
	public String getJid() {
		return jid;
	}
	
	public String getName() {
		return name;
	}
}
