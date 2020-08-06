package icu.jnet.whatsjava.constants;

public class RequestType {

	/* Identifies the json message type to build using buildWebsocketJsonRequest */
	public static final byte LOGIN = 0,
			RESTORE_SESSION = 1,
			SOLVE_CHALLENGE = 2,
			NEW_SERVER_ID = 3;
	
	
	/* Identifies the binary message type to build using buildWebsocketBinaryRequest */
	public static final byte CONTACTS = 4,
			CHAT = 5,
			STATUS = 6,
			QUICK_REPLY = 7,
			LABEL = 8,
			EMOJI = 9,
			SET = 10;
}
