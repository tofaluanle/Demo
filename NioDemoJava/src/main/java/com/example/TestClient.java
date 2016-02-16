package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class TestClient {
    public static void main(String[] args) {
//        new MiniClient("192.168.56.101", 1982);
//        new MiniClient("172.168.66.78", 1982);
        new MiniClient("localhost", 1982);
    }
}

class MiniClient {
    private SocketChannel sc;
    private ByteBuffer w_bBuf;
    private ByteBuffer r_bBuf = ByteBuffer.allocate(1024);

    public MiniClient(String host, int port) {
        try {
            InetSocketAddress remote = new InetSocketAddress(host, port);
            sc = SocketChannel.open();
//            sc.configureBlocking(false);
            sc.connect(remote);
            if (sc.finishConnect()) {
                System.out.println("已经与服务器成功建立连接...");
            }
            while (true) {
                if (!sc.socket().isConnected()) {
                    System.out.println("已经与服务器失去了连接...");
                    return;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String str = br.readLine();
                System.out.println("读入一行数据，开始发送...");
                w_bBuf = ByteBuffer.wrap(str.getBytes("UTF-8"));
                //将缓冲区中数据写入通道
                long start = System.currentTimeMillis();
                sc.write(w_bBuf);
                long end = System.currentTimeMillis();
                System.out.println("数据发送成功... time: " + (start - end));
                w_bBuf.clear();
                System.out.println("接收服务器端响应消息...");
                r_bBuf.clear();
                //将字节序列从此通道中读入给定的缓冲区r_bBuf
                start = System.currentTimeMillis();
                sc.read(r_bBuf);
                end = System.currentTimeMillis();
                r_bBuf.flip();
                String msg = Charset.forName("UTF-8").decode(r_bBuf).toString();
                System.out.println("msg: "+msg + " time: " + (start - end));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}