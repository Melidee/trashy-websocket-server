import java.text.ParseException;
import java.util.Objects;

public class HttpRequest extends HttpObject {
    public final String method;
    public final String resource;
    public final String protocol;

    HttpRequest(String method, String resource, String protocol) {
        super();
        this.method = method;
        this.resource = resource;
        this.protocol = protocol;

    }

    public HttpRequest(String rawRequest) throws ParseException {
        super();
        String[] lines;
        int i = 0;
        try {
            lines = rawRequest.split("\\r?\\n");
            String[] components = lines[0].split(" ");
            this.method = components[0];
            this.resource = components[1];
            this.protocol = components[2];
            for (i = 1; i < lines.length-1; i++) {
                String[] header = lines[i].split(": ");
                super.addHeader(header[0], header[1]);
            }
        } catch (IndexOutOfBoundsException e) {
            if (i == 0) {
                throw new ParseException("Failed to parse http request at line: " + i, 0);
            } else {
                throw new ParseException("Failed to parse http request at line: " + i, i);
            }
        }
    }

    @Override
    public String format() {
        return method + " " + resource + " " + protocol + "\r\n" + super.format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return Objects.equals(method, that.method)
                && Objects.equals(resource, that.resource)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(headers, that.headers);
    }
}
