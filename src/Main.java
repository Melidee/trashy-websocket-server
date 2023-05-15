import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        WebSocket ws = new WebSocket();
        System.out.println("Connected to client");
        while (true) { // as long as the connection is open, every second send a message to the server and print a message if one has been sent
            if (!ws.isOpen()) {
                return;
            }
            Thread.sleep(1000);
            ws.send("hello from server");
            String m = ws.next();
            if (m != null) {
                System.out.println(m);
            }
        }
    }
}