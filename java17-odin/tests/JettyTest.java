import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

public class JettyTest {
    public static void main(String[] a) throws Exception {
        Server s = new Server(0);
        s.setHandler(new AbstractHandler() {
            public void handle(String t, Request br, HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setContentType("text/plain");
                resp.getWriter().print("jetty " + req.getRequestURI());
                br.setHandled(true);
            }
        });
        s.start();
        int port = ((ServerConnector) s.getConnectors()[0]).getLocalPort();
        System.out.println("jetty on " + port);
        int ok = 0;
        for (int i = 0; i < 50; i++) {
            HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/q" + i).openConnection();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                if (("jetty /q" + i).equals(r.readLine())) ok++;
            }
        }
        System.out.println("requests ok " + ok + "/50");
        s.stop();
        System.out.println("JETTY OK");
        System.exit(0);
    }
}
