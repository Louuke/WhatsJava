# WhatsJava
WhatsJava is a reimplementation of the WhatsApp Web API and provides a direct interface for developers.

Big thanks to [@Sigalor](https://github.com/sigalor) and all participants of the [whatsapp-web-reveng project](https://github.com/sigalor/whatsapp-web-reveng) and [@adiwajshing](https://github.com/adiwajshing/Baileys) for the Typescript/Javascript implementation.

### Gradle
```java
allprojects {
    repositories {
	...
        maven { url 'https://jitpack.io' }
    }
}
```
```java
dependencies {
    implementation 'com.github.JicuNull:WhatsJava:v1.0.1'
}
```
Find more options here: **[Jitpack](https://jitpack.io/#JicuNull/WhatsJava)**

## How to use it
**Note**: The project is tested with Java 11

## Connect and login
`WAClient` connects to the WhatsApp backend server and is used for all interactions with the server. Therefore, a new `WAClient` instance needs to be created first.

```java
WAClient client = new WAClient();
```
The session is opened by calling `openConnection()`.
```java
WAClient client = new WAClient();
client.openConnection();
```
You can also define a path to a storage location to store keys needed to reestablish a session. 
```java
client.setCredentialsPath("credentials.json"); // Optional - It defaults to "credentials.json"
```
After a session has been successfully initialized, a qr code must be scanned from the device where WhatsApp is running on. To print a QR code in the console, call the setPrintQRCode() method:
```java
client.setPrintQRCode(true);
```
To learn how to receive the qr code as BufferedImage please refer to "Message handlers".

## Complete example
```java
WAClient client = new WAClient();
client.setPrintQRCode(true);
client.addClientActionListener(new ClientActionListener() {
	@Override
	public void onQRCodeScanRequired(BufferedImage img) {
		System.out.println("Authentication required! Please scan the QR code!");
	}
});
int httpCode = client.openConnection();
if(httpCode == 200) {
	System.out.println("Logged in successfully!");
	System.out.println("You have " + client.loadChats().length + " chats");
} else {
	System.out.println("Login failed! Code: " + httpCode);
}
```

## Message handlers
After a WAClient instance is initialized, the `addClientActionListener` method can be called to register a `ClientActionInterface` and receive a number of callbacks. `ClientActionListener` is an empty implementation of `ClientActionInterface` interface.

- Called when a new qr code has been generated and has to be scanned for login. A qr code has a lifetime of 20 second sand up to 5 five new ones can be created.
```java
client.addClientActionListener(new ClientActionListener() {
    @Override
    public void onQRCodeScanRequired(BufferedImage img) {
        save(img);
    }
    
    // ...
});
```
- Called if the login was successful (Code: 200) or an error occurred.
```java
@Override
public void onReceiveLoginResponse(int httpCode) {
    if(httpCode == 200) {
        System.out.println("Logged in successfully!);
    } else {
        System.out.println("Login failed! Code: " + httpCode);
    }
}
```
- Other callbacks defined in `ClientActionInterface` interface:
```java
@Override
public void onWAMessage(WAMessage[] waMessage) {
    // ...
}

@Override
public void onWAChat(WAChat[] chats) {
    // ...
}
			
@Override
public void onWAContact(WAContact[] contacts) {
    // ...
}
			
@Override
public void onWAEmoji(WAEmoji[] emojis) {
    // ...
}
```
## Sending messages
```java
String remoteJid = "0123456789@s.whatsapp.net";

client.sendMessage(remoteJid, "Hello World");
```
**Note**:
- WhatsApp identifies a person or a group with a unique chat identification called `jid`.
- Chats: `[country code][phone number]@s.whatsapp.net`, for example `490123456789@s.whatsapp.net`
- Groups: `[phone number of group creator]-[timestamp of group creation]@g.us`, e.g. `490123456789-1596766695@g.us`

## Deleting messages
Lets you delete a sent message for yourself
```java
String remoteJid = "0123456789@s.whatsapp.net";
String messageId = "3EM04B5BA7A366D3F9AE";
boolean owner = true;
					
client.clearMessage(remoteJid, messageId, owner);
```

## Loading messages
Queries the chat history of a conversation 
```java
// Query the last 25 messages
WAMessage[] waMessages = client.loadChatHistory(remoteJid, 25);
```
Queries the chat history after a certain message
```java
// Query the last 25 messages after the message with the id x, which I'm not the owner of
WAMessage[] waMessages = client.loadChatHistory(remoteJid, 25, "4C739872E8K15F7L0ACB", false);
```
When you query the messages of a chat, you get an array of WAMessage objects, which in turn can contain different types of message types. To determine which type it is, the methods ```waMessage.hasImageMessage()```, ```waMessage.hasVideoMessage()```, ```waMessage.hasConversationMessage()``` and ```waMessage.hasStubMessage()``` can be used. 
For example:
```java
WAMessage[] waMessages = client.loadChatHistory("490123456789@s.whatsapp.net", 25);
for(WAMessage message : waMessages) {
	if(message.hasConversationMessage()) {
		System.out.println(message.getConversationMessage().getText());
	} else if(message.hasImageMessage()) {
		BufferedImage img = message.getImageMessage().getJpegFullResolution();
		// ...
	}
}
```

## Loading chats
Loads direct and group chats
```java
WAChat[] waChats = client.loadChats();
```

## Loading contacts
Queries your WhatsApp contacts
```java
WAContact[] waContacts = client.loadContacts();
```

## Misc
To get recently used emojis and their relative frequency of use
```java
WAEmoji[] waEmojis = client.loadEmojis();
```
To get the displayed picture of some person or group
```java
BufferedImage img = client.getChatPicture("abc@c.us");
```
To get the status of some person
```java
String status = client.getStatus("abc@c.us");
```
To get someone's presence (if they're typing, online) 
```java
String presence = client.requestPresenceUpdate("abc@c.us");
```
To set your global presence status
```java
client.updatePresence(Presence.AVAILABLE);
// Others: Presence.UNAVAILABLE, Presence.COMPOSING, Presence.RECORDING, Presence.PAUSED
```

## Objects
- **WAChat**:

| Method        | Description   |
| ------------- |---------------|
| getJid        | Returns unique chat identification |
| getName       | Returns your given contact name |
| getUnreadMessages | Returns the amount of unread messages |
| getLastInteraction | Returns the timestamp of the last message |
| isMuted | Returns if the chat is muted |

- **WAContact**:

| Method        | Description   |
| ------------- |---------------|
| getJid        | Returns unique chat identification |
| getName       | Returns the name your contact gave to WhatsApp |

- **WAEmoji**:

| Method        | Description   |
| ------------- |---------------|
| getCode       | Returns emoji code |
| getValue      | Returns the frequency of use |

- **WAConversationMessage**:

| Method        | Description   |
| ------------- |---------------|
| getText | Returns content of the message |
| hasQuotedTextMessage | Returns `true` if it contains a QuotedTextMessage |
| getQuotedTextMessage | Returns QuotedTextMessage |

- **QuotedTextMessage**:

| Method        | Description   |
| ------------- |---------------|
| getText | Returns quote |

- **WAImageMessage**:

| Method        | Description   |
| ------------- |---------------|
| getMimetype | Returns mimetype of the image |
| getCaption | Returns caption |
| getJpegThumbnail | Returns a thumbnail of the image |
| getJpegFullResolution | Returns the image with full resolution or null if it could not be loaded |
| ... | ... |

- **WAVideoMessage**:

| Method        | Description   |
| ------------- |---------------|
| getMimetype | Returns mimetype of the video |
| getSeconds | Returns video length |
| getMp4Thumbnail | Returns a thumbnail of the video |
| getMp4FullResolution | Returns the video with full resolution or null if it could not be loaded |
| ... | ... |

- **WAMessage**:

| Method        | Description   |
| ------------- |---------------|
| getRemoteJid | Returns unique chat identification |
| getId | Returns the id of a message |
| getFromMe | Returns `true` if the message is from you |
| getMessageTimestamp | Returns the message timestamp |
| getStatus | Returns the status of the message |
| getImageMessage |
| getConversationMessage |
| getVideoMessage |
| getStubMessage |
| ... | ... |

**Note**: Audio and document messages are not implemented

## Legal
This code is in no way affiliated with, authorized, maintained, sponsored or endorsed by WhatsApp or any of its affiliates or subsidiaries. This is an independent and unofficial software. Use at your own risk.
