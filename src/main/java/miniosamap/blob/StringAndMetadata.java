package miniosamap.blob;

import miniosamap.S3Metadata;

import java.util.Map;

public class StringAndMetadata extends AbstractStuffAndMetadata<String> {

    public StringAndMetadata(String first, S3Metadata second) {
        super(first, second);
    }

    public StringAndMetadata(String first, Map<String, String> second) {
        super(first, new S3Metadata(second));
    }
}
