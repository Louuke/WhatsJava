package icu.jnet.whatsjava.messages.stub;

import icu.jnet.whatsjava.encryption.proto.ProtoBuf;
import icu.jnet.whatsjava.encryption.proto.ProtoBuf.WebMessageInfo;

public class WAStubMessage {

    /*
     * Stores messages with a StubType other than 0, which is usually used for informal messages
     *
     */

    private final WebMessageInfo.WEB_MESSAGE_INFO_STUBTYPE stubType;
    private final String[] stubParameterList;

    public WAStubMessage(WebMessageInfo message) {
        this.stubType = message.getMessageStubType();

        stubParameterList = new String[message.getMessageStubParametersCount()];
        for(int i = 0; i < message.getMessageStubParametersCount(); i++) {
            stubParameterList[i] = message.getMessageStubParametersList().get(i);
        }
    }

    public WebMessageInfo.WEB_MESSAGE_INFO_STUBTYPE getStubType() {
        return stubType;
    }

    public String[] getStubParameterList() {
        return stubParameterList;
    }
}
