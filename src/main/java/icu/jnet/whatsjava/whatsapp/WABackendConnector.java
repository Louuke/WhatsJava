package icu.jnet.whatsjava.whatsapp;

import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.*;
import icu.jnet.whatsjava.constants.RequestType;
import icu.jnet.whatsjava.encryption.BinaryDecoder;
import icu.jnet.whatsjava.encryption.BinaryEncryption;
import icu.jnet.whatsjava.encryption.EncryptionKeyPair;
import icu.jnet.whatsjava.helper.*;
import icu.jnet.whatsjava.listener.ClientActionInterface;
import icu.jnet.whatsjava.listener.ClientActionListener;
import icu.jnet.whatsjava.messages.WAMessageParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class WABackendConnector extends WebSocketAdapter implements PayloadGenerator {

    // WhatsApp WebSocket server
    private final String WHATSAPP_SERVER = "wss://web.whatsapp.com/ws";
    // WhatsApp rejects requests with different origin header
    private final String HEADER_ORIGIN = "https://web.whatsapp.com";
    private final String HEADER_USER_AGENT = "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:78.0) Gecko/20100101 Firefox/78.0";
    // Path to the file where the credentials get stored
    private String credentialsPath = "credentials.json";
    // Stores ids, encryption keys...
    private final AuthCredentials auth;
    // Print QR code in console
    private boolean printQRCode = false;
    // Used as a callback interface to send the login response code or the QR code for logging in
    protected ClientActionInterface listener = new ClientActionListener();
    // WebSocket object that is used for communication with the WhatsApp web server.
    protected WebSocket ws;

    // Last received messages from onTextMessage and onBinaryMessage callback
    private final List<String> textMessageBuffer = new ArrayList<>();

    public WABackendConnector() {
        this.auth = AuthCredentials.loadAuthCredentials(credentialsPath);
    }

    /** Open WebSocket connection with WhatsApp server */
    public int openConnection() {
        try {
            disconnect();

            WebSocketFactory factory = new WebSocketFactory();
            ws = factory.createSocket(WHATSAPP_SERVER);
            ws.addHeader("Origin", HEADER_ORIGIN);
            ws.addHeader("User-Agent", HEADER_USER_AGENT);
            ws.addListener(this); // WebSocketAdapter
            ws.setPingPayloadGenerator(this);
            ws.setPingInterval(15000);
            ws.connect();

            return initOrRestoreSession();
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Close WebSocket */
    public void disconnect() {
        if(ws != null && ws.isOpen()) {
            ws.disconnect();
        }
    }

    /** Set session credential storage path */
    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    /** Enables or disables whether the QR code is displayed in the console */
    public void setPrintQRCode(boolean enabled) {
        this.printQRCode = enabled;
    }

    /** Add ActionListener oder Interface to receive callbacks */
    public void addClientActionListener(ClientActionInterface listener) {
        this.listener = listener;
    }

    /** Initiate a new session or restore one */
    private int initOrRestoreSession() {
        int loginStatus = 0;

        // Random clientId consists of 16 bytes encoded with Base64
        String clientId = auth.getClientId();
        String loginRequest = Utils.buildWebSocketJsonRequest(RequestType.LOGIN, clientId);

        String serverIdMessage = sendText(loginRequest);

        // True if a previous session file was found and can be restored
        if(!auth.mayRestore()) {
            // Request up to 5 new server refs when previous one expires to generate a QR code with it
            for(int i = 0; i < 6; i++) {
                String serverId = Utils.encodeValidJson(serverIdMessage).get("ref").getAsString();
                byte[] curvePublicKey = auth.getCurveKeyPair().getPublicKey();

                // Send BufferedImage to onQRCodeScanRequired callback
                listener.onQRCodeScanRequired(QRGen.generateQRCodeImage(clientId, serverId, curvePublicKey));
                if(printQRCode) System.out.println(QRGen.generateQRCodeConsole(clientId, serverId, curvePublicKey));

                String sessionInfoMsg = waitForTextMessage("Conn");

                // True if the QR code was not scanned
                if(sessionInfoMsg == null) {
                    // Request new ref for QR code generation
                    String newServerIdRequest = Utils.buildWebSocketJsonRequest(RequestType.NEW_SERVER_ID);
                    serverIdMessage = sendText(newServerIdRequest);
                } else {
                    // Trim start and end of the message to convert it into a valid json format
                    JsonObject sessionInfo = Utils.encodeValidJson(sessionInfoMsg, "\"Conn\",");
                    auth.setSessionEncryptionInfo(
                            sessionInfo.get("clientToken").getAsString(),
                            sessionInfo.get("serverToken").getAsString(),
                            sessionInfo.get("secret").getAsString());

                    // Save session info
                    AuthCredentials.saveAuthCredentials(auth, credentialsPath);
                    loginStatus = 200;
                    break;
                }
            }
        } else {
            // A previous session can be restored
            String clientToken = auth.getClientToken();
            String serverToken = auth.getServerToken();

            String restoreRequest = Utils.buildWebSocketJsonRequest(RequestType.RESTORE_SESSION, clientToken, serverToken, clientId);

            String restoreResponse = sendText(restoreRequest, "challenge");

            // If WhatsApp send a challenge request to confirm if we still have the encryption keys
            if(restoreResponse.contains("challenge")) {
                // We need to solve a challenge by signing a byte array correctly
                // Extract base64 challenge
                String challenge = Utils.encodeValidJson(restoreResponse, "\"Cmd\",").get("challenge").getAsString();
                byte[] decodedChallenge = Base64.getDecoder().decode(challenge);

                // Generate encryption / decryption and mac key
                EncryptionKeyPair keyPair = auth.getEncryptionKeyPair();
                byte[] signedChallenge = Utils.signHMAC(keyPair.getMacKey(), decodedChallenge);

                // Sign challenge, encode it with base64 and send it back to the server
                String signedChallengeBase64 = Base64.getEncoder().encodeToString(signedChallenge);
                String challengeRequest = Utils.buildWebSocketJsonRequest(RequestType.SOLVE_CHALLENGE, signedChallengeBase64, serverToken, clientId);

                String challengeResponse = sendText(challengeRequest);
                loginStatus = Utils.encodeValidJson(challengeResponse).get("status").getAsInt();

                // An error occurred during authentication
                // Old session data is invalid so it gets deleted
                if(loginStatus != 200) {
                    AuthCredentials.deletePreviousSession(credentialsPath);
                    disconnect();
                }
            } else {
                loginStatus = Utils.encodeValidJson(restoreResponse).get("status").getAsInt();
            }
        }
        listener.onReceiveLoginResponse(loginStatus);
        return loginStatus;
    }

    /**
     * Send a text frame and wait until a message matching a specified string has been received
     *
     * @param request
     * @param search
     * @return
     */
    String sendText(String request, String... search) {
        String[] searchArray = new String[search.length + 1];
        System.arraycopy(search, 0, searchArray, 0, search.length);
        // Add the message tag to the array of strings that are searched for
        searchArray[searchArray.length - 1] = request.split(",")[0];

        ws.sendText(request);
        return waitForTextMessage(searchArray);
    }

    /**
     * Send a binary frame and wait until a message matching a specified string has been received
     *
     * @param json
     * @param waTags
     * @param search
     */
    String sendBinary(String json, byte[] waTags, String... search) {
        byte[] binaryRequest = Utils.buildWebSocketBinaryRequest(auth.getEncryptionKeyPair(), json, waTags);

        ws.sendBinary(binaryRequest);
        return waitForTextMessage(search);
    }

    /**
     * Waits until a message matching a specified string has been received from the
     * onTextMessage or onBinaryMessage callback and returns it
     *
     * @param search
     * @return
     */
    private String waitForTextMessage(String... search) {
        textMessageBuffer.clear();
        if(search.length > 0) {
            for(int i = 0; i < 200; i++) {
                for(String s : search) {
                    for(String message : textMessageBuffer) {
                        if(message.contains(s)) {
                            return message;
                        }
                    }
                }
                Utils.waitMill(100);
            }
        }
        return null;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        textMessageBuffer.add(message);
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binaryMessage) throws Exception {
        // Decrypt binary message
        byte[] decrypted = BinaryEncryption.decrypt(binaryMessage, auth.getEncryptionKeyPair());

        // Use protobuf to make messages of the type "message" human readable
        String decoded = new BinaryDecoder().decode(decrypted);
        textMessageBuffer.add(decoded);
        WAMessageParser.jsonToObjects(decoded);
    }

    /**
     *  Keeps the WebSocket connection alive by sending a ping every 20-30 seconds
     */
    @Override
    public byte[] generate() {
        return "?,,".getBytes();
    }
}
