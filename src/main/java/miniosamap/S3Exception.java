package miniosamap;

import java.io.IOException;

public class S3Exception extends IOException {
    public S3Exception(Throwable cause) {
        super(cause);
    }

    public S3Exception(String message) {
        super(message);
    }
}
