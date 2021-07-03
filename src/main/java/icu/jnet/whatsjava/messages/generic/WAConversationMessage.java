package icu.jnet.whatsjava.messages.generic;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf.ExtendedTextMessage;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

public class WAConversationMessage {

	private final String text;
	private QuotedTextMessage quotedTextMessage;
	
	public WAConversationMessage(WebMessageInfo message) {
		if(message.getMessage().hasConversation()) {
			text = message.getMessage().getConversation();
		} else {
			// Message with e.g. a quote or link
			ExtendedTextMessage extendedMessage = message.getMessage().getExtendedTextMessage();
			text = extendedMessage.getText();
			
			// Its a quoted message
			if(extendedMessage.hasContextInfo()) {
				/*
				 *  The quote could contain a extended message:
				 *  In this case we ignore all other information and just use getText()
				 *  
				 *  TODO: Might get extended in the future
				 */
				
				icu.jnet.whatsjava.encryption.proto.ProtoBuf.Message quoted = extendedMessage.getContextInfo().getQuotedMessage();
				
				String stanzaId = extendedMessage.getContextInfo().getStanzaId();
				String participant = extendedMessage.getContextInfo().getParticipant();
				String quotedMessage = quoted.hasExtendedTextMessage() ? quoted.getExtendedTextMessage().getText() : quoted.getConversation();
				
				quotedTextMessage = new QuotedTextMessage(stanzaId, participant, quotedMessage);
			}
		}
	}
	
	public String getText() {
		return text;
	}
	
	public boolean hasQuotedTextMessage() {
		return quotedTextMessage != null;
	}

	public QuotedTextMessage getQuotedTextMessage() {
		return quotedTextMessage;
	}
	
	public static class QuotedTextMessage {
	
		private final String stanzaId, participant, quotedMessage;
		
		public QuotedTextMessage(String stanzaId, String participant, String quotedMessage) {
			this.stanzaId = stanzaId;
			this.participant = participant;
			this.quotedMessage = quotedMessage;
		}
		
		public String getStanzaId() {
			return stanzaId;
		}
		
		public String getParticipant() {
			return participant;
		}
		
		public String getText() {
			return quotedMessage;
		}
	}
}
