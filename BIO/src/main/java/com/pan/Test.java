package com.pan;

import java.util.Random;

/**
 * Created by FantasticPan on 2018/4/18.
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {
        // 运行服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Server.start();
                // 使用线程池
                ServerBetter.start();
            }
        }).start();
        // 线程休眠一段时间，避免客户端先于服务器启动前执行代码
        Thread.sleep(100);
        // 运行客户端
        final char operators[] = {'+', '-', '*', '/'};
        final Random random = new Random(System.currentTimeMillis());
        new Thread(new Runnable() {
            @SuppressWarnings("static-access")
            @Override
            public void run() {
                while (true) {
                    // 随机产生算术表达式
                    String expression = random.nextInt(10)
                            + ""
                            + operators[random.nextInt(4)]
                            + (random.nextInt(10) + 1);
                    Client.send(expression);
                    try {
                        Thread.currentThread().sleep(random.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
