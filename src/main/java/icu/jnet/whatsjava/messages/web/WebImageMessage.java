package icu.jnet.whatsjava.messages.web;

import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.ImageMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class WebImageMessage extends WebMessage {

	/*
	 * E2E media image message
	 * 
	 */

	private final String mimetype, url, caption;
	private final byte[] fileSha256, mediaKey;
	private final long fileLength;
	private final int width, height;
	private final BufferedImage jpegThumbnail, jpegFullResolution;

	public WebImageMessage(WebMessageInfo message) {
		super(message);
		
		ImageMessage imageMessage = message.getMessage().getImageMessage();
		
		this.url = imageMessage.getUrl();
		this.mimetype = imageMessage.getMimetype();
		this.fileSha256 = imageMessage.getFileSha256().toByteArray();
		this.fileLength = imageMessage.getFileLength();
		this.height = imageMessage.getHeight();
		this.width = imageMessage.getWidth();
		this.mediaKey = imageMessage.getMediaKey().toByteArray();
		this.caption = imageMessage.getCaption();

		// Convert byte array to BufferedImage and load full resolution image
		this.jpegThumbnail = MediaEncryption.convertBytesToImage(imageMessage.getJpegThumbnail().toByteArray());
		this.jpegFullResolution = MediaEncryption.convertBytesToImage(MediaEncryption.decrypt(mediaKey, url, MediaEncryption.MEDIA_TYPE_IMAGE));
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
	
	public BufferedImage getJpegThumbnail() {
		return jpegThumbnail;
	}
	
	public BufferedImage getJpegFullResolution() {
		return jpegFullResolution;
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
