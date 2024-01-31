package miniosamap.blob;

import lombok.Builder;
import miniosamap.S3Metadata;

public class FileAndMetadata extends AbstractStuffAndMetadata<String> {

    @Builder
    public FileAndMetadata(String first, S3Metadata second) {
        super(first, second);
    }
}
