# miniosamap

Minio's a map. What? 

Import this package into your Groovy project and do:

```groovy
    final serv = S3Server.from("http://localhost:19000", "admin", "s3cr3tk3y")
    serv.create("a-bucket")
    final bucket = serv["a-bucket"]
    bucket['hello'] = 'world'
    assert bucket['hello'].inputStream.text == 'world'
```

See? What did I tell you. Minio's a map.
