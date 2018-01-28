package me.jeremy.nioclient.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author hzjiwenzhong
 */
public class TimeClientHandler implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private volatile boolean stop;
    private int maxConnections;
    private int i;
    private int fi;

    public TimeClientHandler(int maxConnections, String host, int port) {
        this.maxConnections = maxConnections;
        this.host = host;
        this.port = port;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (i < maxConnections || fi < maxConnections) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                doConnect(socketChannel);
                fi++;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                selector.select(1000);
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleRequest(key);
                    } catch (IOException e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doRequest(sc);
                } else {
                    System.out.println("connect fail, exit");
                    System.exit(2);
                }
            }
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBytes];
                    readBuffer.get(bytes);
                    String currentTime = new String(bytes, "UTF-8");
                    System.out.println(String.format("%d get time from server : %s", ++i, currentTime));
                    this.stop = true;
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void doConnect(SocketChannel socketChannel) throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doRequest(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doRequest(SocketChannel sc) throws IOException {
        byte[] req = "GET TIME".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        sc.write(writeBuffer);
        if (writeBuffer.hasRemaining()) {
            //do while 循环写，解决半包问题
        }
    }
}
