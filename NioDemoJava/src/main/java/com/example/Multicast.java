package com.example;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.channels.DatagramChannel;

/**
 * @auther 宋疆疆
 * @date 2016/1/7.
 */
public class MultiCast {

    public static void main(String[] args) {
        send();
    }

    private static void send() {
        try {
            DatagramChannel dc = DatagramChannel.open();
            dc.bind(new InetSocketAddress(5000));
            NetworkInterface interf = NetworkInterface.getByIndex(0);
            while (true){
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
