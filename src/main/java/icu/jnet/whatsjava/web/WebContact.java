package icu.jnet.whatsjava.web;

public class WebContact {

	private String jid;
	private String name;
	
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
