import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HttpResponse extends HttpObject {
    private static MessageDigest digest = null;
    private final String status;
    private String content;

    public HttpResponse(String status) {
        super();
        this.status = status;
        this.content = "";
    }

    public HttpResponse setContent(String newContent) {
        this.content = newContent;
        headers.put("Content-Length", String.valueOf(content.getBytes().length));
        return this;
    }

    public String getContent() {
        return content;
    }

    public int getContentLength() {
        if (content.equals("")) {
            return 0;
        }
        return content.getBytes().length;
    }

    @Override
    public String format() {
        return "HTTP/1.1 " + status + "\r\n" + super.format();
    }

    public static String genWebsocketAccept(String websocketKey) {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        String combined = websocketKey.strip() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        byte[] accept = digest.digest(combined.getBytes());
        return Base64.getEncoder().encodeToString(accept);
    }
}
