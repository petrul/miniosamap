package miniosamap.blob;

import lombok.NoArgsConstructor;
import miniosamap.S3Metadata;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@NoArgsConstructor
public class OutputStreamAndMetadata extends AbstractStuffAndMetadata<OutputStream> {

    public OutputStreamAndMetadata(OutputStream first, S3Metadata second) {
        super(first, second);
    }


    public OutputStream getOutputStream() {
        return super.first;
    }

//    public static class OutputStreamAndMetadataBuilder extends AbstractStuffAndMetadataBuilder {
//        private OutputStream outputStream;
//
//        public OutputStreamAndMetadataBuilder outputStream(OutputStream outputStream) {
//            this.outputStream = outputStream;
//            return this;
//        }
//
//        public OutputStreamAndMetadataBuilder metadata(S3Metadata metadata) {
//            super. = metadata;
//            return this;
//        }
//    }

    public static OutputStreamAndMetadata from(OutputStream output, S3Metadata metadata) {
        return new OutputStreamAndMetadata(output, metadata);
    }
    public static OutputStreamAndMetadata from(ByteArrayOutputStream output) {
        return new OutputStreamAndMetadata(output, S3Metadata.EMPTY);
    }

//    public static OutputStreamAndMetadata from(HttpServletResponse httpServletResponse, S3Metadata meta) throws IOException {
//        return new OutputStreamAndMetadata(
//                httpServletResponse.getOutputStream(),
//                meta);
//    }

}
