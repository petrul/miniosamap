package miniosamap.blob;

import lombok.extern.java.Log;
import miniosamap.S3Metadata;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Log
public class InputStreamAndMetadata extends AbstractStuffAndMetadata<InputStream>  implements Closeable {

    public InputStreamAndMetadata(InputStream inputStream) {
        this(inputStream, S3Metadata.EMPTY);
    }
    public InputStreamAndMetadata(InputStream inputStream, S3Metadata metadata) {
        super(inputStream, metadata);
    }

    public InputStream getInputStream() {
        return super.first;
    }

    public static InputStreamAndMetadata from(InputStream inputStream, S3Metadata metadata) {
        return new InputStreamAndMetadata(inputStream, metadata);
    }

    public static InputStreamAndMetadata from(ByteArrAndMetadata byteArrAndMetadata) {
        return new InputStreamAndMetadata(new ByteArrayInputStream(byteArrAndMetadata.getByteArray()),
                byteArrAndMetadata.getMetadata());
    }

    public static InputStreamAndMetadata from(InputStream inputStream) {
        return new InputStreamAndMetadata(inputStream);
    }

//    public static InputStreamAndMetadata from(MultipartFile file) throws IOException {
//        final String contentType = file.getContentType();
//        assert Util.nonNullAndNonEmpty(contentType);
//        final var meta = new S3Metadata();
//        meta.setContentType(contentType);
//        if (Util.isImageContentType(contentType)) {
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            meta.setWidth(image.getWidth());
//            meta.setHeight(image.getHeight());
//        }
//        return new InputStreamAndMetadata(file.getInputStream(), meta);
//    }

//    public static InputStreamAndMetadata from(MultipartFile file, String contentType) throws IOException {
//        final InputStreamAndMetadata isam = from(file);
//        if (contentType.equals(isam.getMetadata().getContentType())) {
//            log.warn(String.format(
//                    "content type does not correspond to data [%s]",
//                    contentType
//                    ));
//        }
//        return isam;
//    }

    public static InputStreamAndMetadata from(URL url) throws IOException {
        final var mimetype = URLConnection.getFileNameMap().getContentTypeFor(url.toString());
        final var meta = new S3Metadata();
        meta.setContentType(mimetype);
        if (mimetype.startsWith("image/")) {
            final var image = ImageIO.read(url.openStream());
            final var w = image.getWidth();
            final var h = image.getHeight();
            meta.setWidth(w);
            meta.setHeight(h);
        }
        return new InputStreamAndMetadata(url.openStream(), meta);
    }

    @Override
    public void close() throws IOException {
        InputStream inputStream = this.getInputStream();
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
