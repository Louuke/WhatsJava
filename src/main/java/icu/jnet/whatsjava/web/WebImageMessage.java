package icu.jnet.whatsjava.web;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.ImageMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

public class WebImageMessage extends WebMessage {

	private String mimetype, url, caption;
	private byte[] fileSha256, mediaKey, jpegThumbnail;
	private long fileLength;
	private int width, height;
	
	
	public WebImageMessage(WebMessageInfo message) {
		super(message);
		
		ImageMessage imageMessage = message.getMessage().getImageMessage();
		
		url = imageMessage.getUrl();
		mimetype = imageMessage.getMimetype();
		fileSha256 = imageMessage.getFileSha256().toByteArray();
		fileLength = imageMessage.getFileLength();
		height = imageMessage.getHeight();
		width = imageMessage.getWidth();
		mediaKey = imageMessage.getMediaKey().toByteArray();
		jpegThumbnail = imageMessage.getJpegThumbnail().toByteArray();
		caption = imageMessage.getCaption();
	}
	
	public String getMimetype() {
		return mimetype;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public byte[] getFileSha256() {
		return fileSha256;
	}
	
	public byte[] getMediaKey() {
		return mediaKey;
	}
	
	public byte[] getJpegThumbnail() {
		return jpegThumbnail;
	}
	
	public long getFileLength() {
		return fileLength;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
}
