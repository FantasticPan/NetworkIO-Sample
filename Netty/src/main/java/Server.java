import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by FantasticPan on 2018/4/24.
 */
public class Server {

    private int port;

    public Server(int port) {
        this.port = port;
    }

    /**
     * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
     * Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。在这个例子中我们实现了一个服务端的应用，
     * 因此会有2个NioEventLoopGroup会被使用。
     * 第一个经常被叫做‘boss’，用来接收进来的连接。
     * 第二个经常被叫做‘worker’，用来处理已经被接收的连接， 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
     */
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("准备运行端口：" + port);
        try {
            // ServerBootstrap 是一个启动NIO服务的辅助启动类 你可以在这个服务中直接使用Channel
            ServerBootstrap b = new ServerBootstrap();
            // 这一步是必须的，如果没有设置group将会报java.lang.IllegalStateException: group not set异常
            b.group(bossGroup, workGroup) //绑定俩个线程组
                    // ServerSocketChannel以NIO的selector为基础进行实现的，用来接收新的连接
                    .channel(NioServerSocketChannel.class) //指定NIO的模式
                    /*
                    这里的事件处理类被用来处理一个最近的已经接收的Channel。 ChannelInitializer是一个特殊的处理类，
                    帮助使用者配置一个新的Channel
                    通过增加一些处理类比如NettyServerHandler来配置一个新的Channel，或者其对应的ChannelPipeline来实现你的网络程序。
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024) // 设置tcp缓冲区
                    // option()是提供给NioServerSocketChannel用来接收进来的连接，childOption()是提供给由父管道ServerChannel接收到的连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接
            // 绑定端口并启动去接收进来的连接
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务器开启：" + port);
            // 一直等待，直到socket被关闭
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 将规则跑起来
     */
    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 9090;
        }
        new Server(port).run();
    }
}
