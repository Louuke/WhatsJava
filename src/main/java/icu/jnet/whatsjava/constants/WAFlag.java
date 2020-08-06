package icu.jnet.whatsjava.constants;

public class WAFlag {

	public static byte ignore = (byte )(1 << 7),
		    acknowledge = 1 << 6,
		    available = 1 << 5,
		    unavailable = 1 << 4,
		    expires = 1 << 3,
		    skipOffline = 1 << 2;
}
