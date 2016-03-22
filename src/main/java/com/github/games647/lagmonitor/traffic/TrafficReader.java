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
    public void onChannelRead(ChannelHandlerContext handlerContext, Object object) throws Exception {
        if (object instanceof ByteBuf) {
            int readableBytes = ((ByteBuf) object).readableBytes();
//            System.out.println("IN-BYTE: " + readableBytes);
            incomingBytes.getAndAdd(readableBytes);
        } else if (object instanceof ByteBufHolder) {
            int readableBytes = ((ByteBufHolder) object).content().readableBytes();
//            System.out.println("IN-HOLDER: " + readableBytes);
            incomingBytes.getAndAdd(readableBytes);
        }
    }

    @Override
    public void onChannelWrite(ChannelHandlerContext handlerContext, Object object, ChannelPromise promise)
            throws Exception {
        if (object instanceof ByteBuf) {
            int readableBytes = ((ByteBuf) object).readableBytes();
//            System.out.println("OUT-BYTE: " + readableBytes);
            outgoingBytes.getAndAdd(readableBytes);
        } else if (object instanceof ByteBufHolder) {
            int readableBytes = ((ByteBufHolder) object).content().readableBytes();
//            System.out.println("OUT-HOLDER: " + readableBytes);
            outgoingBytes.getAndAdd(readableBytes);
        }
    }
}
