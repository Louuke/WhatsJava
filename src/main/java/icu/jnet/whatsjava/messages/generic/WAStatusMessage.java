package icu.jnet.whatsjava.messages.generic;

public class WAStatusMessage {

	private final WAMessage message;
	
	public WAStatusMessage(WAMessage message) {
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
