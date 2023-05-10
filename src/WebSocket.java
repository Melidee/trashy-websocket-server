import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Objects;

public class WebSocket {
    private final WebSocketInput input;
    private final DataInputStream inputStream;
    private final OutputStream output;
    private ServerSocket serverSock;
    private Socket sock;
    private LinkedList<String> messages;

    public WebSocket() throws IOException {
        ServerSocket serverSock = new ServerSocket(80);
        Socket sock = serverSock.accept();
        InputStream in = sock.getInputStream();
        inputStream = new DataInputStream(in);
        output = new DataOutputStream(sock.getOutputStream());
        input = new WebSocketInput(inputStream, this);
        input.start();
        messages = new LinkedList<>();
        String response = doHandshake(in);
        System.out.println(response);
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

    public void send(String message) throws IOException {
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        byte first = (byte) 0b10000001;
        byte second = (byte) payload.length;
        byte[] frame = new byte[payload.length + 2];
        frame[0] = first;
        frame[1] = second;
        System.arraycopy(payload, 0, frame, 2, payload.length);
        output.write(frame);
    }

    public void close() throws IOException {
        output.write(new byte[]{(byte) 0b10001000, (byte) 0});
        inputStream.close();
        sock.close();
        serverSock.close();
    }

    void closeFromClient() throws IOException {
        sock.close();
        serverSock.close();
    }

    void add(String input) {
        messages.add(input);
    }

    public String next() {
        return messages.poll();
    }

    private String doHandshake(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String rawRequest = br.readLine() + "\n";
        String line = null;
        while (!Objects.equals(line, "")) {
            line = br.readLine();
            rawRequest += line + "\n";
        }
        System.out.println("```");
        System.out.println(rawRequest);
        System.out.println("```");
        HttpRequest request;
        try {
            request = new HttpRequest(rawRequest);
            if (!Objects.equals(request.headers.get("Connection"), "Upgrade")
                    || Objects.equals(request.headers.get("Upgrade"), "websocket")
                    || Objects.equals(request.headers.get("Sec-WebSocket-Version"), "13")
                    || !request.headers.containsKey("Sec-WebSocket-Key"))
                return new HttpResponse("101 Switching Protocols")
                        .addHeader("Connection", "Upgrade")
                        .addHeader("Upgrade", "websocket")
                        .addHeader("Sec-WebSocket-Accept",
                                HttpResponse.genWebsocketAccept(request.headers.get("Sec-WebSocket-Key")))
                        .makeResponse();

        } catch (ParseException e) {
            return new HttpResponse("400 Bad Request")
                    .addHeader("Connection", "close")
                    .makeResponse();
        }
        return rawRequest;
    }
}
