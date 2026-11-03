import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

public class NioTest {
    static int fails = 0;

    static void check(String name, boolean ok, String extra) {
        System.out.println((ok ? "PASS " : "FAIL ") + name + (extra == null ? "" : " : " + extra));
        if (!ok) fails++;
    }

    interface T { void run() throws Exception; }

    static void t(String name, T body) {
        try {
            body.run();
        } catch (Throwable e) {
            check(name, false, e.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        Path dir = Paths.get("D:/OdinTests/jt/niotmp");
        t("files: create dir tree", () -> {
            Files.createDirectories(dir.resolve("a/b"));
            Files.write(dir.resolve("a/hello.txt"), Arrays.asList("line one", "line two", "unicode \u00fc\u00e9"), StandardCharsets.UTF_8);
            Files.write(dir.resolve("a/b/data.bin"), new byte[]{1, 2, 3, 4, 5});
            check("files: create dir tree", Files.isDirectory(dir.resolve("a/b")), null);
        });
        t("files: read lines", () -> {
            List<String> l = Files.readAllLines(dir.resolve("a/hello.txt"), StandardCharsets.UTF_8);
            check("files: read lines", l.size() == 3 && l.get(2).equals("unicode \u00fc\u00e9"), l.toString());
        });
        t("files: walk", () -> {
            try (Stream<Path> s = Files.walk(dir)) {
                long n = s.count();
                check("files: walk", n >= 5, "entries=" + n);
            }
        });
        t("files: copy/move/delete", () -> {
            Path c = dir.resolve("a/copy.txt");
            Files.copy(dir.resolve("a/hello.txt"), c, StandardCopyOption.REPLACE_EXISTING);
            Files.move(c, dir.resolve("a/moved.txt"), StandardCopyOption.REPLACE_EXISTING);
            boolean ok = Files.exists(dir.resolve("a/moved.txt")) && !Files.exists(c);
            Files.delete(dir.resolve("a/moved.txt"));
            check("files: copy/move/delete", ok && !Files.exists(dir.resolve("a/moved.txt")), null);
        });
        t("files: attributes", () -> {
            var attrs = Files.readAttributes(dir.resolve("a/hello.txt"), java.nio.file.attribute.BasicFileAttributes.class);
            check("files: attributes", attrs.size() > 0 && attrs.isRegularFile() && attrs.lastModifiedTime().toMillis() > 0,
                  "size=" + attrs.size() + " mtime=" + attrs.lastModifiedTime());
        });
        t("filechannel: positional read/write", () -> {
            try (FileChannel fc = FileChannel.open(dir.resolve("chan.dat"), StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                fc.write(ByteBuffer.wrap("0123456789".getBytes()), 0);
                fc.write(ByteBuffer.wrap("ABC".getBytes()), 4);
                ByteBuffer b = ByteBuffer.allocate(10);
                fc.read(b, 0);
                String s = new String(b.array(), 0, 10);
                check("filechannel: positional read/write", s.equals("0123ABC789") && fc.size() == 10, s + " size=" + fc.size());
            }
        });
        t("filechannel: mmap", () -> {
            try (FileChannel fc = FileChannel.open(dir.resolve("map.dat"), StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                MappedByteBuffer mb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 8192);
                for (int i = 0; i < 8192; i++) mb.put(i, (byte) (i * 7));
                mb.force();
                ByteBuffer r = ByteBuffer.allocate(8192);
                fc.read(r, 0);
                boolean ok = true;
                for (int i = 0; i < 8192; i++) if (r.get(i) != (byte) (i * 7)) { ok = false; break; }
                check("filechannel: mmap", ok && fc.size() == 8192, "size=" + fc.size());
            }
        });
        t("filechannel: transferTo", () -> {
            try (FileChannel in = FileChannel.open(dir.resolve("map.dat"));
                 FileChannel out = FileChannel.open(dir.resolve("copy.dat"), StandardOpenOption.CREATE,
                         StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                long n = in.transferTo(0, in.size(), out);
                check("filechannel: transferTo", n == 8192 && Files.size(dir.resolve("copy.dat")) == 8192, "n=" + n);
            }
        });
        t("randomaccessfile + file lock", () -> {
            try (RandomAccessFile raf = new RandomAccessFile(dir.resolve("raf.dat").toFile(), "rw");
                 FileLock lock = raf.getChannel().tryLock()) {
                raf.writeInt(0xCAFEBABE);
                raf.seek(0);
                check("randomaccessfile + file lock", raf.readInt() == 0xCAFEBABE && lock != null && lock.isValid(), null);
            }
        });
        t("selector: nonblocking echo", () -> {
            try (Selector sel = Selector.open();
                 ServerSocketChannel ssc = ServerSocketChannel.open()) {
                ssc.bind(new java.net.InetSocketAddress("127.0.0.1", 0));
                ssc.configureBlocking(false);
                ssc.register(sel, SelectionKey.OP_ACCEPT);
                int port = ((java.net.InetSocketAddress) ssc.getLocalAddress()).getPort();
                Thread client = new Thread(() -> {
                    try (SocketChannel sc = SocketChannel.open(new java.net.InetSocketAddress("127.0.0.1", port))) {
                        sc.write(ByteBuffer.wrap("ping".getBytes()));
                        ByteBuffer b = ByteBuffer.allocate(16);
                        sc.read(b);
                        System.out.println("  client got: " + new String(b.array(), 0, b.position()));
                    } catch (IOException e) {
                        System.out.println("  client error " + e);
                    }
                });
                client.start();
                boolean done = false;
                long end = System.currentTimeMillis() + 15000;
                while (!done && System.currentTimeMillis() < end) {
                    if (sel.select(1000) == 0) continue;
                    for (var it = sel.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey k = it.next();
                        it.remove();
                        if (k.isAcceptable()) {
                            SocketChannel c = ssc.accept();
                            c.configureBlocking(false);
                            c.register(sel, SelectionKey.OP_READ);
                        } else if (k.isReadable()) {
                            SocketChannel c = (SocketChannel) k.channel();
                            ByteBuffer b = ByteBuffer.allocate(16);
                            int n = c.read(b);
                            if (n > 0) {
                                b.flip();
                                c.write(ByteBuffer.wrap(("echo:" + new String(b.array(), 0, b.limit())).getBytes()));
                                done = true;
                                c.close();
                            }
                        }
                    }
                }
                client.join(5000);
                check("selector: nonblocking echo", done, null);
            }
        });
        t("async: AsynchronousFileChannel", () -> {
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(dir.resolve("map.dat"), StandardOpenOption.READ)) {
                ByteBuffer b = ByteBuffer.allocate(16);
                int n = ch.read(b, 100).get(10, TimeUnit.SECONDS);
                check("async: AsynchronousFileChannel", n == 16 && b.get(0) == (byte) (100 * 7), "n=" + n);
            }
        });
        t("async: AsynchronousSocketChannel", () -> {
            try (AsynchronousServerSocketChannel srv = AsynchronousServerSocketChannel.open().bind(new java.net.InetSocketAddress("127.0.0.1", 0))) {
                int port = ((java.net.InetSocketAddress) srv.getLocalAddress()).getPort();
                var acc = srv.accept();
                try (AsynchronousSocketChannel c = AsynchronousSocketChannel.open()) {
                    c.connect(new java.net.InetSocketAddress("127.0.0.1", port)).get(10, TimeUnit.SECONDS);
                    try (AsynchronousSocketChannel s = acc.get(10, TimeUnit.SECONDS)) {
                        c.write(ByteBuffer.wrap("hi".getBytes())).get(10, TimeUnit.SECONDS);
                        ByteBuffer b = ByteBuffer.allocate(8);
                        int n = s.read(b).get(10, TimeUnit.SECONDS);
                        check("async: AsynchronousSocketChannel", n == 2, "n=" + n);
                    }
                }
            }
        });
        System.out.println(fails == 0 ? "NIO ALL PASS" : "NIO FAILURES: " + fails);
        System.exit(0);
    }
}
