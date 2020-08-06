package icu.jnet.whatsjava.encryption;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import icu.jnet.whatsjava.constants.BinaryConstants;

public class BinaryEncoder {

	/*
	 * Thanks a lot to @adiwajshing for the Typescript implementation!
	 * https://github.com/adiwajshing/Baileys/
	 * 
	 * This class creates a binary array out of a json string
	 * 
	 * Notice:  & 0xff is used sometimes to create a "unsigned byte"
	 * 
	 */
	
	private List<Byte> data;
	
	
	public byte[] encode(String buffer) {
		data = new ArrayList<Byte>();
		writeNode(buffer);
		
		byte[] dataArray = new byte[data.size()];
		for(int i = 0; i < data.size(); i++) {
			dataArray[i] = data.get(i);
		}
		
		//System.out.println(new String(dataArray));
		
		return dataArray;
	}
	
	private void writeNode(String node) {
		if(node == null || node.equals("")) {
			return;
		} else {
			JsonArray jsonNodeArray = JsonParser.parseString(node).getAsJsonArray();
			
			if(jsonNodeArray.size() != 3) {
				System.err.println("Invalid node: " + node);
			} else {
				JsonObject jsonAttributes = null;
				Set<String> validAttributes = null;
				int jsonAttributesNum = 0;
				
				//String content = null;
				Object children = null;
				
				// Get node data
				if(!jsonNodeArray.get(2).isJsonNull()) {
					if(jsonNodeArray.get(2).isJsonArray()) {
						children = jsonNodeArray.get(2).getAsJsonArray();
					} else if(jsonNodeArray.get(2).isJsonObject()) {
						children = jsonNodeArray.get(2).getAsJsonObject();
					}
				}
				
				// Json attributes as key and value
				if(!jsonNodeArray.get(1).isJsonNull()) {
					jsonAttributes = jsonNodeArray.get(1).getAsJsonObject();
					validAttributes = jsonAttributes.keySet();
					jsonAttributesNum = jsonAttributes.size();
				}
				
				writeListStart(2 * jsonAttributesNum + 1 + (children != null ? 1 : 0));
				writeString(jsonNodeArray.get(0).getAsString(), false);
				
				if(!jsonNodeArray.get(1).isJsonNull()) {
					writeAttributes(jsonAttributes, validAttributes);
				}
				
				// Write children or "nodeContent"
				writeChildren(children);
			}
		}
	}
	
	private void writeChildren(Object children) {
		if(children == null)
			return;
		
		if(children instanceof String) {
			writeString((String) children, true);
		} else if(children instanceof JsonArray) {
			// The node contains a node array
			writeListStart(((JsonArray) children).size());
			
			for(JsonElement element : (JsonArray) children) {
				String node = element.toString();
				if(node != null) {
					writeNode(node);
				}
			}
		} else if(children instanceof JsonObject) {
			// Gets called only if we encode a text message
			// The children object is a WebMessageInfo, which is encoded with base64
			
			String base64Message = ((JsonObject) children).get("webmessage").getAsString();
			byte[] message = Base64.getDecoder().decode(base64Message);
			
			// Convert bytes to int
			int[] intMessage = new int[message.length];
			
			for(int i = 0; i < message.length; i++) {
				intMessage[i] = message[i] & 0xff;
			}
			
			writeByteLength(intMessage.length);
			pushBytes(intMessage);
		}
	}
	
	private void writeAttributes(JsonObject attrs, Set<String> keys) {
		for(String key : keys) {
			writeString(key, false);
			writeString(attrs.get(key).isJsonNull() ? null : attrs.get(key).getAsString(), false);
		}
	}
	
	private void writeByteLength(int length) {
		if(length >= Long.MAX_VALUE) {
			System.err.println("String to large to encode: " + length);
		}
		
		if(length >= (1 << 20)) {
			pushByte(BinaryConstants.Tags.BINARY_32);
			pushInt(length, 4, false);
		} else if(length >= 256) {
			pushByte(BinaryConstants.Tags.BINARY_20);
			pushInt20(length);
		} else {
			pushByte(BinaryConstants.Tags.BINARY_8);
			pushByte(length);
		}
	}
	
	private void writeStringRaw(String str) {
		writeByteLength(str.length());
		pushString(str);
	}
	
	private void writeJid(String left, String right) {
		pushByte(BinaryConstants.Tags.JID_PAIR);
		
		if(left != null && left.length() > 0) {
			writeString(left, false);
		} else {
			writeToken(BinaryConstants.Tags.LIST_EMPTY);
		}
		writeString(right, false);
	}
	
	private void writeListStart(int listSize) {
		if(listSize == 0) {
			pushByte(BinaryConstants.Tags.LIST_EMPTY);
		} else if(listSize < 256) {
			pushBytes(new int[] {BinaryConstants.Tags.LIST_8, listSize});
		} else {
			pushBytes(new int[] {BinaryConstants.Tags.LIST_16, listSize});
		}
	}
	
	private void writeString(String token, boolean i) {
		if(token != null && token.equals("c.us")) {
			token = "s.whatsapp.net";
		}
		
		int tokenIndex = 0;
		for(int t = 0; t < BinaryConstants.singleByteTokens.length; t++) {
			if(BinaryConstants.singleByteTokens[t] == null && token == null) {
				tokenIndex = t;
				break;
			} else if(BinaryConstants.singleByteTokens[t] != null) {
				if(BinaryConstants.singleByteTokens[t].equals(token)) {
					tokenIndex = t;
					break;
				}
			}
		}
		
		if(!i && token != null && token.equals("s.whatsapp.net")) {
			writeToken(tokenIndex);
		} else if(tokenIndex > 0) { // Changed from >= 
			if(tokenIndex < BinaryConstants.Tags.SINGLE_BYTE_MAX) {
				writeToken(tokenIndex);
			} else {
				int overflow = tokenIndex - BinaryConstants.Tags.SINGLE_BYTE_MAX;
				int dictionaryIndex = overflow >> 8;
				if(dictionaryIndex < 0 || dictionaryIndex > 3) {
					System.err.println("Double byte dictionary token out of range: " + token
							+ ", " + tokenIndex);
				}
				writeToken(BinaryConstants.Tags.DICTIONARY_0 + dictionaryIndex);
				writeToken(overflow % 256);
			}
		// Encode jid mobile phone number
		} else if(token != null) {
			int jidSepIndex = token.indexOf('@');
			if(jidSepIndex <= 0) {
				writeStringRaw(token);
			} else {
				writeJid(token.substring(0, jidSepIndex), token.substring(jidSepIndex + 1, token.length()));
			}
		}
	}
	
	private void writeToken(int token) {
		if(token < 245) {
			pushByte(token);
		} else if(token <= 500) {
			System.err.println("Invalid token");
		}
	}
	
	private void pushByte(int value) {
		data.add((byte) (value & 0xff));
	}
	
	private void pushBytes(int[] intArray) {
		for(int i : intArray) {
			data.add((byte) i);
		}
	}
	
	private void pushString(String str) {
		byte[] byteArray = str.getBytes();
		int[] intArray = new int[byteArray.length];
		
		for(int i = 0; i < byteArray.length; i++) {
			intArray[i] = byteArray[i] & 0xff;
		}
		
		pushBytes(intArray);
	}
	
	private void pushInt(int value, int n, boolean littleEndian) {
		for(int i = 0; i < n; n++) {
			int curShift = littleEndian ? i : n - 1 - i;
			data.add((byte) ((value >> (curShift * 8)) & 0xff));
		}
	}
	
	private void pushInt20(int value) {
		pushBytes(new int[] {(value >> 16) & 0x0f, (value >> 8) & 0xff, value & 0xff});
    }
}
