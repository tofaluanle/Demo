package com.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;

public final class ASyncUDPClient {

    public static void main(String[] args) throws IOException {
        InetSocketAddress hostAddress = new InetSocketAddress("255.255.255.255", 12344);
        System.out.println(hostAddress);

        // Create a non-blocking socket channel
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().setBroadcast(true);
        channel.socket().bind(new InetSocketAddress(getAddress(), 12345));
        channel.configureBlocking(false);

        // Kick off connection establishment
//        channel.connect(hostAddress);

        ByteBuffer buffer = getBuffer();

        Selector selector = Selector.open();
        channel.send(buffer, hostAddress);
        System.out.println("data send");
        channel.register(selector, SelectionKey.OP_READ);

        while (true) {
            final int select = selector.select();
            System.out.println("select " + select);
            Iterator selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                System.out.println("key selected");
                SelectionKey key = (SelectionKey) selectedKeys.next();
                selectedKeys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isReadable()) {
                    System.out.println("read");
                } else if (key.isWritable()) {
                    System.out.println("write");
                }
            }
        }
    }

    private static ByteBuffer getBuffer() throws CharacterCodingException {
        return Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap("1234"));
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

}