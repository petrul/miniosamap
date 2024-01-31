package miniosamap.blob;

import miniosamap.S3Metadata;
import miniosamap.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class ByteArrAndMetadata extends Pair<byte[], S3Metadata> {

    public ByteArrAndMetadata(byte[] bytes, S3Metadata metadata) {
        super(bytes, metadata);
    }

    public byte[] getByteArray() {
        return super.first;
    }

    public void setByteArray(byte[] arr) {
        super.setFirst(arr);
    }


    public S3Metadata getMetadata() {
        return super.second;
    }
    public void setMetadata(S3Metadata meta) {
        super.setSecond(meta);
    }

    public InputStreamAndMetadata toInputStreamAndMetadata() {
        return new InputStreamAndMetadata(
                new ByteArrayInputStream(this.getByteArray()),
                this.getMetadata());
    }

    public static ByteArrAndMetadata from(byte[] bytes) throws IOException {
        final var meta = new S3Metadata();
        final String contentType = Util.guessContentType(bytes);
        meta.setContentType(contentType);
        if (Util.isImageContentType(contentType)) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            meta.setWidth(image.getWidth());
            meta.setHeight(image.getHeight());
        }
        return new ByteArrAndMetadata(bytes, meta);
    }

}
