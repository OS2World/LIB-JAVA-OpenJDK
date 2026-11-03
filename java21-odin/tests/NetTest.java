import java.io.*;
import java.net.*;

public class NetTest {
    public static void main(String[] args) throws Exception {
        System.out.println("localhost -> " + InetAddress.getByName("localhost"));
        System.out.println("127.0.0.1 -> " + InetAddress.getByName("127.0.0.1").getHostName());
        System.out.println("local host -> " + InetAddress.getLocalHost());

        try (ServerSocket ss = new ServerSocket(0, 5, InetAddress.getByName("127.0.0.1"))) {
            int port = ss.getLocalPort();
            System.out.println("server listening on port " + port);
            Thread t = new Thread(() -> {
                try (Socket c = ss.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
                     PrintWriter out = new PrintWriter(c.getOutputStream(), true)) {
                    String line = in.readLine();
                    out.println("echo: " + line);
                } catch (IOException e) {
                    System.out.println("server error: " + e);
                }
            });
            t.start();
            try (Socket s = new Socket("127.0.0.1", port);
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                out.println("hello over loopback");
                System.out.println("client got: " + in.readLine());
            }
            t.join();
        }
        if (args.length > 0) {
            for (String h : args) {
                try {
                    System.out.println(h + " -> " + InetAddress.getByName(h).getHostAddress());
                } catch (Exception e) {
                    System.out.println(h + " lookup failed: " + e);
                }
            }
        }
        System.out.println("NET OK");
    }
}
