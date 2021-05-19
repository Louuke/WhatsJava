package icu.jnet.whatsjava.web;

import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.ImageMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

public class WebImageMessage extends WebMessage {

	/*
	 * E2E media image message
	 * 
	 */

	private final String mimetype, url, caption;
	private final byte[] fileSha256, mediaKey, jpegThumbnail;
	private final long fileLength;
	private final int width, height;

	public WebImageMessage(WebMessageInfo message) {
		super(message);
		
		ImageMessage imageMessage = message.getMessage().getImageMessage();
		
		this.url = imageMessage.getUrl();
		this.mimetype = imageMessage.getMimetype();
		this.fileSha256 = imageMessage.getFileSha256().toByteArray();
		this.fileLength = imageMessage.getFileLength();
		this.height = imageMessage.getHeight();
		this.width = imageMessage.getWidth();
		this.	mediaKey = imageMessage.getMediaKey().toByteArray();
		this.jpegThumbnail = imageMessage.getJpegThumbnail().toByteArray();
		this.caption = imageMessage.getCaption();
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
	
	public byte[] getJpegFullResolution() {
		return MediaEncryption.decrypt(mediaKey, url, MediaEncryption.MEDIA_TYPE_IMAGE);
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
