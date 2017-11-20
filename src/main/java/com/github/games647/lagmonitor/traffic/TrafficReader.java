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
        ByteBuf bytes = null;
        if (object instanceof ByteBuf) {
            bytes = ((ByteBuf) object);
        } else if (object instanceof ByteBufHolder) {
            bytes = ((ByteBufHolder) object).content();
        }

        if (bytes != null) {
            int readableBytes = bytes.readableBytes();
            if (incoming) {
                incomingBytes.getAndAdd(readableBytes);
            } else {
                outgoingBytes.getAndAdd(readableBytes);
            }
        }
    }
}
