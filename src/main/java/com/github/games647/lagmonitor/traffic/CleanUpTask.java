package com.github.games647.lagmonitor.traffic;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.NoSuchElementException;

/**
 * Moving this runnable to outer class fixes class loading issues on reload of the server which ends up in a
 * ClassNotFoundException on plugin disable:
 *
 * ClassNotFoundException: com.github.games647.lagmonitor.traffic.TinyProtocol$3
 */
public class CleanUpTask implements Runnable {

    private final ChannelPipeline pipeline;
    private final ChannelInboundHandlerAdapter serverChannelHandler;

    public CleanUpTask(ChannelPipeline pipeline, ChannelInboundHandlerAdapter serverChannelHandler) {
        this.pipeline = pipeline;
        this.serverChannelHandler = serverChannelHandler;
    }

    @Override
    public void run() {
        try {
            pipeline.remove(serverChannelHandler);
        } catch (NoSuchElementException e) {
            // That's fine
        }
    }
}
