package org.example;

import SITM.QueueService;
import com.zeroc.Ice.Current;

import java.util.LinkedList;

public class QueueServiceI implements QueueService {

    private final LinkedList<String> queue = new LinkedList<>();

    @Override
    public synchronized void enqueue(String msg, Current c) {
        queue.add(msg);
    }

    @Override
    public synchronized String dequeue(Current c) {
        return queue.isEmpty() ? null : queue.removeFirst();
    }

    @Override
    public synchronized int size(Current c) {
        return queue.size();
    }
}
