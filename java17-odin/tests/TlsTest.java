import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;

/** Local TLS handshake with a generated self-signed key (uses keytool's keystore in args[0]). */
public class TlsTest {
    public static void main(String[] args) throws Exception {
        String ks = args.length > 0 ? args[0] : "D:/OdinTests/jt/test.jks";
        char[] pw = "changeit".toCharArray();
        KeyStore store = KeyStore.getInstance(new File(ks), pw);
        System.out.println("keystore entries: " + Collections.list(store.aliases()));
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(store, pw);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(store);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        System.out.println("protocols: " + Arrays.toString(ctx.getSupportedSSLParameters().getProtocols()));

        SSLServerSocket srv = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0, 5, InetAddress.getByName("127.0.0.1"));
        int port = srv.getLocalPort();
        Thread t = new Thread(() -> {
            try (SSLSocket s = (SSLSocket) srv.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
                out.println("secure echo: " + in.readLine());
            } catch (Exception e) {
                System.out.println("server error: " + e);
            }
        });
        t.start();
        try (SSLSocket c = (SSLSocket) ctx.getSocketFactory().createSocket("127.0.0.1", port)) {
            c.startHandshake();
            SSLSession ses = c.getSession();
            System.out.println("negotiated " + ses.getProtocol() + " " + ses.getCipherSuite());
            System.out.println("peer cert: " + ((X509Certificate) ses.getPeerCertificates()[0]).getSubjectX500Principal());
            PrintWriter out = new PrintWriter(c.getOutputStream(), true);
            out.println("hello over tls");
            System.out.println("client got: " + new BufferedReader(new InputStreamReader(c.getInputStream())).readLine());
        }
        t.join(10000);
        srv.close();
        System.out.println("TLS OK");
        System.exit(0);
    }
}
