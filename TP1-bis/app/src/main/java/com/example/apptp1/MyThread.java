package com.example.apptp1;

import java.util.concurrent.BlockingQueue;

public class MyThread extends Thread {
    private final BlockingQueue<String> queue;
    public MyThread(BlockingQueue<String> queue) {
        this.queue = queue;
    }
    public void run() {
        while (true) {
            try {
                String message = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
