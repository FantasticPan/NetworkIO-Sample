package com.pan;

import java.io.IOException;

/**
 * Created by FantasticPan on 2018/4/19.
 */
public class Client {

    private static String DEFAULT_HOST = "127.0.0.1";
    private static int DEFAULT_PORT = 12345;
    private static ClientHandler clientHandler;

    public static synchronized void start(String ip, int port) {
        if (clientHandler != null) {
            clientHandler.stop();
        }
        clientHandler = new ClientHandler(ip, port);
        new Thread(clientHandler, "Server").start();
    }

    /**
     * 向服务器发送消息
     *
     * @param msg 消息
     * @return
     * @throws IOException
     */
    public static boolean sendMsg(String msg) throws Exception {
        if (msg.equals("q")) {
            return false;
        }
        clientHandler.sendMsg(msg);
        return true;
    }

    public static void start() {
        start(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static void main(String[] args) {
        start();
    }
}
