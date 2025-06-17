package main.java.com.rxlite.schedule;

//package com.rxlite.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ComputationScheduler implements Scheduler {
    private final ExecutorService exec =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    @Override public void execute(Runnable task) { exec.submit(task); }
}

