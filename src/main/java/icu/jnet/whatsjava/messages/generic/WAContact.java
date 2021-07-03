package icu.jnet.whatsjava.messages.generic;

public class WAContact {

	private final String jid;
	private final String name;
	
	public WAContact(String jid, String name) {
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
