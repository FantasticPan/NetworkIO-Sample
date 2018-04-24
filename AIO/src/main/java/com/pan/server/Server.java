package com.pan.server;

/**
 * AIO服务端
 * Created by FantasticPan on 2018/4/21.
 */
public class Server {

    private static int DEFAULT_PORT = 12345;
    private static AsyncServerHandler serverHandler;
    public volatile static long clientCount = 0;

    public static synchronized void start(int port) {
        if (serverHandler != null) {
            return;
        }
        serverHandler = new AsyncServerHandler(port);
        new Thread(serverHandler, "Server").start();
    }

    public static void start() {
        start(DEFAULT_PORT);
    }

    public static void main(String[] args) {
        Server.start();
    }
}
