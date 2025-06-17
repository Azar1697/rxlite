package main.java.com.rxlite.schedule;

//package com.rxlite.schedulers;

@FunctionalInterface
public interface Scheduler {
    void execute(Runnable task);
}

