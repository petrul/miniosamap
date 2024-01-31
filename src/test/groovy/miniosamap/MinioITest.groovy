package miniosamap

import miniosamap.blob.StringAndMetadata
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MinioITest {

    S3Server s3Server
    S3Bucket bucket

    @BeforeEach
    void beforeEach() {
        this.s3Server = S3Server.from("http://localhost:19000", "admin", "s3cr3tk3y")
        this.bucket = s3Server.create("test-" + TestUtil.randomAlphabetic(10))
    }

    @AfterEach
    void afterEach() {
        this.bucket.destroy(true)
    }

    @Test
    void crudBucket() {
        final bucketName = "test-" + TestUtil.randomAlphabetic(10)
        assert ! this.s3Server.has('non-existant' + TestUtil.randomAlphabetic(10))
        assert !this.s3Server.listBucketNames().contains(bucketName)
        assert !this.s3Server.has(bucketName)
        this.s3Server.create(bucketName)
        assert this.s3Server.has(bucketName)
        final b = s3Server[bucketName]
        b['key'] = 'value'
        this.s3Server.destroy(bucketName, true)
        assert !this.s3Server.has(bucketName)
    }

    @Test
    void testListBucket() {
        final s3 = this.s3Server
        final existingBuckets = s3.listBucketNames()
        assert existingBuckets == s3.listBucketNames()

        this.bucket['something here'] = 'foaie verde'
        this.bucket[TestUtil.randomAlphabetic(10)] = 'foaie verde\n' + TestUtil.randomAlphabetic(20)

        final name = "test-" + TestUtil.randomAlphabetic(10)
        final b = s3.create(name)
        assert b.exists()

        def randomAlphabetic = TestUtil.randomAlphabetic(200)
        b['hei'] = randomAlphabetic

        assert b['hei'].inputStream.text == randomAlphabetic

        p b['hei']

        b['second object'] =  'hei there'

        assert b.list().size() == 2
        p b.list()

        b.delete('hei')
        assert b.list().size() == 1
        p b.list()

        b.deleteAllObjects();
        assert b.list().size() == 0
        b.destroy(true);
        assert ! b.exists();
    }

    @Test
    void test2() {
        final s3 = this.s3Server
        p s3.listBucketNames()

        final galeata1Name = this.bucket.name
        final b = s3[galeata1Name]
        assert b.exists()

        final key = "hehehe-" + TestUtil.randomAlphabetic(10)
        b[key] = new StringAndMetadata("content here", [
                'key1': 'val1',
                'key2' : 'val2'
        ])

        p b[key].metadata.entrySet().forEach {p "${it.key} -> ${it.value}"}

        final newKey = "abc/lalala-" + TestUtil.randomAlphabetic(5)
        b.rename(key, newKey)
        assert ! b.has(key)
        assert b.has(newKey)
    }

    @Test
    void testVitrina() {
        final bucketName = "test-" + TestUtil.randomAlphabetic(10)
        final serv = this.s3Server // S3Server.from("http://localhost:19000", "admin", "s3cr3tk3y")
        serv.create(bucketName)
        final bucket = serv[bucketName]
        bucket['hello'] = 'world'
        assert bucket['hello'].inputStream.text == 'world'
        serv.destroy(bucketName, true)
    }

    def p(args) { println(args) }
}
