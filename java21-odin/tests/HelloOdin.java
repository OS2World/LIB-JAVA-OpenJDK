import java.util.*;
import java.util.stream.*;

public class HelloOdin {
    record Point(int x, int y) {}

    public static void main(String[] args) throws Exception {
        System.out.println("Hello from Java " + System.getProperty("java.version")
            + " on " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        List<Point> pts = IntStream.range(0, 5).mapToObj(i -> new Point(i, i * i)).collect(Collectors.toList());
        System.out.println(pts);
        System.out.println("sum=" + pts.stream().mapToInt(Point::y).sum());
        var t = new Thread(() -> System.out.println("thread ok: " + Thread.currentThread().getName()));
        t.start();
        t.join();
        Map<String, Integer> m = new TreeMap<>();
        for (String w : "the quick brown fox jumps over the lazy dog the end".split(" ")) m.merge(w, 1, Integer::sum);
        System.out.println(m);
        System.out.println("user.dir=" + System.getProperty("user.dir") + " cpus=" + Runtime.getRuntime().availableProcessors()
            + " maxMem=" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
    }
}
