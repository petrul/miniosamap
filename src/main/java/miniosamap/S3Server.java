package miniosamap;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor @Builder
public class S3Server {

    @Getter
    MinioClient minioClient;

    public static S3Server from (String url, String accessKey, String secretKey) {
        return new S3Server(MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build());
    }

    public S3Bucket getAt(String bucketName) throws S3Exception {
        final var b = new S3Bucket(this.minioClient, bucketName);
        if  (!b.exists())
            throw new IllegalStateException("no such bucket");
        return b;
    }

    public boolean has(String bucketName) throws S3Exception {
        return this.listBucketNames().contains(bucketName);
    }

    /**
     * use with attenttion
     */
    public void destroy(String bucketName, boolean iUnderstandThatThisIsADangerousOperation) throws S3Exception {
        final S3Bucket bucket = this.getAt(bucketName);
        bucket.destroy(iUnderstandThatThisIsADangerousOperation);
    }

    public S3Bucket create(String bucketName) throws S3Exception {
        final var b = new S3Bucket(this.minioClient, bucketName);
        if (b.exists())
            throw new IllegalStateException("bucket exists");
        b.create();
        return b;
    }

    public java.util.List<String> listBucketNames() {
        try {
            return this.minioClient.listBuckets().stream().map(it -> it.name()).toList();
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public S3Bucket getOrCreate(String name) throws S3Exception {
        if (this.has(name))
            return this.getAt(name);
        else {
            return this.create(name);
        }

    }
}
