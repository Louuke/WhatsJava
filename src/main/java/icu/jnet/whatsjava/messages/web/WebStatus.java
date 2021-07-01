package icu.jnet.whatsjava.messages.web;

public class WebStatus {

	private WebImageMessage imageMessage;
	private WebVideoMessage videoMessage;
	
	public WebStatus(WebImageMessage imageMessage) {
		this.imageMessage = imageMessage;
	}
	
	public WebStatus(WebVideoMessage videoMessage) {
		this.videoMessage = videoMessage;
	}
	
	public boolean isWebImageMessage() {
		return imageMessage != null;
	}
	
	public boolean isWebVideoMessage() {
		return videoMessage != null;
	}
	
	public WebImageMessage getWebImageMessage() {
		return imageMessage;
	}
	
	public WebVideoMessage getWebVideoMessage() {
		return videoMessage;
	}
}
