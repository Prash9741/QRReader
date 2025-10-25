import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class QRCodeBackend {

    private static final String SECRET = "shared-secret-123";
    private static final int WINDOW_SIZE = 5; // seconds

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        long lastWindow = -1;

        System.out.println("Press ENTER at any time to stop the program.\n");

        Thread stopper = new Thread(() -> {
            scanner.nextLine();
            System.exit(0); // exit program when key is pressed
        });
        stopper.setDaemon(true); // run in background
        stopper.start();

        while (true) {
            long utcSeconds = Instant.now().getEpochSecond()-5; // UTC+0 seconds and can change the time from here, Prashant
            long window = utcSeconds / WINDOW_SIZE;

            if (window != lastWindow) { // generate new code only when window changes
                String input = SECRET + window;
                String hash = sha256(input);
                String code = hexToCode(hash);

                String humanTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC+0'")
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.ofEpochSecond(utcSeconds));

                System.out.println("Backend UTC+0 Time: " + humanTime);
                System.out.println("Backend Window: " + window);
                System.out.println("Backend Code: " + code);
                System.out.println("-----------------------------------");

                lastWindow = window;
            }

            Thread.sleep(200); // check every 200ms
        }
    }

    private static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hexToCode(String hex) {
        BigInteger num = new BigInteger(hex, 16);
        String base36 = num.toString(36).toUpperCase();
        return base36.substring(0, 6);
    }
}
