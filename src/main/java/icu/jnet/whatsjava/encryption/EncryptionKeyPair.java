package icu.jnet.whatsjava.encryption;

public class EncryptionKeyPair {

	private final byte[] encKey;
	private final byte[] macKey;
	
	public EncryptionKeyPair(byte[] encKey, byte[] macKey) {
		this.encKey = encKey;
		this.macKey = macKey;
	}
	
	public byte[] getEncKey() {
		return encKey;
	}
	
	public byte[] getMacKey() {
		return macKey;
	}
}
