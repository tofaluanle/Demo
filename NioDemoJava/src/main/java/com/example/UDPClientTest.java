package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UDPClientTest extends Thread {
    private String host;
    private int port;

    public UDPClientTest(String host, int port) {
        this.host = host;
        this.port = port;

        new Thread(this).start();
    }

    public void run() {
        //构造一个数据报Socket
        DatagramChannel dc = null;
        try {
            dc = DatagramChannel.open();
        } catch (IOException ex4) {
            ex4.printStackTrace();
        }
        SocketAddress address = new InetSocketAddress(host, port);
        try {
            dc.bind(new InetSocketAddress("172.168.66.79", 7777));
            dc.connect(address);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //dc = Socket.getChannel();
        //发送请求

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        ByteBuffer bb = null;
        ByteBuffer rbuf = ByteBuffer.allocate(1024);
        while (true) {
            try {
                str = br.readLine();
                System.out.println("读入一行数据，开始发送...");
                bb = ByteBuffer.wrap(str.getBytes("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                int num = dc.send(bb, address);
                rbuf.clear();
//                dc.receive(rbuf);
//                System.out.println("read: " + rbuf.remaining());
//                rbuf.flip();
//                String msg = Charset.forName("UTF-8").decode(rbuf).toString();
//                System.out.println(msg);
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }

    //start
    public static void main(String[] args) {
        String host = "127.0.0.1";//args[0];
        host = "172.168.66.78";
        host = "255.255.255.255";
        int port = 5667;

        new UDPClientTest(host, port);
    }

}