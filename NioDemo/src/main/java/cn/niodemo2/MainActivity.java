package cn.niodemo2;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private boolean stop;
    private long totalByte;
    private int totalSocket;
    TextView tv_byte;
    TextView tv_count;
    TextView tv_count_5769;
    //生成一个信号监视器
    private Selector s;
    ServerSocketChannel ssc;
    DatagramChannel dc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findView();
    }

    private void findView() {
        tv_byte = (TextView) findViewById(R.id.textView);
        tv_count = (TextView) findViewById(R.id.textView2);
        tv_count_5769 = (TextView) findViewById(R.id.textView3);
    }

    public void click1(View v) {
        totalByte = 0;
        totalSocket = 0;
        stop = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!stop) {
                    SystemClock.sleep(1000);
                    tv_byte.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_byte.setText(TextFormat.formatByte(totalByte, 3));
                        }
                    });
                }
            }
        }.start();
        new Thread(new EchoServer(1982)).start();
    }

    public void click2(View v) {
        stop = true;
        try {
            if (ssc != null) {
//                ssc.close();
                ssc = null;
            }
            if (s != null) {
                Iterator<SelectionKey> it = s.keys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    SelectableChannel channel = key.channel();
                    System.out.println(channel);
                    if (channel instanceof SocketChannel) {
                        SocketChannel sc = (SocketChannel) channel;
//                        sc.socket().close();
                        channel.close();
                    } else {
                    }
                }
                System.out.println("close selector");
                s.close();
                s = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void click3(View v) {
        totalByte = 0;
        totalSocket = 0;
        stop = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!stop) {
                    SystemClock.sleep(1000);
                    tv_byte.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_byte.setText(TextFormat.formatByte(totalByte, 3));
                        }
                    });
                }
            }
        }.start();
        new ServerTest(5667);
    }

    public void click4(View v) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    dc.send(ByteBuffer.wrap("s".getBytes("UTF-8")), new InetSocketAddress("255.255.255.255", 1111));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop = true;
        try {
            if (s != null) {
                s.close();
                s = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class EchoServer implements Runnable {
        //要监听的端口号
        private int port;
        //读缓冲区
        private ByteBuffer r_bBuf = ByteBuffer.allocate(1024);
        private ByteBuffer w_bBuf;

        public EchoServer(int port) {
            this.port = port;
            try {
                s = Selector.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                //生成一个ServerScoket通道的实例对象，用于侦听可能发生的IO事件
                ssc = ServerSocketChannel.open();
                //将该通道设置为异步方式
                ssc.configureBlocking(false);
                //绑定到一个指定的端口
                ssc.socket().bind(new InetSocketAddress(port));
                //注册特定类型的事件到信号监视器上
                ssc.register(s, SelectionKey.OP_ACCEPT);
//                System.out.println("The server has been launched...");
                while (!stop) {
                    //将会阻塞执行，直到有事件发生
                    s.select();
                    Iterator<SelectionKey> it = s.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        //key定义了四种不同形式的操作
                        switch (key.readyOps()) {
                            case SelectionKey.OP_ACCEPT:
                                dealwithAccept(key);
                                break;
                            case SelectionKey.OP_CONNECT:
                                break;
                            case SelectionKey.OP_READ:
                                dealwithRead(key);
                                break;
                            case SelectionKey.OP_WRITE:
                                break;
                        }
                        //处理结束后移除当前事件，以免重复处理
                        it.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                stop = true;
//                try {
//                    if (s != null) {
//                        s.close();
//                        s = null;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }

        //处理接收连接的事件
        private void dealwithAccept(SelectionKey key) {
            try {
//                System.out.println("新的客户端请求连接...");
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                SocketChannel sc = server.accept();
                sc.configureBlocking(false);
                //注册读事件
                sc.register(s, SelectionKey.OP_READ);
                totalSocket++;
                tv_count.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_count.setText(totalSocket + "");
                    }
                });
//                System.out.println("客户端连接成功...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //处理客户端发来的消息，处理读事件
        private void dealwithRead(SelectionKey key) {
            SocketChannel sc = (SocketChannel) key.channel();
            try {
//                System.out.println("读入数据");
                r_bBuf.clear();
                //将字节序列从此通道中读入给定的缓冲区r_bBuf
                sc.read(r_bBuf);
                r_bBuf.flip();
                totalByte += r_bBuf.remaining();
                String msg = Charset.forName("UTF-8").decode(r_bBuf).toString();
                if (msg.equalsIgnoreCase("time")) {
                    w_bBuf = ByteBuffer.wrap(getCurrentTime().getBytes("UTF-8"));
                    sc.write(w_bBuf);
                    w_bBuf.clear();
                } else if (msg.equalsIgnoreCase("bye")) {
                    sc.write(ByteBuffer.wrap("已经与服务器断开连接".getBytes("UTF-8")));
                    sc.socket().close();
                } else {
                    sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
                }
                System.out.println(msg);
//                System.out.println("处理完毕...");
                r_bBuf.clear();
            } catch (IOException e) {
                e.printStackTrace();
                totalSocket--;
                tv_count.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_count.setText(totalSocket + "");
                    }
                });
                try {
                    sc.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        private String getCurrentTime() {
            Calendar date = Calendar.getInstance();
            String time = "服务器当前时间：" +
                    date.get(Calendar.YEAR) + "-" +
                    date.get(Calendar.MONTH) + 1 + "-" +
                    date.get(Calendar.DATE) + " " +
                    date.get(Calendar.HOUR) + ":" +
                    date.get(Calendar.MINUTE) + ":" +
                    date.get(Calendar.SECOND);
            return time;
        }
    }

    class ServerTest implements Runnable {
        private int port;

        public ServerTest(int port) {
            this.port = port;
            new Thread(this).start();

            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    System.out.println(networkInterface);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                dc = DatagramChannel.open();
                dc.configureBlocking(false);
                SocketAddress address = new InetSocketAddress(port);
                dc.socket().bind(address);
                s = Selector.open();
                dc.register(s, SelectionKey.OP_READ);
                System.out.println("Listening on port " + port);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
//            dc.send(ByteBuffer.wrap("s".getBytes("UTF-8")), new InetSocketAddress("255.255.255.255", port));
                while (!stop) {
                    int num = s.select();
                    if (num == 0) {
                        continue;
                    }
                    Set Keys = s.selectedKeys();
                    Iterator it = Keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey k = (SelectionKey) it.next();
                        if ((k.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                            DatagramChannel cc = (DatagramChannel) k.channel();
                            cc.configureBlocking(false);
                            buffer.clear();
                            InetSocketAddress client = (InetSocketAddress) cc.receive(buffer);
                            buffer.flip();
                            if (buffer.remaining() <= 0) {
                                System.out.println("bb is null");
                            }
                            totalByte += buffer.remaining();
                            String msg = Charset.forName("UTF-8").decode(buffer).toString();
                            System.out.println(msg + ", from: " + client);
                            ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes("UTF-8"));
                            System.out.println("response: " + wrap.remaining());
                            if (!client.getAddress().getHostAddress().equals("172.168.66.78")) {
//                                cc.send(wrap, client);
                            }
//                            SystemClock.sleep(1000);
//                        cc.send(wrap, new InetSocketAddress("255.255.255.255", port));
//                        cc.send(wrap, new InetSocketAddress("127.0.0.1", client.getPort()));
                        }
                    }
                    Keys.clear();
                }

            } catch (Exception ie) {
                ie.printStackTrace();
            } finally {
                System.out.println("end udp");
                if (dc != null) {
                    try {
                        dc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dc = null;
                }
            }
        }
    }
}
