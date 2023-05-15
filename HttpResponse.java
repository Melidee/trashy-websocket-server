import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HttpResponse extends HttpObject {
    private static MessageDigest digest = null;
    private final String status;
    private String content;

    public HttpResponse(String status) { // http responses MUST have a status code, this tells the client how their request was interpreted
        super();
        this.status = status;
        this.content = "";
    }

    public HttpResponse setContent(String newContent) { // correctly sets the content length
        this.content = newContent;
        headers.put("Content-Length", String.valueOf(content.getBytes().length));
        return this;
    }

    public String getContent() { // getter
        return content;
    }

    public int getContentLength() { // http requires a content-length header for content, this is not used in this codebase though
        if (content.equals("")) {
            return 0;
        }
        return content.getBytes().length;
    }

    @Override
    public String format() {
        return "HTTP/1.1 " + status + "\r\n" + super.format(); // formats the properties into an http response
    }

    // to generate the websocket accept you must concatenate the client sec websocket key with a magic string, then sha1 hash it
    public static String genWebsocketAccept(String websocketKey) {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {} // never throws
        }
        String combined = websocketKey.strip() + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        byte[] accept = digest.digest(combined.getBytes());
        return Base64.getEncoder().encodeToString(accept);
    }
}
