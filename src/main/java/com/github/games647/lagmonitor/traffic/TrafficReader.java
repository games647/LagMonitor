package com.github.games647.lagmonitor.traffic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.atomic.LongAdder;

import org.bukkit.plugin.Plugin;

public class TrafficReader extends TinyProtocol {

    private final LongAdder incomingBytes = new LongAdder();
    private final LongAdder outgoingBytes = new LongAdder();

    public TrafficReader(Plugin plugin) {
        super(plugin);
    }

    public LongAdder getIncomingBytes() {
        return incomingBytes;
    }

    public LongAdder getOutgoingBytes() {
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
                incomingBytes.add(readableBytes);
            } else {
                outgoingBytes.add(readableBytes);
            }
        }
    }
}
