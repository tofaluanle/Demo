package com.example.jnithread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class NetUtil {

	public static String getIp() throws IOException {
		String localIp = null;
		for (Enumeration<NetworkInterface> en = NetworkInterface
				.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress()) {
					if (inetAddress.isReachable(1000)) {
						InetAddress localInetAddress = inetAddress;
						localIp = inetAddress.getHostAddress().toString();
						byte[] localIpBytes = inetAddress.getAddress();
						System.arraycopy(localIpBytes, 0, new byte[255], 44, 4);
					}
				}
			}
		}
		return localIp;
	}

}
