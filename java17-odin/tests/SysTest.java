import java.nio.charset.Charset;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class SysTest {
    public static void main(String[] a) throws Exception {
        System.out.println("user.name=" + System.getProperty("user.name") + " user.home=" + System.getProperty("user.home")
                + " user.dir=" + System.getProperty("user.dir"));
        System.out.println("java.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
        System.out.println("os.name=" + System.getProperty("os.name") + " os.version=" + System.getProperty("os.version")
                + " os.arch=" + System.getProperty("os.arch"));
        System.out.println("file.encoding=" + System.getProperty("file.encoding") + " defaultCharset=" + Charset.defaultCharset()
                + " sun.jnu.encoding=" + System.getProperty("sun.jnu.encoding"));
        System.out.println("locale=" + Locale.getDefault() + " display=" + Locale.getDefault().getDisplayName());
        TimeZone tz = TimeZone.getDefault();
        System.out.println("timezone id=" + tz.getID() + " name=" + tz.getDisplayName() + " rawOffset(h)=" + tz.getRawOffset() / 3600000.0
                + " user.timezone=" + System.getProperty("user.timezone"));
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("now=" + now + " instant=" + Instant.now());
        System.out.println("fmt=" + DateTimeFormatter.ofPattern("EEEE d MMMM yyyy HH:mm:ss z").format(now));
        System.out.println("known zones: " + ZoneId.getAvailableZoneIds().size() + " e.g. Europe/Paris offset today: "
                + ZonedDateTime.now(ZoneId.of("Europe/Paris")).getOffset());
        System.out.println("number fmt: " + NumberFormat.getCurrencyInstance().format(1234.5) + " | " + NumberFormat.getInstance().format(1234567.891));
        System.out.println("uuid=" + UUID.randomUUID() + " random=" + new Random().nextInt(100));
        System.out.println("nanoTime ok=" + (System.nanoTime() > 0) + " currentTimeMillis=" + System.currentTimeMillis());
        System.out.println("pid=" + ProcessHandle.current().pid());
        Runtime r = Runtime.getRuntime();
        System.out.println("processors=" + r.availableProcessors() + " free=" + r.freeMemory() / 1048576 + "MB");
        System.out.println("getenv PATH set=" + (System.getenv("PATH") != null));
        long t0 = System.nanoTime();
        Thread.sleep(250);
        long dt = (System.nanoTime() - t0) / 1000000;
        System.out.println("sleep(250) took " + dt + " ms " + (dt >= 245 && dt < 600 ? "OK" : "SUSPECT"));
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest("odin".getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        System.out.println("sha256(odin)=" + sb);
        javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(javax.crypto.Cipher.ENCRYPT_MODE, new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"), new javax.crypto.spec.IvParameterSpec(new byte[16]));
        System.out.println("aes len=" + c.doFinal("hello odin java".getBytes()).length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update("odin".getBytes());
        System.out.println("crc32=" + Long.toHexString(crc.getValue()));
        System.out.println("SYS DONE");
        System.exit(0);
    }
}
