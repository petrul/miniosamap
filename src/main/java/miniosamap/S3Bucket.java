package miniosamap;


import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import miniosamap.blob.ByteArrAndMetadata;
import miniosamap.blob.InputStreamAndMetadata;
import miniosamap.blob.StringAndMetadata;
import okhttp3.Headers;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;


@Log
@Builder @AllArgsConstructor
public class S3Bucket {

    private static final long PART_SIZE = 5 * 1024 * 1024;

    @NonNull
    MinioClient minioClient;

    @NonNull @Getter
    String name;


    public void putAt(String objName, File file) throws S3Exception {
        try {
        this.minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(this.name)
                        .object(objName)
                        .filename(file.getAbsolutePath())
                        .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }


    /**
     * use this to put some content when dont know what name to give to it.
     * the name will be the hash.
     *
     * Content will not be stored twice.
     *
     * @return id is the md5 hash
     */
    public String put(InputStreamAndMetadata inputStreamAndMeta) throws S3Exception {
        DigestInputStream dis;
        try {
            dis = new DigestInputStream(inputStreamAndMeta.getInputStream(),
                    MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final String tmpName = UUID.randomUUID().toString();


        final String etag = this.putAt(tmpName,
                new InputStreamAndMetadata(
                        dis,
                        inputStreamAndMeta.getMetadata()));

        final byte[] digest = dis.getMessageDigest().digest();
        final var md5 = Hex.encodeHexString(digest);
        if (this.has(md5)) {
            // replace the old object
            // NB (the new metadata may not exist on the old object even if content is the same)
           this.delete(md5);
        }
        this.rename(tmpName, md5);

        if (!etag.equals(md5))
            log.fine(String.format("etag different from md5 %s <-> %s", etag,  md5));

        return md5;
    }



    /**
     * @return minio etag which for some reason does not seem to be the actual
     * md5 of the content at creation time, at least.
     */
    public String putAt(String objName, InputStreamAndMetadata inputStreamAndMeta) throws S3Exception {
        try {
            final var inputStream = inputStreamAndMeta.getFirst();
            try (inputStream) { // try with resource
                final var meta = new S3Metadata(inputStreamAndMeta.getSecond());

                assert inputStream != null;
                assert meta != null;

                String contentType = meta.getContentType();
                if (contentType == null) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM.getMimeType();
                }
                meta.removeContentTypeHeader();

                final var resp = this.minioClient.putObject(PutObjectArgs.builder()
                        .bucket(this.name)
                        .object(objName)
                        .stream(inputStream, -1, PART_SIZE)
                        .contentType(contentType)
                        .userMetadata(meta)
                        .build());
                return Util.removeDblQuotes(resp.etag());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public void putAt(String objName, ByteArrAndMetadata obj) throws S3Exception {
        this.putAt(objName, new InputStreamAndMetadata(
                new ByteArrayInputStream(obj.getByteArray()),
                obj.getMetadata()
        ));
    }

    public void putAt(String objName, StringAndMetadata obj) throws S3Exception {
        this.putAt(objName, new ByteArrAndMetadata(
                obj.getFirst().getBytes(StandardCharsets.UTF_8),
                obj.getSecond()));
    }

    public void putAt(String objName, String str) throws S3Exception {
        this.putAt(objName, new StringAndMetadata(str,
                S3Metadata.EMPTY.setContentType(ContentType.TEXT_PLAIN.getMimeType())));
    }

     GetObjectResponse getAtNative(String objName) throws S3Exception {
        try {
            return this.minioClient.getObject(GetObjectArgs.builder()
                    .bucket(this.name)
                    .object(objName)

                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                    IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
                throw new S3Exception(e);
            }
     }

    public InputStreamAndMetadata getAt(String objName) throws S3Exception {
        final var resp = this.getAtNative(objName);

        final var prefix = "x-amz-meta-";
        final Headers headers = resp.headers();
        final var metadata = new S3Metadata(5);
        metadata.setContentType(headers.get(HttpHeaders.CONTENT_TYPE));
        headers.names().stream()
                .filter(it -> it.startsWith(prefix))
                .forEach(key -> {
                    final var suffix = key.substring(prefix.length());
                    metadata.put(suffix, headers.get(key));
                });
        return new InputStreamAndMetadata(resp, metadata);
    }

    public boolean exists() throws S3Exception {
        try {
            return this.minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(this.name).build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public List<String> list() {
            final Iterable<Result<Item>> results = this.minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(this.name)
                    .recursive(true)
                    .build());
            return StreamSupport.stream(results.spliterator(), false)
                    .map(it -> {
                        try {
                            return it.get().objectName();
                        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
    }

    public void delete(String objName) throws S3Exception {
        try {
            this.minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(this.name)
                            .object(objName)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public void copy(String sourceName, String targetName) throws S3Exception {
        try {
            this.minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(this.name)
                    .source(CopySource.builder()
                            .bucket(this.name)
                            .object(sourceName)
                            .build())
                    .object(targetName)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public void rename(String sourceName, String targetName) throws S3Exception {
        this.copy(sourceName, targetName);
        this.delete(sourceName);
    }

    public void deleteAllObjects() throws S3Exception {
        for (var it : this.list()) {
            this.delete(it);
        }
//        this.list().forEach();
//        final List<DeleteObject> list = this.list().stream().map(it -> new DeleteObject(it)).toList();
////        try {
//
//        Iterable<Result<DeleteError>> removedObjects = this.minioClient.removeObjects(RemoveObjectsArgs.builder()
//                .bucket(this.name)
//                .objects(list)
//                .build());
        System.out.println(this.list());
        assert this.list().isEmpty();
//        StreamSupport.stream(removedObjects.spliterator(), false).map(it -> it.get().message())
//        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
//                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }

    }

    public void destroy(boolean iUnderstandThatThisIsADangerousOperation) throws S3Exception {
        if (!iUnderstandThatThisIsADangerousOperation)
            throw new RuntimeException("this is a pontetially dangerous operation");

//        final List<DeleteObject> list = this.list().stream().map(it -> new DeleteObject(it)).toList();
        try {
            this.deleteAllObjects();

            this.minioClient.removeBucket(RemoveBucketArgs.builder()
                            .bucket(this.name)
                            .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public void create() throws S3Exception {
        try {
            this.minioClient.makeBucket (io.minio.MakeBucketArgs.builder().bucket(this.name).build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }

    public boolean has(String key) throws S3Exception {
        try {
        this.minioClient.statObject(StatObjectArgs.builder()
                .bucket(this.name)
                .object(key)
                .build());
        return true;
        } catch ( InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code()))
                return false;
            throw new S3Exception(e);
        }
    }

    public StatObjectResponse stat(String objId) throws S3Exception {
        try {
            return this.minioClient.statObject(StatObjectArgs.builder()
                    .bucket(this.name)
                    .object(objId)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new S3Exception(e);
        }
    }
}
