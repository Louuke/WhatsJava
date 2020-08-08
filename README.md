# WhatsJava
WhatsJava is a reimplementation of the WhatsApp Web API and provides a direct interface for developers.

Big thanks to [@Sigalor](https://github.com/sigalor) and all participants of the [whatsapp-web-reveng project](https://github.com/sigalor/whatsapp-web-reveng) and [@adiwajshing](https://github.com/adiwajshing/Baileys) for the Typescript/Javascript implementation.

This project is **WIP** and things could break in the future.

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
    implementation 'com.github.JicuNull:WhatsJava:v0.1.1-alpha'
}
```
Find more options here: **[Jitpack](https://jitpack.io/#JicuNull/WhatsJava)**

## How to use it
**Note**: The project is tested and compiled with Java 11

### Connect and login
`WAClient` connects to the WhatsApp backend server and is used for all interactions with the server. Therefore, a new `WAClient` instance needs to be created first.

A path to a storage location is also required to store the keys needed to reestablish a session.
```java
WAClient client = new WAClient("auth.json");
```
The session is opened by calling `openConnection()`.
```java
WAClient client = new WAClient("auth.json");
client.openConnection();
```
After a session has been successfully initialized, a qr code must be scanned from the device where WhatsApp is running on. To learn how to receive the qr code please refer to "Message handlers".

### Message handlers
After a WAClient instance is initialized, the `addClientActionListener` method can be called to register a `ClientActionInterface` and receive a number of callbacks. `ClientActionListener` is an empty implementation of `ClientActionInterface` interface.

- Called when a new qr code has been generated and has to be scanned for login. A qr code has a lifetime of 20 seconds, which is why it can be generated up to five times.
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
        System.out.println("Logged in successfully! Code: " + httpCode);
    } else {
        System.out.println("Login failed! Code: " + httpCode);
    }
}
```
- Other callbacks defined in `ClientActionInterface` interface:
```java
@Override
public void onWebChat(WebChat[] chats) {
    // ...
}

@Override
public void onWebContact(WebContact[] contacts) {
    // ...
}
			
@Override
public void onWebEmoji(WebEmoji[] emojis) {
    // ...
}
			
@Override
public void onWebStatus(WebStatus[] status) {
    // ...
}

@Override
public void onWebConversationMessage(WebConversationMessage conversationMessage) {
    // ...
}

@Override
public void onWebImageMessage(WebImageMessage imageMessage) {
    // ...
}

@Override
public void onWebVideoMessage(WebVideoMessage videoMessage) {
    // ...
}
```
### Send text messages
```java
String remoteJid = "0123456789@s.whatsapp.net";

client.sendMessage(remoteJid, "Hello World");
```
**Note**:
- WhatsApp identifies a person or a group with a unique chat identification - `jid`.
- Chats: `[country code][phone number]@s.whatsapp.net`, for example `490123456789@s.whatsapp.net`
- Groups: `[phone number of group creator]-[timestamp of group creation]@g.us`, e.g. `490123456789-1596766695@g.us`

### Load conversation
Queries the chat history of a conversation 
```java
// Query the last 25 messages
client.loadConversation(remoteJid, 25);
```
Queries the chat history after a certain message
```java
// Query the last 25 messages after the message with the id x, which I'm not the owner of
client.loadConversation(remoteJid, 25, "4C739872E8K15F7L0ACB", false);
```

### Objects
- **WebChat**: Contains information about direct chats or groups

| Method        | Description   |
| ------------- |---------------|
| getJid        | Returns unique chat identification |
| getName       | Returns your given contact name |
| getUnreadMessages | Returns the amount of unread messages |
| getLastInteraction | Returns the timestamp of the last message |
| isMuted | Returns if the chat is muted |

- **WebContact**: Contains information about direct chats or groups

| Method        | Description   |
| ------------- |---------------|
| getJid        | Returns unique chat identification |
| getName       | Returns the name your contact gave to WhatsApp |

- **WebEmoji**: Contains information about the emojis you used the most

| Method        | Description   |
| ------------- |---------------|
| getCode       | Returns emoji code |
| getValue      | Returns the frequency of use |

- **WebStatus**: Contains a WebImageMessage or WebVideoMessage

| Method        | Description   |
| ------------- |---------------|
| isWebImageMessage | Returns `true` if it contains a WebImageMessage |
| isWebVideoMessage | Returns `true` if it contains a WebVideoMessage |
| getWebImageMessage | Returns WebImageMessage |
| getWebVideoMessage | Returns WebVideoMessage |

- **WebConversationMessage**: Contains information about generic text messages

| Method        | Description   |
| ------------- |---------------|
| getText | Returns content of the message |
| hasQuotedTextMessage | Returns `true` if it contains a QuotedTextMessage |
| getQuotedTextMessage | Returns QuotedTextMessage |

- **QuotedTextMessage**:

| Method        | Description   |
| ------------- |---------------|
| getText | Returns quote |

- **WebImageMessage**: Contains information about image messages

| Method        | Description   |
| ------------- |---------------|
| getMimetype | Returns mimetype of the image |
| getCaption | Returns caption |
| getJpegThumbnail | Returns a thumbnail of the image |
| ... | ... |

**Note**: Full resolution images are not implemented

- **WebVideoMessage**: Contains information about video messages

| Method        | Description   |
| ------------- |---------------|
| getMimetype | Returns mimetype of the video |
| getSeconds | Returns video length |
| getJpegThumbnail | Returns a thumbnail of the video |
| ... | ... |

**Note**: The download of videos is not implemented

- **WebMessage**: Each Web[Type]Message class extends WebMessage and therefore contains the following methods

| Method        | Description   |
| ------------- |---------------|
| getRemoteJid | Returns unique chat identification |
| getId | Returns the id of a message |
| getFromMe | Returns `true` if the message is from you |
| getMessageTimestamp | Returns the message timestamp |
| getStatus | Returns the status of the message |

## Legal
This code is in no way affiliated with, authorized, maintained, sponsored or endorsed by WhatsApp or any of its affiliates or subsidiaries. This is an independent and unofficial software. Use at your own risk.
