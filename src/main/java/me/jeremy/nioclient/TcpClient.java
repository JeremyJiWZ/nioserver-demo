package me.jeremy.nioclient;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.jeremy.nioclient.task.TimeClientHandler;

/**
 * @author hzjiwenzhong
 */
public class TcpClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        final int maxRequest = 10000;
        String host = "localhost";
        int port = 8080;
        ExecutorService es = Executors.newFixedThreadPool(10);
        final long startTime = System.currentTimeMillis();
        Selector selector = Selector.open();
            es.execute(new TimeClientHandler(maxRequest, host, port));
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        final long endTime = System.currentTimeMillis();
        System.out.println("total time : "+ (endTime - startTime));
    }
}
