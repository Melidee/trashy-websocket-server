import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;

public class HttpResponse {
    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).withZone(ZoneId.of("GMT"));
    private static MessageDigest digest = null;
    private String status;
    private HashMap<String, String> headers;
    private String content;

    public HttpResponse(String status) {
        this.status = status;
        this.headers = new HashMap<>();
        this.content = "";
    }

    public HttpResponse addHeader(String field, String value) {
        headers.put(field, value);
        return this;
    }

    public String getHeader(String key) {
        return headers.get(key);
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

    public String makeResponse() {
        String response = "HTTP/1.1 ";
        response += status + "\r\n";
        headers.put("Date", dtf.format(LocalDateTime.now()));
        for (String key : headers.keySet()) {
            response += key + ": " + headers.get(key) + "\r\n";
        }
        response += "\r\n" + content;
        return response;
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
