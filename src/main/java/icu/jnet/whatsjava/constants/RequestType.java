package icu.jnet.whatsjava.constants;

public class RequestType {

	/* Identifies the json message type to build using buildWebSocketJsonRequest */
	public static final int LOGIN = 0,
			RESTORE_SESSION = 1,
			SOLVE_CHALLENGE = 2,
			NEW_SERVER_ID = 3,
			QUERY_PROFILE_PICTURE = 4,
			QUERY_GROUP_METADATA = 5,
			QUERY_USER_PRESENCE = 6,
			PRESENCE_UPDATE = 7;
	
	
	/* Identifies the binary message type to build using buildWebSocketBinaryRequest */
	public static final int CONTACTS = 4,
			CHAT = 5,
			STATUS = 6,
			QUICK_REPLY = 7,
			LABEL = 8,
			EMOJI = 9,
			SET = 10;
}
