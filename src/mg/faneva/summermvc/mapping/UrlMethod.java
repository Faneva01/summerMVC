package mg.faneva.summermvc.mapping;

import java.util.Objects;
import java.util.Locale;

public class UrlMethod {

    private String url;
    private String method;

    public UrlMethod(String url, String method) {
        this.url = normalizeUrl(url);
        this.method = method == null
                ? "GET"
                : method.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }

        String normalized = url.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/") && !normalized.isEmpty()) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {

        if(this == obj)
            return true;

        if(obj == null || getClass() != obj.getClass())
            return false;

        UrlMethod other = (UrlMethod) obj;

        return url.equals(other.url)
            && method.equals(other.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}
