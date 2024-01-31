package miniosamap;

import org.apache.commons.text.RandomStringGenerator;

import java.util.Random;

public class TestUtil {

    static Random random = new Random();

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String randomAlphabetic(int length) {
        return new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(length);
    }

    public static double getRandomLongitude() {
        double res = 180 * random.nextDouble();
        return res;
    }

}
