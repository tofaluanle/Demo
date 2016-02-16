package com.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;

public class ASyncUDPSvr {

    static int BUF_SZ = 1024;
    static int port = 12344;

    static public void main(String[] args) {
        ASyncUDPSvr svr = new ASyncUDPSvr();
        svr.process();
    }

    private static InetAddress getAddress() throws SocketException {
        final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        NetworkInterface networkInterfaceToUse = null;
        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.getDisplayName().contains("Virtual"))
                continue;
            if (networkInterface.isVirtual()) continue;
            if (networkInterface.isLoopback()) continue;
            if (!networkInterface.isUp()) continue;
            networkInterfaceToUse = networkInterface;
            System.out.println(networkInterfaceToUse);
        }
        return networkInterfaceToUse.getInterfaceAddresses().get(1).getAddress();
    }

    private void process() {
        try {
            Selector selector = Selector.open();
            DatagramChannel channel = DatagramChannel.open();
            InetSocketAddress isa = new InetSocketAddress(getAddress(), port);
            channel.bind(isa);
            channel.configureBlocking(false);
            SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new Con());
            while (true) {
                try {
                    selector.select();
                    Iterator selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        try {
                            SelectionKey key = (SelectionKey) selectedKeys.next();
                            selectedKeys.remove();
                            if (!key.isValid()) {
                                continue;
                            }
                            if (key.isReadable()) {
                                read(key);
                                key.interestOps(SelectionKey.OP_WRITE);
                            } else if (key.isWritable()) {
                                write(key);
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            System.err.println("glitch, continuing... " + (e.getMessage() != null ? e.getMessage() : ""));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("glitch, continuing... " + (e.getMessage() != null ? e.getMessage() : ""));
                }
            }
        } catch (IOException e) {
            System.err.println("network error: " + (e.getMessage() != null ? e.getMessage() : ""));
        }
    }

    private void read(SelectionKey key) throws IOException {
        DatagramChannel chan = (DatagramChannel) key.channel();
        Con con = (Con) key.attachment();
        con.sa = chan.receive(con.req);
        System.out.println("sender address: " + con.sa + "rcv: " + new String(con.req.array(), "UTF-8"));
        con.resp = Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap("send string"));
    }

    private void write(SelectionKey key) throws IOException {
        DatagramChannel chan = (DatagramChannel) key.channel();
        Con con = (Con) key.attachment();
        System.out.println("sending data: " + new String(con.resp.array(), "UTF-8") + " to " + con.sa);
        chan.send(con.resp, con.sa);
        System.out.println("data send");
    }

    class Con {

        ByteBuffer req;
        ByteBuffer resp;
        SocketAddress sa;

        public Con() {
            req = ByteBuffer.allocate(BUF_SZ);
        }
    }
}