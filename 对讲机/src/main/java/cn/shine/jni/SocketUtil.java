package cn.shine.jni;

public class SocketUtil {

	public native int createClient(byte[] ip, int port);

	public native int createServer(int port);

	public native int send(int fd, byte[] buf, int len);

	public native int stopSend(int fd);

	public native int receive(int fd, byte[] buf, int len);

	public native int stopReceive(int fd);

	static {
		System.loadLibrary("LSOCKET");
	}
}
