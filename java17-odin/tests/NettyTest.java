import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class NettyTest {
    public static void main(String[] a) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1), work = new NioEventLoopGroup(2);
        ServerBootstrap b = new ServerBootstrap().group(boss, work)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(65536),
                        new SimpleChannelInboundHandler<FullHttpRequest>() {
                            protected void channelRead0(ChannelHandlerContext c, FullHttpRequest r) {
                                byte[] body = ("netty " + r.uri()).getBytes(StandardCharsets.UTF_8);
                                FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                    HttpResponseStatus.OK, Unpooled.wrappedBuffer(body));
                                resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length);
                                c.writeAndFlush(resp);
                            }
                        });
                }
            });
        Channel ch = b.bind("127.0.0.1", 0).sync().channel();
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        System.out.println("netty server on " + port);
        ExecutorService ex = Executors.newFixedThreadPool(8);
        int n = 100, ok = 0;
        java.util.List<Future<String>> fs = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            final int k = i;
            fs.add(ex.submit(() -> {
                HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/p" + k).openConnection();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) { return r.readLine(); }
            }));
        }
        for (int i = 0; i < n; i++) if (("netty /p" + i).equals(fs.get(i).get(60, TimeUnit.SECONDS))) ok++;
        System.out.println("requests ok " + ok + "/" + n);
        ex.shutdown();
        ch.close().sync();
        work.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
        boss.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
        System.out.println("NETTY OK");
        System.exit(0);
    }
}
