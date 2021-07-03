package icu.jnet.whatsjava.constants;

public class Presence {

    public static final String AVAILABLE = "available",
            UNAVAILABLE = "unavailable",
            COMPOSING = "composing",
            RECORDING = "recording",
            PAUSED = "paused";

    public static byte getCode(String presence) {
        byte code = WAFlag.UNAVAILABLE;
        switch (presence) {
            case "available":
                code = WAFlag.AVAILABLE;
                break;
            case "unavailable":
                code = WAFlag.UNAVAILABLE;
                break;
            case "composing":
                code = WAFlag.COMPOSING;
                break;
            case "recording":
                code = WAFlag.RECORDING;
                break;
            case "paused":
                code = WAFlag.PAUSED;
                break;
        }
        return code;
    }
}
