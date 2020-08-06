package icu.jnet.whatsjava.encryption;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import icu.jnet.whatsjava.constants.BinaryConstants;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

public class BinaryDecoder {

	/*
	 * Thanks a lot to @adiwajshing for the Typescript implementation!
	 * https://github.com/adiwajshing/Baileys/
	 * 
	 * This class creates a human readable json structure out of binary data
	 * 
	 * Notice:  & 0xff is used sometimes to create a "unsigned byte"
	 * 
	 */
	
	private byte[] buffer;
	private int index = 0;
	
	
	public String decode(byte[] buffer) {
		this.buffer = buffer;
		index = 0;
		return readNode();
	}
	
	// Number reformatting
	// https://github.com/sigalor/whatsapp-web-reveng/#number-reformatting
	
	private int unpackNibble(int value) {
		if(value >= 0 && value <= 9) {
			return (int) '0' + value;
		}
		switch(value) {
			case 10:
				return (int) '-';
			case 11:
				return (int) '.';
			case 15:
				return (int) '\0';
		}
		
		System.err.println("Invalid nibble: " + value);
		return 0;
	}
	
	private int unpackHex(int value) {
		if(value >= 0 && value <= 15) {
			return value < 10 ? '0' + value : 'A' + value - 10;
		}
		
		System.err.println("Invalid hex: " + value);
		return 0;
	}
	
	private int unpackByte(int tag, int value) {
		if(tag == BinaryConstants.Tags.NIBBLE_8) {
			return unpackNibble(value);
		} else if(tag == BinaryConstants.Tags.HEX_8) {
			return unpackHex(value);
		} else {
			System.err.println("Unknown tag: " + tag);
		}
		return 0;
	}
	
	// Number formats
	
	private int readInt(int n, boolean littleEndian) {
		checkEOS(n);
		
		int val = 0;
		for(int i = 0; i < n; i++) {
			int shift = littleEndian ? i : n - 1 - i;
			val |= next() << (shift * 8);
		}
		
		return val;
	}
	
	private int readInt20() {
        checkEOS(3);
        
		int a = next() & 0xff;
		int b = next() & 0xff;
		int c = next() & 0xff;
		
        return ((a & 15) << 16) + (b << 8) + c;
    }
	
	private String readPacked8(int tag) {
		byte startByte = readByte();
		
		String value = "";
		
		for(int i = 0; i < (startByte & 127); i++) {
			int curByte = readByte();
			
			int nibbleOne = unpackByte(tag, ((curByte & 0xf0)) >> 4);
			int nibbleSecond = unpackByte(tag, (curByte & 0x0f));
			
			value += String.valueOf(Character.toChars(nibbleOne));
			value += String.valueOf(Character.toChars(nibbleSecond));
		}
		
		if (startByte >> 7 != 0) {
            value = value.substring(0, value.length() - 1);
        }
		return value;
	}
	
	// Helper methods
	
	private byte[] readBytes(int n) {
		checkEOS(n);
		
		byte[] byteArray = Arrays.copyOfRange(buffer, index, index + n);
		index += n;
		return byteArray;
	}
	
	private byte readByte() {
		checkEOS(1);
		return next();
	}
	
	private boolean isListTag(int tag) {
		return tag == BinaryConstants.Tags.LIST_EMPTY || tag == BinaryConstants.Tags.LIST_8
				|| tag == BinaryConstants.Tags.LIST_16;
	}
	
	private int readListSize(int tag) {
		switch(tag) {
			case BinaryConstants.Tags.LIST_EMPTY:
				return 0;
			case BinaryConstants.Tags.LIST_8:
				return readByte();
			case BinaryConstants.Tags.LIST_16:
				return readInt(2, false);
		}
		
		System.err.println("Invalid tag for list size: " + tag);
		return 0;
	}
	
	private String readStringFromCharacters(int length) {
		checkEOS(length);
		
		byte[] value = Arrays.copyOfRange(buffer, index, index + length);
		index += length;
		
		return new String(value);
	}
	
	private String getToken(int index) {
		if(index < 3 || index >= BinaryConstants.singleByteTokens.length) {
			System.err.println("Invalid token index: " + index);
		}
		return BinaryConstants.singleByteTokens[index];
	}
	
	private String getDoubleToken(int a, int b) {
		int n = a * 256 + b;
		
		if(n < 0 || n > BinaryConstants.doubleByteTokens.length) {
			System.err.println("Invalid token index: " + index);
		}
		
		try {
			return BinaryConstants.doubleByteTokens[n];
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	// Strings
	
	private String readString(int tag) {
		if(tag >= 3 && tag <= 235) {
			String token = getToken(tag);
			return token.equals("s.whatsapp.net") ? "c.us" : token;
		}
		
		switch(tag) {
			case BinaryConstants.Tags.DICTIONARY_0:
			case BinaryConstants.Tags.DICTIONARY_1:
			case BinaryConstants.Tags.DICTIONARY_2:
			case BinaryConstants.Tags.DICTIONARY_3:
				return getDoubleToken(tag - BinaryConstants.Tags.DICTIONARY_0, readByte());
			case BinaryConstants.Tags.LIST_EMPTY:
				return null;
			case BinaryConstants.Tags.BINARY_8:
				return readStringFromCharacters(readByte());
			case BinaryConstants.Tags.BINARY_20:
				return readStringFromCharacters(readInt20());
			case BinaryConstants.Tags.BINARY_32:
				return readStringFromCharacters(readInt(4, false));
			case BinaryConstants.Tags.JID_PAIR:
				String i = readString(readByte() & 0xff);
				String j = readString(readByte() & 0xff);
				
				if(i != null && j != null) {
					return i + "@" + j;
				}
				System.err.println("Invalid jid pair: " + i + ", " + j);
			case BinaryConstants.Tags.NIBBLE_8:
			case BinaryConstants.Tags.HEX_8:
				return readPacked8(tag);
			default:
				System.err.println("Invalid tag: " + tag);
		}
		return null;
	}
	
	// Attribute lists
	
	private String readAttributes(int n) {
		if(n != 0) {
			// NodeAttributes
			HashMap<String, String> attributeMap = new HashMap<String, String>();
			
			for(int i = 0; i < n; i++) {
				String key = readString(readByte() & 0xff);
				String value = readString(readByte() & 0xff);
				
				attributeMap.put(key, value);
			}
			return new Gson().toJson(attributeMap);
		}
		return null;
	}
	
	// Nodes
	
	private String readNode() {
		int listSize = readListSize(readByte() & 0xff); // Needs to cast to unsigned byte or int
		int descrTag = readByte() & 0xff;
		
		if(descrTag == BinaryConstants.Tags.STREAM_END) {
			System.err.println("Unexpected stream end");
		}
		
		String descr = readString(descrTag);
		if(listSize == 0 || descr == null) {
			System.err.println("Invalid node");
		}
		
		//System.out.println("List size: " + listSize + " - " + descr);
		
		String attrs = readAttributes((listSize - 1) >> 1);
		//System.out.println(attrs);
		
		
		String[] content = null;
		
		if(listSize % 2 == 0) {
			int tag = readByte() & 0xff; // Needs to cast to unsigned byte or int
			
			if(isListTag(tag)) {
				content = readList(tag);
			} else {
				/*
				 * Handles messages with the message description only
				 * The byte array gets further processed using protobuf
				 * 
				 */
				
				String base64Decoded = "";
				
				try {
					switch(tag) {
						// "message" message
						case BinaryConstants.Tags.BINARY_8:
							byte[] bin8 = readBytes(readByte() & 0xff);
							base64Decoded = Base64.getEncoder().encodeToString(
									WebMessageInfo.parseFrom(bin8).toByteArray());
							break;
						// video & image message
						case BinaryConstants.Tags.BINARY_20:
							byte[] bin20 = readBytes(readInt20());
							
							base64Decoded = Base64.getEncoder().encodeToString(
									WebMessageInfo.parseFrom(bin20).toByteArray());
							break;
						// ?
						case BinaryConstants.Tags.BINARY_32:
							byte[] bin32 = readBytes(readInt(4, false));
							
							base64Decoded = Base64.getEncoder().encodeToString(
									WebMessageInfo.parseFrom(bin32).toByteArray());
						
							break;
						default:
							base64Decoded = readString(tag);
							break;
					}
				
					content = new String[1];
					content[0] = "\"" + base64Decoded + "\"";
				
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
		}
		
		String node = "[\"" + descr + "\", " + attrs + ", " + Arrays.toString(content) + "]";
		
		return node;
	}
	
	// Lists
	
	private String[] readList(int tag) {
		String[] arr = new String[readListSize(tag)];
		//System.out.println("readList: List size: " + arr.length);
		
		for(int i = 0; i < arr.length; i++) {
			arr[i] = readNode();
		}
		return arr;
	}
	
	private void checkEOS(int length) {
		if(index + length > buffer.length) {
			System.err.println("End of stream");
		}
	}
	
	private byte next() {
		return buffer[index++];
	}
}
