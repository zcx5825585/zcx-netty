package org.zcx.netty.mqtt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TimerData {

    public static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
    public static ScheduledThreadPoolExecutor keepAliveThreadPoolExecutor = new ScheduledThreadPoolExecutor(50);

    public static ConcurrentHashMap<String, ScheduledFuture<?>> keepAliveFutureMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
}