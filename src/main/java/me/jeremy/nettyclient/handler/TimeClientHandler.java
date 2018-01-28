package me.jeremy.nettyclient.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author hzjiwenzhong
 */
public class TimeClientHandler extends ChannelHandlerAdapter {
    private final ByteBuf msg;

    public TimeClientHandler() {
        byte[] req = "GET TIME ".getBytes();
        msg = Unpooled.buffer(req.length);
        msg.writeBytes(req);
    }

    //write message to server

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(msg);
    }

    //read message from server

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] resp = new byte[buf.readableBytes()];
        buf.readBytes(resp);
        String currentTime = new String(resp, "UTF-8");
        System.out.println(currentTime);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
