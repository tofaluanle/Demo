package com.example;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


public class UDPServerTest implements Runnable {
    private int port;

    public UDPServerTest(int port) {
        this.port = port;
        new Thread(this).start();
    }

    public void run() {
        try {
            DatagramChannel dc = DatagramChannel.open();
            dc.configureBlocking(false);
            SocketAddress address = new InetSocketAddress(port);
            dc.bind(address);
            Selector select = Selector.open();
            dc.register(select, SelectionKey.OP_READ);
            System.out.println("Listening on port " + port);

            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
//            dc.send(ByteBuffer.wrap("s".getBytes("UTF-8")), new InetSocketAddress("255.255.255.255", port));
            int number = 0;  //只为记录接受的字节数
            while (true) {
                int num = select.select();
                if (num == 0) {
                    continue;
                }
                Set Keys = select.selectedKeys();
                Iterator it = Keys.iterator();

                while (it.hasNext()) {
                    SelectionKey k = (SelectionKey) it.next();
                    if ((k.readyOps() & SelectionKey.OP_READ)== SelectionKey.OP_READ) {
                        DatagramChannel cc = (DatagramChannel) k.channel();
                        cc.configureBlocking(false);
                        buffer.clear();
                        InetSocketAddress client = (InetSocketAddress) cc.receive(buffer);
                        buffer.flip();
                        if (buffer.remaining() <= 0) {
                            System.out.println("bb is null");
                        }
                        //记录接收到的字节总数
                        number += buffer.remaining();
                        String msg = Charset.forName("UTF-8").decode(buffer).toString();
                        System.out.println(msg + ", from: " + client);
                        ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes("UTF-8"));
                        System.out.println("response: " + wrap.remaining());
//                        cc.send(wrap, client);
//                        cc.send(wrap, new InetSocketAddress("255.255.255.255", port));
//                        cc.send(wrap, new InetSocketAddress("127.0.0.1", client.getPort()));
                    }
                }
                Keys.clear();
            }

        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }

    static public void main(String args[]) {
        int port = 5667;//Integer.parseInt( args[0] );

        new UDPServerTest(port);
    }
} 