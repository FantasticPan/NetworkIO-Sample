package com.pan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO服务端，使用线程池，控制线程最大数量，本质不变
 * 因为限制了线程数量，如果发生大量并发请求，超过最大数量的线程就只能等待，直到线程池中的有空闲的线程可以被复用
 * Created by FantasticPan on 2018/4/18.
 */
public final class ServerBetter {

    private static int DEFAULT_PORT = 12345;
    // 单例的ServerSocket
    private static ServerSocket serverSocket;
    // 线程池 懒汉式的单例
    private static ExecutorService executorService = Executors.newFixedThreadPool(60);
    public static void start() {
        start(DEFAULT_PORT);
    }

    private synchronized static void start(int port) {
        if (serverSocket != null) {
            return;
        }
        try {
            // 通过构造函数创建ServerSocket
            // 如果端口合法且空闲，服务端就监听成功
            serverSocket = new ServerSocket(port);
            System.out.println("服务器已启动，端口号：" + port);
            // 通过无线循环监听客户端连接
            // 如果没有客户端接入，将阻塞在accept操作上。
            while (true) {
                Socket socket = serverSocket.accept();
                // 当有新的客户端接入时，会执行下面的代码
                // 然后创建一个新的线程处理这条Socket链路
                executorService.execute(new ServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
