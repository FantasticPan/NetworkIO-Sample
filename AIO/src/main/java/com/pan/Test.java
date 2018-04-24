package com.pan;

import com.pan.client.Client;
import com.pan.server.Server;

import java.util.Scanner;

/**
 * Created by FantasticPan on 2018/4/22.
 */
public class Test {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws InterruptedException {
        Server.start();
        Thread.sleep(100);
        Client.start();
        System.out.println("请输入请求消息：");
        Scanner scanner = new Scanner(System.in);
        while(Client.sendMsg(scanner.nextLine()));
    }
}
