package com.example;

import static com.example.QueueServiceUtil.*;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryQueueService implements QueueService {
    // Fields
    private volatile ConcurrentMap<String, Object> queueLocks; // used as queue locks
    private volatile ConcurrentMap<String, Deque<QueueMessage>> queues; // url > queue
    private volatile ConcurrentMap<String, Queue<QueueMessage>> invisibleQueues; // url > invisible queue
    private volatile BiMap<String, String> queueUrl2Names; // url > name
    private volatile Object mainLock;
    private String urlPrefix;
    private long pullWaitTimeMillis;
    private long visibilityTimeoutMillis;

    public InMemoryQueueService(String urlPrefix, long pullWaitTimeMillis, long visibilityTimeoutMillis) {
        queueLocks = new ConcurrentHashMap<>(); // used as queueLocks
        queues = new ConcurrentHashMap<>(); // queue name > queue
        invisibleQueues = new ConcurrentHashMap<>();
        queueUrl2Names = HashBiMap.create(); // queue name > url
        mainLock = new Object();
        this.urlPrefix = urlPrefix;
        this.pullWaitTimeMillis = pullWaitTimeMillis;
        this.visibilityTimeoutMillis = visibilityTimeoutMillis;
    }

    // Queue methods

    @Override
    public String createQueue(String queueName) {
        checkQueueName(queueName);

        String queueUrl = toUrl(queueName);

        synchronized (mainLock) {
            if (!queues.containsKey(queueUrl)) {
                queueLocks.put(queueUrl, new Object());
                queues.put(queueUrl, new ArrayDeque<QueueMessage>());
                invisibleQueues.put(queueUrl, new ArrayDeque<>());
                queueUrl2Names.put(queueUrl, queueName);
            }
        }

        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
        return queues.keySet();
    }

    @Override
    public String getQueueUrl(String queueName) {
        checkQueueName(queueName);

        checkState(queueUrl2Names.values().contains(queueName), QUEUE_NAME_DOES_NOT_EXIST);

        return queueUrl2Names.inverse().get(queueName);
    }

    @Override
    public void deleteQueue(String queueUrl) {
        checkQueueUrl(queueUrl);

        synchronized (mainLock) {
            if (queues.containsKey(queueUrl)) {
                synchronized (queueLocks.get(queueUrl)) {
                    queueLocks.remove(queueUrl);
                    queues.remove(queueUrl);
                    invisibleQueues.remove(queueUrl);
                    queueUrl2Names.remove(queueUrl);
                }
            }
        }
    }

    // Message methods

    @Override
    public QueueMessage push(String queueUrl, String message) {
        checkQueueUrl(queueUrl);
        checkMessageBody(message);

        QueueMessage pushMessage = new QueueMessage(message, generateMessageId());

        checkState(queues.containsKey(queueUrl), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queueLocks.get(queueUrl)) {
            queues.get(queueUrl).offer(pushMessage);
            queueLocks.get(queueUrl).notifyAll();
        }

        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        checkQueueUrl(queueUrl);

        checkState(queues.containsKey(queueUrl), QUEUE_URL_DOES_NOT_EXIST);

        QueueMessage dequeued;

        synchronized (queueLocks.get(queueUrl)) {
            processInvisibleMessages(queueUrl);

            long startTime = now(); // start timer for wait time for pulling

            while (queues.get(queueUrl).isEmpty()) {
                try {
                    queueLocks.get(queueUrl).wait(50);
                } catch (InterruptedException e) {
                    Throwables.propagate(e); // fatal
                }

                if ((now() - startTime) > pullWaitTimeMillis) { // if time elpased is greater than wait time
                    return new QueueMessage();
                }
            }
            // queue message with visibility timeout timestamp
            dequeued = new QueueMessage(queues.get(queueUrl).poll(), generateReceiptId(), now());
            // push onto invisible queue
            invisibleQueues.get(queueUrl).offer(dequeued);

        }

        return dequeued;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        checkQueueUrl(queueUrl);
        checkReceiptId(receiptId);

        checkState(queues.containsKey(queueUrl), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queueLocks.get(queueUrl)) {
            processInvisibleMessages(queueUrl);

            // Iterate from head (oldest pulled messages) to tail and look for receipt id
            Iterator<QueueMessage> invisibileIt = invisibleQueues.get(queueUrl).iterator();
            while (invisibileIt.hasNext()) {
                QueueMessage message = invisibileIt.next();
                if (message.getReceiptId().equals(receiptId)) { // case sensitive
                    invisibleQueues.get(queueUrl).remove(message);
                    return true;
                }
            }
        }
        return false;
    }

    // private methods

    private String toUrl(String queueName) {
        return urlPrefix + queueName;
    }

    public static String fromUrl(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
    }

    private void processInvisibleMessages(String queueUrl) {
        synchronized (queueLocks.get(queueUrl)) {
            // process any messages on the invisible queue that have elapsed past their timeout and put back onto the head of the main queue
            Stack<QueueMessage> visibleAgain = new Stack<>(); // via a temporary stack because want to enqueue onto the head of the main queue in as close to original FIFO order as possible

            for (QueueMessage invisibleMessage = invisibleQueues.get(queueUrl).peek(); invisibleMessage != null; invisibleMessage = invisibleQueues.get(queueUrl).peek()) {
                if (now() - invisibleMessage.getVisibilityTimeoutFrom() > visibilityTimeoutMillis) {
                    // if the message on the head of the invisible queue's visibility timeout has elapsed, dequeue and push onto temp stack
                    visibleAgain.push(invisibleQueues.get(queueUrl).poll());
                } else {
                    break; // if the the message on the head's invisible queu is still valid then stop
                }
            }

            // finally pop from the temp stack and add to the head of the regular queue
            while (!visibleAgain.empty()) {
                queues.get(queueUrl).addFirst(visibleAgain.pop());
            }
        }
    }
}
