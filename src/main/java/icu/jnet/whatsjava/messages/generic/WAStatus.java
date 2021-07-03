package icu.jnet.whatsjava.messages.generic;

public class WAStatus {

	private final WAMessage message;
	
	public WAStatus(WAMessage message) {
		this.message = message;
	}
	
	public boolean hasImageMessage() {
		return message.hasImageMessage();
	}
	
	public boolean hasVideoMessage() {
		return message.hasVideoMessage();
	}
	
	public WAImageMessage getImageMessage() {
		return message.getImageMessage();
	}
	
	public WAVideoMessage getVideoMessage() {
		return message.getVideoMessage();
	}
}
