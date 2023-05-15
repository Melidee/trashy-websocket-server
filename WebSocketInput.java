import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class WebSocketInput extends Thread {
    private DataInputStream inputStream;
    private WebSocket ws;
    private boolean open;
    WebSocketInput(DataInputStream inputStream, WebSocket ws) {
        super();
        this.inputStream = inputStream;
        this.ws = ws;
    }

    private void inputLoop() {
        try {
            /*
             * websocket messages are in this format:
             * the first bit of the first byte says if the message is final
             * the next three bits of the first byte are reserved and can be ignored
             * the last 4 bits of the first byte say if the message is final or not
             *
             * the first bit of the second byte says if the message is masked:
             *      masking is a simple XOR encryption that makes it harder for attackers to insert fake messages
             *      ALL messages sent from the client to the server MUST be masked
             *      ALL messages sent from the server to the client MUST be unmasked
             * the next seven bits are the _length_ of the payload
             *      if _length_ == 126 (or 0b01111110) the length is contained in the next 2 bytes
             *      if _length_ == 127 (or 0b01111111) the length is contained in tge next 8 bytes
             *          this server does not support 8 byte length prefix, as it's larger than a Java LONG primitive
             *
             * the next 4 bytes are the mask key, if the message is masked
             *      these must be randomly generated by the client and are used to decode the message
             *
             * after that there are a number of bytes equal to _length_, this is the client message
             *      for a text format message these bytes are UTF-8 encoded
             */
            byte first = inputStream.readByte();
            byte second = inputStream.readByte();
            boolean fin = (first & 0b10000000) > 0; // should be true
            boolean masked = (second & 0b10000000) > 0; // should be true
            int opcode = first & 0b00001111;
            int length = second & 0b01111111;
            if (opcode == 0x8) { // if we received a close message we need to end the connection
                try { // we need to catch the case where the client tries to send a close message as the host is closing
                    ws.closeFromClient();
                } catch (NullPointerException e) {
                    return;
                }
                return;
            }
            if (length == 126) { // if we have a 2 byte length prefix
                length = (int) (inputStream.readByte() << 8 + inputStream.readByte());
            }
            // message must be fin, masked, in text format, and not use an 8 byte length prefix
            if (!fin || !masked || opcode != 0x1 || length == 127) {
                ws.closeFromClient();
                return;
            }
            byte[] maskKey = { // the key for the encryption
                    inputStream.readByte(),
                    inputStream.readByte(),
                    inputStream.readByte(),
                    inputStream.readByte(),
            };
            byte[] payload = unpackPayload(length, maskKey);
            String decoded = new String(payload, StandardCharsets.UTF_8);
            ws.add(decoded);
            inputLoop();
        } catch (IOException e) {
            return;
        }
    }

    private byte[] unpackPayload(int length, byte[] maskKey) throws IOException {
        byte[] payload = new byte[length]; // where we are storing the message
        for (int i = 0; i < length; i++) {
            // bytes are read and then XORed with their respective part of the mask key
            payload[i] = (byte) (inputStream.readByte() ^ maskKey[i % 4]);
        }
        return payload;
    }

    @Override
    public void run() {
        inputLoop();
    }
}