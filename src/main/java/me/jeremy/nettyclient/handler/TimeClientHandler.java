package me.jeremy.nettyclient.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author hzjiwenzhong
 */
public class TimeClientHandler extends ChannelHandlerAdapter {

    private final byte[] req;

    private int counter;

    public TimeClientHandler() {
        req = ("GET TIME " + System.getProperty("line.separator")).getBytes();
    }

    //write message to server

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        for (int i = 0; i < 100; ++i) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    //read message from server

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String currentTime = (String) msg;
        System.out.println(currentTime + "; the counter is: " + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
