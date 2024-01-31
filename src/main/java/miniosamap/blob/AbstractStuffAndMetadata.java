package miniosamap.blob;

import lombok.NoArgsConstructor;
import miniosamap.S3Metadata;

@NoArgsConstructor
public abstract class AbstractStuffAndMetadata<T> extends Pair<T, S3Metadata> {

    public AbstractStuffAndMetadata(T first, S3Metadata metadata) {
        super(first, metadata);
    }

    public T getStuff() {
        return super.first;
    }

    public S3Metadata getMetadata() {
        return super.second;
    }

}
