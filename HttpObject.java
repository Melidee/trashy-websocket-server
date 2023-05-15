import java.util.HashMap;

public class HttpObject {

    public final HashMap<String, String> headers;

    public HttpObject() {
        this.headers = new HashMap<>();
    }

    public HttpObject addHeader(String key, String val) {
        headers.put(key, val);
        return this;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String format() {
        String out = "";
        for (String key : headers.keySet()) {
            out += key + ": " + headers.get(key) + "\r\n";
        }
        return out + "\r\n";
    }
}
