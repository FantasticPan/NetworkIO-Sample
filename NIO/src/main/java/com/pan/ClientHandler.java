package com.pan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by FantasticPan on 2018/4/20.
 */
public class ClientHandler implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean started;

    /**
     * 客户端连接代码
     */
    public ClientHandler(String ip, int port) {
        this.host = ip;
        this.port = port;
        try {
            // 创建选择器
            // 获得通道管理器
            selector = Selector.open();
            // 打开监听通道
            // 获取socket通道
            socketChannel = SocketChannel.open();
            // 若为true，则通道将被置于阻塞模式；若为false，则通道将被置于非阻塞模式
            socketChannel.configureBlocking(false); // 开启非阻塞模式
            started = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        started = false;
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) ;
                else System.exit(1);
            }
            // 读消息
            if (key.isReadable()) {
                // 创建ByteBuffer，并开辟一个1M的缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 读取请求码流，返回读取到的字节数
                int readBytes = sc.read(buffer);
                // 读取到字节，对字节进行编解码
                if (readBytes > 0) {
                    // 将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                    buffer.flip();
                    // 根据缓冲区可读字节数创建字节数组
                    byte[] bytes = new byte[buffer.remaining()];
                    // 将缓冲区可读字节数组复制到新建的数组中
                    buffer.get(bytes);
                    String result = new String(bytes, "UTF-8");
                    System.out.println("客户端收到消息：" + result);
                }
                // 没有读取到字节 忽略
                //else if(readBytes==0);
                // 链路已经关闭，释放资源
                else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    /**
     * 异步发送消息，此处不含处理 “写半包” 的代码
     *
     * @param channel socket通道
     * @param request 客户端请求
     */
    private void doWrite(SocketChannel channel, String request) throws IOException {
        // 将消息编码为字节数组
        byte[] bytes = request.getBytes();
        // 根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        // 将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        // flip操作
        writeBuffer.flip();
        // 发送缓冲区的字节数组
        channel.write(writeBuffer);
    }

    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) ;
        else {
            // 为该通道注册SelectionKey.OP_CONNECT事件
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    public void sendMsg(String msg) throws Exception {
        socketChannel.register(selector, SelectionKey.OP_READ);
        doWrite(socketChannel, msg);
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            // 这个方法是用来结束当前正在运行中的java虚拟机，0是正常退出程序，非0表示非正常退出程序
            System.exit(1);
        }
        // 循环遍历selector，选择注册过的io操作的事件(第一次为SelectionKey.OP_CONNECT)
        while (started) {
            try {
                // 无论是否有读写事件发生，selector每隔1s被唤醒一次
                selector.select(1000);
                // 阻塞,只有当至少一个注册的事件发生的时候才会继续.
                //selector.select();
                // 选择就绪通道
                Set<SelectionKey> keys = selector.selectedKeys();
                // 遍历已选择的集合来访问就绪的通道
                Iterator<SelectionKey> keyIterator = keys.iterator();
                SelectionKey key;
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    // Selector不会自己从已选择集中移除SelectionKey实例，必须在处理完通道时自己移除
                    keyIterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        // selector关闭后会自动释放里面管理的资源
        if (selector != null)
            try {
                selector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
