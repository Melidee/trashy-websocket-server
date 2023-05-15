import java.util.HashMap;

public class HttpObject {

    public final HashMap<String, String> headers; // http headers are common between request and response

    public HttpObject() { // generic constructor
        this.headers = new HashMap<>();
    }

    public HttpObject addHeader(String key, String val) { // mutator
        headers.put(key, val);
        return this;
    }

    public String getHeader(String key) { // accessor
        return headers.get(key);
    }

    public String format() { // format the headers, needs to be extended to create a valid request
        String out = "";
        for (String key : headers.keySet()) {
            out += key + ": " + headers.get(key) + "\r\n";
        }
        return out + "\r\n";
    }
}
