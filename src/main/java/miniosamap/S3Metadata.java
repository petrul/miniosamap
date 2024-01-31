package miniosamap;

import lombok.Builder;
import okhttp3.Headers;
import org.apache.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * this is what headers look like:
 * Content-Length: 0
 * Server: MinIO
 * Strict-Transport-Security: max-age=31536000; includeSubDomains
 * Vary: Origin
 * Vary: Accept-Encoding
 * X-Amz-Id-2: dd9025bab4ad464b049177c95eb6ebf374d3b3fd1af9251148b658df7ac2e3e8
 * X-Amz-Request-Id: 1772F8C2740F401A
 * X-Content-Type-Options: nosniff
 * X-Minio-Error-Code: NoSuchKey
 * X-Minio-Error-Desc: "The specified key does not exist."
 * X-Xss-Protection: 1; mode=block
 * Date: Tue, 18 Jul 2023 13:18:44 GMT
 */
public class S3Metadata extends HashMap<String, String> {

    // TODO should be immutable
    public static S3Metadata EMPTY = new S3Metadata(0);
    public static String KEY_WIDTH = "width";
    public static String KEY_HEIGHT = "height";

    public S3Metadata(int initialCapacity) {
        super(initialCapacity);
    }

    public S3Metadata() {
        super(3); // 3 = content type + width + height
    }

    @Builder
    public S3Metadata(String contentType, Integer width, Integer height) {
        this(3);
        this.setContentType(contentType);
        this.setWidth(width);
        this.setHeight(height);
    }

    public S3Metadata(Map<String, String> other) {
        super(other.size());
        this.putAll(other);
    }

    public S3Metadata(Headers headers) {
        super(headers.size());
        headers.forEach(it -> {
            final var key = it.getFirst();
            final var value = it.getSecond();
            this.put(key, value);
        });
    }

    public static S3Metadata forContentType(String contentType) {
        final var res = new S3Metadata();
        res.setContentType(contentType);
        return res;
    }

    public String getContentType() {
        return this.get(HttpHeaders.CONTENT_TYPE);
    }

    public S3Metadata setContentType(String contentType) {
        this.put(HttpHeaders.CONTENT_TYPE, contentType);
        return this;
    }
    public void removeContentTypeHeader(){
        this.remove(HttpHeaders.CONTENT_TYPE);
    }
    public S3Metadata setWidth(Integer size) {
        this.put(KEY_WIDTH, size.toString());
        return this;
    }
    public S3Metadata setHeight(Integer size) {
        this.put(KEY_HEIGHT, size.toString());
        return this;
    }

    public Integer getWidth() {
        try {
            final String strWidth = this.get(KEY_WIDTH);
            return Integer.parseInt(strWidth);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer getHeight() {
        try {
            final String strHeight = this.get(KEY_HEIGHT);
            return Integer.parseInt(strHeight);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static S3Metadata from (Headers headers) {
        return new S3Metadata(headers);
    }
}
