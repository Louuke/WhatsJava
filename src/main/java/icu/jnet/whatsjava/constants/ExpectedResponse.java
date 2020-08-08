package icu.jnet.whatsjava.constants;

public class ExpectedResponse {

	// Excepted message type for the next message of the backend
	
	public static final byte LOGIN = 0,
			NEW_SERVER_ID = 1,
			SCAN_QR_CODE = 2,
			RESTORE_SESSION = 3,
			RESOLVE_CHALLENGE = 4,
			LOGGING_OUT = 5,
			MESSAGE_GENERIC = 6;
}
