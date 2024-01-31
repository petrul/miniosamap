package miniosamap;

import lombok.extern.java.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class Util {


    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.ofHours(3)));




    public static byte[] sha1(byte[] bytes) {
        try {
            MessageDigest md = null;
            md = MessageDigest.getInstance("SHA-1");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1AsString(byte[] bytes) {
        return byteArray2Hex(sha1(bytes));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static byte[] url2bytes(String url) {
        try {
            return url2bytes(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] url2bytes(URL url) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(url.openStream(), baos);
            baos.close();
            byte[] avatar = baos.toByteArray();
            log.fine("downloaded remote content of size " + avatar.length);
            return avatar;
        } catch (IOException e) {
            throw new RuntimeException((e));
        }
    }

//    public static String storeBase64Image(String base64Avatar, S3BucketBackedContentStore contentStore) throws IOException {
//        final byte[] bytes = Base64.getDecoder().decode(base64Avatar);
//        final String avatarId = contentStore.putRawBytesAsWebp(bytes);
//        return avatarId;
//    }

//    public static boolean validateUsernameOrEmailAddress(String usernameOrEmail) {
//        if (isValidEmailAddress(usernameOrEmail))
//            return true;
//        validateUsername(usernameOrEmail);
//        return true;
//    }

//    public static void validatePassword(String password) {
//        // for anon users, login is possible and password can be null or empty
//        if (nonNullAndNonEmpty(password) && password.length() > 200)
//             RestUtil.throw400("bad password");
//    }

//    public byte[] retrieveAvatarFromRobohash(String username) {
//        try {
//            int set = new Random().nextInt(5) + 1;
//            String url = String.format("%s/%s?set=set%d",
//                    this.robohashServer,
//                    username,
//                    set);
//            url = Util.replaceDoubleSlashes(url);
//            return url2bytes(new URL(url));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static String replaceDoubleSlashes(String url) {
        return url.replaceAll("(?<!(http:|https:))/+", "/");
    }

    public static java.sql.Date nowAsJavaSqlDate() {
        return new java.sql.Date(System.currentTimeMillis());
    }

//    public static RandomStringGenerator newRandomStringGenerator() {
//        RandomStringGenerator generator = new RandomStringGenerator
//                .Builder()
//                .withinRange('a', 'z')
//                .build();
//        return generator;
//    }

//    public static String randomString(int length) {
//        return newRandomStringGenerator().generate(length);
//    }
//
//    static Logger LOG = LoggerFactory.getLogger(Util.class);

//    private static boolean isValidEmailAddressAccordingToJavaxmailPackage(String email) {
//        boolean result = true;
//        try {
//            InternetAddress emailAddr = new InternetAddress(email);
//            emailAddr.validate();
//        } catch (AddressException ex) {
//            result = false;
//        }
//
//        return result;
//    }

    private static boolean doesEmailServerRespond(String email) {
        if (!email.contains("@"))
            return false;
        String[] parts = email.split("@");
        if (parts.length != 2)
            return false;
//        String host = parts[1];

//            try (Socket s = new Socket(SERVER_ADDRESS, TCP_SERVER_PORT)) {
//                return true;
//            } catch (IOException ex) {
//                /* ignore */
//            }
//            return false;
//        }
        return true;
    }

//    public static boolean isValidEmailAddress(String emailAddress) {
//        return !(emailAddress == null
//                || emailAddress.trim().equals("")
//                || !emailAddress.contains("@")
//                || !Util.isValidEmailAddressAccordingToJavaxmailPackage(emailAddress)
//                || !doesEmailServerRespond(emailAddress));
//    }

    public static boolean nonNullAndNonEmpty(String str) {
        return !(nullOrEmpty(str));
    }

    public static boolean nullOrEmpty(String str) {
        return Objects.isNull(str) || str.trim().isEmpty();
    }

//    public static String getUsernameFromJwt(String jwt, String secret) {
//        final String username = Jwts.parser()
//                .setSigningKey(secret)
//                .parseClaimsJws(jwt)
//                .getBody()
//                .getSubject();
//        return username;
//    }

    static final Pattern SPACES = Pattern.compile("[\\s\\t\\n\\r]+");
    static final Pattern NOT_ALLOWED_CHARS = Pattern.compile("[^a-zA-Z0-9_]");

//    public static void validateUsername(String username) {
//        if (username == null || "".equals(username.trim()))
//            RestUtil.throw400("empty username");
//
//        if (username.length() > 2000)
//            RestUtil.throw422("too long");
//
//        if (SPACES.matcher(username).find())
//            RestUtil.throw422("no spaces allowed");
//
//        if (NOT_ALLOWED_CHARS.matcher(username).find())
//            RestUtil.throw422("character not allowed");
//    }

//    public static void validateUserDto(UserDto userDto) {
//        // username constraints
//        validateUsername(userDto.getUsername());
//
//        // check valid email
//        final String emailAddress = userDto.getEmail();
//        if (!Util.isValidEmailAddress(emailAddress)) {
//            RestUtil.throw422("invalid email address");
//        }
//
//        // check valid password
//        if (userDto.getPassword() == null || "".equals(userDto.getPassword().trim())) {
//            RestUtil.throw422("password: empty");
//        }
//    }

    public static boolean isImageContentType(String contentType) {
        if (contentType == null)
            return false;
        if (contentType.startsWith("image/"))
            return true;
        return false;
    }

    public static boolean isVideoContentType(String contentType) {
        if (contentType == null)
            return false;
        if (contentType.startsWith("video/"))
            return true;
        return false;
    }


    public static void assertImageContentType(String contentType) {
        if (!isImageContentType(contentType))
            throw new IllegalArgumentException(String.format("not an image content type: [%s]", contentType));
    }

    public static String contentTypeFromExtension(String onlyExtension) throws IOException {
        return contentTypeFromFilename("a." + onlyExtension);
    }

    public static String contentTypeFromFilename(String filenameWithExtension) throws IOException {
        return Files.probeContentType(Path.of(filenameWithExtension));
    }

    public static String removeDblQuotes(String str) throws S3Exception {
        final var p = Pattern.compile("\"*([^\"]+)\"*");
        final Matcher matcher = p.matcher(str);
        if (!matcher.matches())
            throw new RuntimeException("no double quotes");
        return matcher.group(1);
    }

    public static String guessContentType(InputStream inputStream) throws IOException {
        // check manually for webp first for it is not supported by the jdk
        if (!inputStream.markSupported())
            throw new IllegalArgumentException("mark not supported");

        final var header = new byte[15];
        inputStream.mark(header.length);

        inputStream.read(header);
        if (isWebPHeader(header)) {
            return ContentType.IMAGE_WEBP.getMimeType();
        }
        inputStream.reset();

        // other image types are supported
        return URLConnection.guessContentTypeFromStream(inputStream);
    }

    private static boolean isWebPHeader(byte[] header) {
        assert header.length == 15;
        int i = 0;
        boolean riff = false;
        boolean webpvp8 = false;
        if (
                header[i++] == 'R'
                        && header[i++] == 'I'
                        && header[i++] == 'F'
                        && header[i] == 'F'
        ) {
            riff = true;
        }
        i = 8;
        if (
                header[i++] == 'W'
                        && header[i++] == 'E'
                        && header[i++] == 'B'
                        && header[i++] == 'P'
                        && header[i++] == 'V'
                        && header[i++] == 'P'
                        && header[i] == '8'
        ) {
            webpvp8 = true;
        }

        return (riff && webpvp8);
    }

    public static String guessContentType(byte[] bytes) throws IOException {
        return guessContentType(new ByteArrayInputStream(bytes));
    }

    public static String rootPath(String path) {
        final String withRoot = String.format("/%s", path);
        return replaceDoubleSlashes(withRoot);
    }

    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String md5HexString(byte[] bytes) {
        return
//                DigestUtils.md5DigestAsHex(bytes);
        DigestUtils.md5Hex(bytes);
    }

}
