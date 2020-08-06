package icu.jnet.whatsjava.web;

public class WebEmoji {

	private String code;
	private double value;
	
	public WebEmoji(String code, double value) {
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
