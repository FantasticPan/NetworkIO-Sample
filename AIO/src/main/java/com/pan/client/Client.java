package com.pan.client;

import java.util.Scanner;

/**
 * Created by FantasticPan on 2018/4/22.
 */
public class Client {

    private static String DEFAULT_HOST = "127.0.0.1";
    private static int DEFAULT_PORT = 12345;
    private static AsyncClientHandler clientHandle;

    public static synchronized void start(String ip, int port) {
        if (clientHandle != null)
            return;
        clientHandle = new AsyncClientHandler(ip, port);
        new Thread(clientHandle, "Client").start();
    }

    /**
     * 向服务器发送消息
     */
    public static boolean sendMsg(String msg) {
        if (msg.equals("q")) return false;
        clientHandle.sendMsg(msg);
        return true;
    }

    public static void start() {
        start(DEFAULT_HOST, DEFAULT_PORT);
    }

    // 注解作用：告诉编译器忽略指定的警告，不用在编译完成后出现警告信息
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        Client.start();
        System.out.println("请输入请求消息：");
        Scanner scanner = new Scanner(System.in);
        while (Client.sendMsg(scanner.nextLine())) ;
    }
}
