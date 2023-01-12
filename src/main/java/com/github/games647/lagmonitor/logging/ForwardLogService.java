package com.github.games647.lagmonitor.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.jul.JULServiceProvider;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class ForwardLogService implements SLF4JServiceProvider {

    private ILoggerFactory loggerFactory;
    private JULServiceProvider delegate;

    public ILoggerFactory getLoggerFactory() {
        return this.loggerFactory;
    }

    public IMarkerFactory getMarkerFactory() {
        return delegate.getMarkerFactory();
    }

    public MDCAdapter getMDCAdapter() {
        return delegate.getMDCAdapter();
    }

    @Override
    public String getRequesteApiVersion() {
        return delegate.getRequestedApiVersion();
    }

    public String getRequestedApiVersion() {
        return delegate.getRequestedApiVersion();
    }

    public void initialize() {
        this.delegate = new JULServiceProvider();
        this.loggerFactory = new ForwardingLoggerFactory();
    }
}
