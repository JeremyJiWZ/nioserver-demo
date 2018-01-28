package me.jeremy.bioclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BioTcpClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        String host = "localhost";
        int port = 8080;
        ExecutorService es = Executors.newFixedThreadPool(10);
        final int maxThreads = 10000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < maxThreads; ++i) {
            Socket socket = new Socket(host, port);
            es.execute(new RequestClient(i, socket));
        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        System.out.println("total time : " + (endTime - startTime));
    }

    static class RequestClient implements Runnable {
        Socket socket;
        int i;

        public RequestClient(int i, Socket socket) {
            this.i = i;
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("GET TIME");
                String currentTime = in.readLine();
                System.out.println(i + " current Time : " + currentTime);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
