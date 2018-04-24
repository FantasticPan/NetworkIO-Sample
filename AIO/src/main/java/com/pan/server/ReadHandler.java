package com.pan.server;

import com.pan.util.Calculator;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by FantasticPan on 2018/4/21.
 */
public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    // 用于读取半包消息和发送应答
    private AsynchronousSocketChannel channel;

    public ReadHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    // 发送消息
    private void doWrite(String result) {
        byte[] bytes = result.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        // 使缓冲区为一系列新的通道写入或相对获取 操作做好准备，将操作模式转为从buff读出数据
        writeBuffer.flip();
        // 异步写数据 参数与前面的read一样
        channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                // 如果没有发送完，就继续发送直到完成
                // hasRemaining()，判断当前位置是否在限制范围内，告知程序在当前位置和限制之间是否有元素，是否继续往下读取数据
                if (buffer.hasRemaining()) {
                    channel.write(buffer, buffer, this);
                } else {
                    // 创建新的Buffer
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    // 异步读  第三个参数为接收消息回调的业务Handler
                    channel.read(readBuffer, readBuffer, new ReadHandler(channel));
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 读取到消息后的处理
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] message = new byte[attachment.remaining()];
        attachment.get(message);
        try {
            String expression = new String(message, "UTF-8");
            System.out.println("服务器收到消息: " + expression);
            String calResult = null;
            try {
                calResult = Calculator.cal(expression).toString();
            } catch (ScriptException e) {
                calResult = "计算错误：" + e.getMessage();
            }
            // 向客户端发送消息
            doWrite(calResult);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
