package icu.jnet.whatsjava.messages.web;

import icu.jnet.whatsjava.encryption.MediaEncryption;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.VideoMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.VideoMessage.VIDEO_MESSAGE_ATTRIBUTION;

public class WebVideoMessage extends WebMessage {

	/*
	 * E2E media video message
	 * 
	 */
	
	private final String mimetype, url;
	private final byte[] fileSha256, mediaKey, jpegMp4Thumbnail, mp4FullResolution;
	private final long fileLength;
	private final int seconds;
	private final VIDEO_MESSAGE_ATTRIBUTION gifAttribution;
	private final boolean gifPlayback;
	
	
	public WebVideoMessage(WebMessageInfo message) {
		super(message);
		
		VideoMessage videoMessage = message.getMessage().getVideoMessage();

		this.url = videoMessage.getUrl();
		this.mimetype = videoMessage.getMimetype();
		this.fileSha256 = videoMessage.getFileSha256().toByteArray();
		this.fileLength = videoMessage.getFileLength();
		this.seconds = videoMessage.getSeconds();
		this.mediaKey = videoMessage.getMediaKey().toByteArray();
		this.gifPlayback = videoMessage.getGifPlayback();
		this.gifAttribution = videoMessage.getGifAttribution();

		this.jpegMp4Thumbnail = videoMessage.getJpegThumbnail().toByteArray();
		this.mp4FullResolution = MediaEncryption.decrypt(mediaKey, url, MediaEncryption.MEDIA_TYPE_VIDEO);
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
		return jpegMp4Thumbnail;
	}
	
	public byte[] getMp4FullResolution() {
		return mp4FullResolution;
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
