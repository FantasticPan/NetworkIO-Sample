package com.pan;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by FantasticPan on 2018/4/19.
 */
public class ServerHandler implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean started;

    /**
     * 服务端连接代码
     */
    public ServerHandler(int port) {
        try {
            // 创建选择器
            // 获取通道管理器
            selector = Selector.open();
            // 打开监听通道
            // 获取一个ServerSocket通道
            serverSocketChannel = ServerSocketChannel.open();
            // 若为true，则通道将被置于阻塞模式；若为 false，则通道将被置于非阻塞模式
            serverSocketChannel.configureBlocking(false); // 非阻塞模式，与Selector一起使用时，Channel必须处于非阻塞模式下
            // 绑定端口，backlog设为1024
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            // 监听客户端连接请求，将serverSocketChannel注册到selector上，并为该通道注册SelectionKey.OP_ACCEPT事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 标记服务器已开启
            started = true;
            System.out.println("服务器已启动，端口号：" + port);
        } catch (IOException e) {
            e.printStackTrace();
            // 结束当前正在运行中的java虚拟机，0是正常退出程序，非0表示非正常退出程序
            System.exit(1);
        }
    }

    public void stop() {
        started = false;
    }

    /**
     * 异步发送应答消息
     *
     * @param channel  socket通道
     * @param response 服务器返回
     */
    private void doWrite(SocketChannel channel, String response) throws IOException {
        // 将消息编码为字节数组
        byte[] bytes = response.getBytes();
        // 根据数组容量创建ByteBuffer
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        // 将字节数组复制到缓冲区
        writeBuffer.put(bytes);
        // flip操作
        writeBuffer.flip();
        // 发送缓冲区的字节数组
        channel.write(writeBuffer);
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            // 处理新接入的请求消息
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // 通过ServerSocketChannel的accept创建SocketChannel实例
                // 完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
                SocketChannel sc = ssc.accept();
                // 设置为非阻塞的
                sc.configureBlocking(false);
                // 在与客户端连接成功后，，为客户端通道注册SelectionKey.OP_READ事件
                sc.register(selector, SelectionKey.OP_READ);
            }
            // 有可读数据事件
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
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
                    String expression = new String(bytes, "UTF-8");
                    System.out.println("服务器收到消息：" + expression);
                    // 处理数据
                    String result = null;
                    try {
                        result = Calculator.cal(expression).toString();
                    } catch (ScriptException e) {
                        result = "计算错误：" + e.getMessage();
                    }
                    // 发送应答消息
                    doWrite(sc, result);
                }
                // 没有读取到字节 忽略
                //else if (readBytes == 0)
                // 链路已经关闭，释放资源
                else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    @Override
    public void run() {
        while (started) {
            try {
                // 无论是否有读写事件发生，selector每隔1s被唤醒一次
                selector.select(1000);
                // 阻塞,只有当至少一个注册的事件发生的时候才会继续
                //selector.select()
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
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // selector关闭后会自动释放里面管理的资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
