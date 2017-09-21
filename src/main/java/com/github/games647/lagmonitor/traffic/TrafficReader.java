package com.github.games647.lagmonitor.traffic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.plugin.Plugin;

public class TrafficReader extends TinyProtocol {

    private final AtomicLong incomingBytes = new AtomicLong();
    private final AtomicLong outgoingBytes = new AtomicLong();

    public TrafficReader(Plugin plugin) {
        super(plugin);
    }

    public AtomicLong getIncomingBytes() {
        return incomingBytes;
    }

    public AtomicLong getOutgoingBytes() {
        return outgoingBytes;
    }

    @Override
    public void onChannelRead(ChannelHandlerContext handlerContext, Object object) {
        onChannel(object, true);
    }

    @Override
    public void onChannelWrite(ChannelHandlerContext handlerContext, Object object, ChannelPromise promise) {
        onChannel(object, false);
    }

    private void onChannel(Object object, boolean incoming) {
        int readableBytes = 0;
        if (object instanceof ByteBuf) {
            readableBytes = ((ByteBuf) object).readableBytes();
        } else if (object instanceof  ByteBufHolder) {
            readableBytes = ((ByteBufHolder) object).content().readableBytes();
        }

        if (incoming) {
            incomingBytes.getAndAdd(readableBytes);
        } else {
            outgoingBytes.getAndAdd(readableBytes);
        }
    }
}
