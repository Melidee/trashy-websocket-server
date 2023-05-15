import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Objects;

/**
 * represents a websocket connection with a client
 */
public class WebSocket extends HttpObject {
    private final DataInputStream input;
    private final OutputStream output;
    private final ServerSocket serverSock;
    private final Socket sock;
    private final LinkedList<String> messages;

    private boolean open;


    public WebSocket() throws IOException { // initialize all the things
        serverSock = new ServerSocket(80);
        sock = serverSock.accept();
        InputStream inStream = sock.getInputStream();
        input = new DataInputStream(inStream);
        output = new DataOutputStream(sock.getOutputStream());
        messages = new LinkedList<>();
        WebSocketInput inputHandler = new WebSocketInput(input, this);
        inputHandler.start();
        String response = handleRequest(inStream);
        System.out.println(response);
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

    // sends a message to the client
    public void send(String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        byte first = (byte) 0b10000001; // final text message
        byte second = (byte) payload.length; // unmasked and up to 125 bytes length
        byte[] frame = new byte[payload.length + 2];
        frame[0] = first;
        frame[1] = second;
        System.arraycopy(payload, 0, frame, 2, payload.length); // append headers and payload
        output.write(frame);
    }

    // close the websocket connection (this isn't always graceful because of platform and os issues, tons of things throw)
    public void close() throws IOException {
        open = false;
        output.write(new byte[] { (byte) 0b10001000, (byte) 0 }); // send a closing message
        sock.close();
        serverSock.close();
    }

    // for internal use only, called if the client sends a closing message
    void closeFromClient() throws IOException {
        open = false;
        sock.close();
        serverSock.close();
    }

    // for internal use only, called when the client sends a message
    void add(String input) {
        messages.add(input);
    }

    // returns the first in line message received, or null if there are no new messages
    public String next() {
        return messages.poll();
    }

    // performs a websocket handshake
    private String handleRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String rawRequest = br.readLine() + "\n"; // read http attributes
        String line = null;
        while (!Objects.equals(line, "")) { // read http headers
            line = br.readLine();
            rawRequest += line + "\n";
        }
        HttpRequest request;
        try { // verify correctness of headers
            request = new HttpRequest(rawRequest);
            if (!Objects.equals(request.headers.get("Connection"), "Upgrade")
                    || Objects.equals(request.headers.get("Upgrade"), "websocket")
                    || Objects.equals(request.headers.get("Sec-WebSocket-Version"), "13")
                    || !request.headers.containsKey("Sec-WebSocket-Key"))
                return new HttpResponse("101 Switching Protocols") // send the response to open the websocket
                        .addHeader("Connection", "Upgrade")
                        .addHeader("Upgrade", "websocket")
                        .addHeader("Sec-WebSocket-Accept",
                                HttpResponse.genWebsocketAccept(request.headers.get("Sec-WebSocket-Key")))
                        .format();

        } catch (ParseException e) { // if we received a bad header
            return new HttpResponse("400 Bad Request")
                    .addHeader("Connection", "close")
                    .format();
        }
        return rawRequest;
    }

    public boolean isOpen() {
        return open;
    }
}