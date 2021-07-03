package icu.jnet.whatsjava.messages.generic;

public class WAEmoji {

	private final String code;
	private final double value;
	
	public WAEmoji(String code, double value) {
		this.code = code;
		this.value = value;
	}
	
	public String getCode() {
		return code;
	}
	
	public double getValue() {
		return value;
	}
}
