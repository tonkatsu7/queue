package com.example;

import static com.example.QueueServiceUtil.*;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemoryQueueService implements QueueService {
    // Fields
    private volatile ConcurrentMap<String, Deque<QueueMessage>> queues; // name > url
    private volatile ConcurrentMap<String, Queue<QueueMessage>> inflightQueues; // name > invisible queue
    private volatile Object mainLock;
    private String urlPrefix;
    private long pullWaitTimeMillis;
    private long visibilityTimeoutMillis;

    public InMemoryQueueService(String urlPrefix, long pullWaitTimeMillis, long visibilityTimeoutMillis) {
        queues = new ConcurrentHashMap<>();
        inflightQueues = new ConcurrentHashMap<>();
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
            if (!queues.containsKey(queueName)) {
                queues.put(queueName, new ArrayDeque<QueueMessage>());
                inflightQueues.put(queueName, new ArrayDeque<>());
            }
        }

        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
        return queues.keySet().stream().map( queueName -> toUrl(queueName)).collect(Collectors.toSet());
    }

    @Override
    public String getQueueUrl(String queueName) {
        checkQueueName(queueName);

        checkState(queues.containsKey(queueName), QUEUE_NAME_DOES_NOT_EXIST);

        return toUrl(queueName);
    }

    @Override
    public void deleteQueue(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);

        synchronized (mainLock) {
            if (queues.containsKey(queueName)) {
                synchronized (queues.get(queueName)) {
                    queues.remove(queueName);
                    inflightQueues.remove(queueName);
                }
            }
        }
    }

    // Message methods

    @Override
    public QueueMessage push(String queueUrl, String message) {
        checkQueueUrl(queueUrl);
        checkMessageBody(message);

        String queueName = fromUrl(queueUrl);

        QueueMessage pushMessage = new QueueMessage(message, generateMessageId());

        checkState(queues.containsKey(queueName), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queues.get(queueName)) {
            queues.get(queueName).offer(pushMessage);
            queues.get(queueName).notifyAll();
        }

        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);

        QueueMessage dequeued;

        checkState(queues.containsKey(queueName), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queues.get(queueName)) {
            processInflightMessages(queueName);

            long startTime = now(); // start timer for wait time for pulling

            while (queues.get(queueName).isEmpty()) {
                try {
                    queues.get(queueName).wait(50);
                } catch (InterruptedException e) {
                    Throwables.propagate(e); // fatal
                }
                if ((now() - startTime) > pullWaitTimeMillis) { // if time elpased is greater than wait time
                    return new QueueMessage();
                }
            }

            dequeued = new QueueMessage(queues.get(queueName).poll(), generateReceiptId(), now()); // queue message with visibility timeout timestamp

            inflightQueues.get(queueName).offer(dequeued); // push onto invisible queue
        }

        return dequeued;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        checkQueueUrl(queueUrl);
        checkReceiptId(receiptId);

        String queueName = fromUrl(queueUrl);

        checkState(queues.containsKey(queueName), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queues.get(queueName)) {
            processInflightMessages(queueName);

            // Iterate from head (oldest pulled messages) to tail and look for receipt id
            Iterator<QueueMessage> itr = inflightQueues.get(queueName).iterator();
            while (itr.hasNext()) {
                QueueMessage message = itr.next();
                if (message.getReceiptId().equals(receiptId)) { // case sensitive
                    inflightQueues.get(queueName).remove(message);
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

    private void processInflightMessages(String queueName) {
        synchronized (queues.get(queueName)) {
            // process any messages on the invisible queue that have elapsed past their timeout and put back onto the head of the main queue
            Stack<QueueMessage> visibleAgain = new Stack<>(); // via a temporary stack because want to enqueue onto the head of the main queue in as close to original FIFO order as possible

            for (QueueMessage invisibleMessage = inflightQueues.get(queueName).peek(); invisibleMessage != null; invisibleMessage = inflightQueues.get(queueName).peek()) {
                if (now() - invisibleMessage.getVisibilityTimeoutFrom() > visibilityTimeoutMillis) {
                    visibleAgain.push(inflightQueues.get(queueName).poll()); // if the message on the head of the invisible queue's visibility timeout has elapsed, dequeue and push onto temp stack
                } else {
                    break; // if the the message on the head's invisible queue is still valid then stop
                }
            }

            while (!visibleAgain.empty()) { // finally pop from the temp stack and add to the head of the regular queue until temp stack is empty
                queues.get(queueName).addFirst(visibleAgain.pop());
            }
        }
    }
}
