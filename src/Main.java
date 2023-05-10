import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        WebSocket ws = new WebSocket();
        while (true) {
            Thread.sleep(1000);
            ws.send("hello from server");
            String m = ws.next();
            if (m != null) {
                System.out.println(m);
            }
        }
    }
}