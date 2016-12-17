package com.example;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InMemoryQueueService implements QueueService {
    private static final long WAIT_TIME_SECONDS = 10L;

    private static volatile ConcurrentMap<String, Object> queueLocks = new ConcurrentHashMap<>(); // used as queueLocks
    private static volatile ConcurrentMap<String, Queue<QueueMessage>> queues = new ConcurrentHashMap<>(); // queue name > queue
    private static volatile BiMap<String, String> queueUrlLookup = HashBiMap.create(); // queue name > url
    private static volatile Object mainLock = new Object();

    // Queue methods

    @Override
    public String createQueue(String queueName) {
        checkNotNull(queueName);
        checkArgument(!queueName.isEmpty(), "Queue name cannot be an empty string");

        if (!queues.containsKey(queueName)) {
            queueUrlLookup.put(queueName, queueName);
            queues.put(queueName, new ArrayDeque<QueueMessage>());
            queueLocks.put(queueName, new Object());
        }

        return queueName;
    }

    @Override
    public List<String> listQueues() {
        List<String> list = new ArrayList<String>();
        list.addAll(queues.keySet());
        return list;
    }

    @Override
    public String getQueueUrl(String queueName) {
        return null;
    }

    @Override
    public boolean deleteQueue(String queueUrl) {
        checkNotNull(queueUrl);
        checkArgument(!queueUrl.isEmpty(), "Queue URL cannot be empty");

        if (queues.containsKey(queueUrl)) {
            queues.remove(queueUrl);
            queueUrlLookup.remove(queueUrl);
            queueLocks.remove(queueUrl);
            return true;
        } else {
            return false;
        }
    }

    // Message methods

    @Override
    public QueueMessage push(String queueUrl, String message) {
        checkNotNull(queueUrl);
        checkArgument(!queueUrl.isEmpty(), "Queue URL cannot be an empty string");

        checkNotNull(message);
        checkArgument(!message.isEmpty(), "Message body cannot be an empty string");

        QueueMessage pushMessage = new QueueMessage(message, generateMessageId());

        synchronized (queueLocks.get(queueUrl)) {
            queues.get(queueUrl).offer(pushMessage);
            queueLocks.get(queueUrl).notifyAll();
        }

        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {

        QueueMessage dequeued;

        synchronized (queueLocks.get(queueUrl)) {
            while (queues.get(queueUrl).isEmpty()) {
                try {
                    queueLocks.get(queueUrl).wait(TimeUnit.SECONDS.toMillis(WAIT_TIME_SECONDS));
                } catch (InterruptedException e) {
                    return new QueueMessage();
                }
            }
            dequeued = queues.get(queueUrl).poll();
        }

        return new QueueMessage(dequeued, generateReceiptId());
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        return false;
    }

    // private methods

    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    private String generateReceiptId() {
        return UUID.randomUUID().toString();
    }

    private String fromUrl(String queueName) {
        return null;
    }

    private String fromQueueName(String queueUrl) {
        return null;
    }
}
