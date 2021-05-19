package icu.jnet.whatsjava.encryption;

public class CurveKeyPair {
	
	/* Stores a Curve25519 key pair or loads it */
	
	private final byte[] privateKey;
	private final byte[] publicKey;
	
	
	public CurveKeyPair(byte[] privateKey, byte[] publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	
	public byte[] getPrivateKey() {
		return privateKey;
	}
	
	public byte[] getPublicKey() {
		return publicKey;
	}
}
