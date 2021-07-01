package icu.jnet.whatsjava.messages;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf;
import icu.jnet.whatsjava.helper.Utils;
import org.apache.commons.codec.binary.Hex;

import java.util.Base64;

public class WAMessageBuilder {

    // Create a new message object and encode it with base64 later on
    // It gets send to the WhatsApp backend
    public static String generateJson(String remoteJid, String messageContent, boolean fromMe, long timestamp) {
        ProtoBuf.WebMessageInfo message = ProtoBuf.WebMessageInfo.newBuilder()
                .setMessage(ProtoBuf.Message.newBuilder().setExtendedTextMessage(ProtoBuf.ExtendedTextMessage.newBuilder().setText(messageContent).build()))
                .setKey(ProtoBuf.MessageKey.newBuilder().setFromMe(fromMe).setRemoteJid(remoteJid).setId(generateMessageID()).build())
                .setMessageTimestamp(timestamp)
                .setStatus(ProtoBuf.WebMessageInfo.WEB_MESSAGE_INFO_STATUS.PENDING).build();

        String base64Message = Base64.getEncoder().encodeToString(message.toByteArray());
        return String.format("['action', {epoch: '%s', type: 'relay'}, [['message', null, { 'webmessage': '%s'}]]]", Utils.getMessageCount(), base64Message);
    }

    // Generate random byte id to mark a message with it
    private static String generateMessageID() {
        return Hex.encodeHexString(Utils.randomBytes(10), false);
    }
}
