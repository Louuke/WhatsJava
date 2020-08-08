package icu.jnet.whatsjava.web;

import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.VideoMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.VideoMessage.VIDEO_MESSAGE_ATTRIBUTION;

public class WebVideoMessage extends WebMessage {

	/*
	 * E2E media video message
	 * 
	 */
	
	private String mimetype, url;
	private byte[] fileSha256, mediaKey, jpegThumbnail;
	private long fileLength;
	private int seconds;
	private VIDEO_MESSAGE_ATTRIBUTION gifAttribution;
	private boolean gifPlayback;
	
	
	public WebVideoMessage(WebMessageInfo message) {
		super(message);
		
		VideoMessage videoMessage = message.getMessage().getVideoMessage();
		
		url = videoMessage.getUrl();
		mimetype = videoMessage.getMimetype();
		fileSha256 = videoMessage.getFileSha256().toByteArray();
		fileLength = videoMessage.getFileLength();
		seconds = videoMessage.getSeconds();
		mediaKey = videoMessage.getMediaKey().toByteArray();
		gifPlayback = videoMessage.getGifPlayback();
		jpegThumbnail = videoMessage.getJpegThumbnail().toByteArray();
		gifAttribution = videoMessage.getGifAttribution();
	}
	
	public String getMimetype() {
		return mimetype;
	}
	
	public String getUrl() {
		return url;
	}
	
	public byte[] getFileSha256() {
		return fileSha256;
	}
	
	public byte[] getMediaKey() {
		return mediaKey;
	}
	
	public byte[] getMp4Thumbnail() {
		return jpegThumbnail;
	}
	
	public byte[] getMp4FullResolution() {
		return MediaEncryption.decrypt(mediaKey, url, MediaEncryption.MEDIA_TYPE_VIDEO);
	}
	
	public long getFileLength() {
		return fileLength;
	}
	
	public int getSeconds() {
		return seconds;
	}
	
	public VIDEO_MESSAGE_ATTRIBUTION getGifAttribution() {
		return gifAttribution;
	}
	
	public boolean getGifPlayback() {
		return gifPlayback;
	}
}
