package me.jeremy.nioserver;

import me.jeremy.nioserver.task.MultiplexerTimeServer;

/**
 * @author hzjiwenzhong
 */
public class Bootstrap {
    public static void main(String[] args) {
        int port = 8080;
        new Thread(new MultiplexerTimeServer(port), "nio-timeserver-001").start();
    }
}
