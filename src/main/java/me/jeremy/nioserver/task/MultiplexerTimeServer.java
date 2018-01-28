package me.jeremy.nioserver.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author hzjiwenzhong
 */
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;





    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port), 1024);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("server start listening port :" + port + "...");
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
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
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

    @SuppressWarnings("unchecked")
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                //new request
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                //read data
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("order : " + body);
                    String currentTime = new Date().toString();
                    //write response
                    response(sc, currentTime+"\r\n");
                } else if (readBytes < 0){
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void response(SocketChannel sc, String content) throws IOException {
        if (content != null) {
            byte[] bytes = content.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            sc.write(writeBuffer);
        }
    }
}
