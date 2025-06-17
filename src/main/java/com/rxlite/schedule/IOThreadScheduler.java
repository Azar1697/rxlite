package main.java.com.rxlite.schedule;

//package com.rxlite.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class IOThreadScheduler implements Scheduler {
    private final ExecutorService exec = Executors.newCachedThreadPool();
    @Override public void execute(Runnable task) { exec.submit(task); }
}

