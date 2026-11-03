import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ServerTest {
    public static void main(String[] args) throws Exception {
        HttpServer srv = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger hits = new AtomicInteger();
        srv.createContext("/hello", ex -> {
            hits.incrementAndGet();
            byte[] body = ("Hello from Java " + System.getProperty("java.version") + " path=" + ex.getRequestURI()).getBytes();
            ex.getResponseHeaders().add("Content-Type", "text/plain");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        });
        srv.createContext("/post", ex -> {
            byte[] in = ex.getRequestBody().readAllBytes();
            byte[] out = new StringBuilder(new String(in)).reverse().toString().getBytes();
            ex.sendResponseHeaders(200, out.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(out);
            }
        });
        ExecutorService pool = Executors.newFixedThreadPool(4);
        srv.setExecutor(pool);
        srv.start();
        int port = srv.getAddress().getPort();
        System.out.println("http server on " + port);

        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/hello?x=1").openConnection();
        System.out.println("GET -> " + c.getResponseCode() + " " + new String(c.getInputStream().readAllBytes()));

        c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/post").openConnection();
        c.setDoOutput(true);
        c.setRequestMethod("POST");
        c.getOutputStream().write("stressed".getBytes());
        System.out.println("POST -> " + c.getResponseCode() + " " + new String(c.getInputStream().readAllBytes()));

        var client = java.net.http.HttpClient.newHttpClient();
        var resp = client.send(java.net.http.HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/hello?via=httpclient")).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        System.out.println("HttpClient -> " + resp.statusCode() + " " + resp.body());

        ExecutorService clients = Executors.newFixedThreadPool(8);
        List<Future<Integer>> fs = new ArrayList<>();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 8; i++) {
            fs.add(clients.submit(() -> {
                int ok = 0;
                for (int j = 0; j < 25; j++) {
                    HttpURLConnection h = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/hello").openConnection();
                    if (h.getResponseCode() == 200) {
                        h.getInputStream().readAllBytes();
                        ok++;
                    }
                }
                return ok;
            }));
        }
        int total = 0;
        for (Future<Integer> f : fs) total += f.get(120, TimeUnit.SECONDS);
        System.out.println("load: " + total + "/200 ok in " + (System.currentTimeMillis() - t0) + " ms, server hits=" + hits.get());

        ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>();
        IntStream.range(0, 100000).parallel().forEach(i -> m.merge(i % 100, 1, Integer::sum));
        System.out.println("parallel stream map: size=" + m.size() + " sum=" + m.values().stream().mapToInt(i -> i).sum());
        System.out.println("fib(27)=" + fib(27));
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "async").thenApply(s -> s + "-done");
        System.out.println("completablefuture: " + cf.get(10, TimeUnit.SECONDS));

        srv.stop(0);
        pool.shutdownNow();
        clients.shutdownNow();
        System.out.println("SERVER OK");
        System.exit(0);
    }

    static long fib(int n) {
        return n < 2 ? n : fib(n - 1) + fib(n - 2);
    }
}
