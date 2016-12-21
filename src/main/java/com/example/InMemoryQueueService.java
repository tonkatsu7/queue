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
    private volatile ConcurrentMap<String, Object> queueLocks; // used as queue locks, name > url
    private volatile ConcurrentMap<String, Deque<QueueMessage>> queues; // name > url
    private volatile ConcurrentMap<String, Queue<QueueMessage>> inflightQueues; // name > invisible queue
//    private volatile BiMap<String, String> queueUrl2Names; // name > url
    private volatile Object mainLock;
    private String urlPrefix;
    private long pullWaitTimeMillis;
    private long visibilityTimeoutMillis;

    public InMemoryQueueService(String urlPrefix, long pullWaitTimeMillis, long visibilityTimeoutMillis) {
        queueLocks = new ConcurrentHashMap<>();
        queues = new ConcurrentHashMap<>();
        inflightQueues = new ConcurrentHashMap<>();
//        queueUrl2Names = HashBiMap.create();
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
                queueLocks.put(queueName, new Object());
                queues.put(queueName, new ArrayDeque<QueueMessage>());
                inflightQueues.put(queueName, new ArrayDeque<>());
//                queueUrl2Names.put(queueName, queueName);
            }
        }

        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
//        return queues.keySet().;
        return queues.keySet().stream().map( queueName -> toUrl(queueName)).collect(Collectors.toSet());
    }

    @Override
    public String getQueueUrl(String queueName) {
        checkQueueName(queueName);

//        checkState(queueUrl2Names.values().contains(queueName), QUEUE_NAME_DOES_NOT_EXIST);
        checkState(queues.containsKey(queueName), QUEUE_NAME_DOES_NOT_EXIST);

//        synchronized (mainLock) {
//            return queueUrl2Names.inverse().get(queueName);
//        }
        return toUrl(queueName);
    }

    @Override
    public void deleteQueue(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);

        synchronized (mainLock) {
            if (queues.containsKey(queueName)) {
                synchronized (queueLocks.get(queueName)) {
                    queueLocks.remove(queueName);
                    queues.remove(queueName);
                    inflightQueues.remove(queueName);
//                    queueUrl2Names.remove(queueUrl);
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

        synchronized (queueLocks.get(queueName)) {
            queues.get(queueName).offer(pushMessage);
            queueLocks.get(queueName).notifyAll();
        }

        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);

        QueueMessage dequeued;

        checkState(queues.containsKey(queueName), QUEUE_URL_DOES_NOT_EXIST);

        synchronized (queueLocks.get(queueName)) {
            processInflightMessages(queueName);

            long startTime = now(); // start timer for wait time for pulling

            while (queues.get(queueName).isEmpty()) {
                try {
                    queueLocks.get(queueName).wait(50);
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

        synchronized (queueLocks.get(queueName)) {
            processInflightMessages(queueName);

            // Iterate from head (oldest pulled messages) to tail and look for receipt id
            Iterator<QueueMessage> invisibileIt = inflightQueues.get(queueName).iterator();
            while (invisibileIt.hasNext()) {
                QueueMessage message = invisibileIt.next();
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
        synchronized (queueLocks.get(queueName)) {
            // process any messages on the invisible queue that have elapsed past their timeout and put back onto the head of the main queue
            Stack<QueueMessage> visibleAgain = new Stack<>(); // via a temporary stack because want to enqueue onto the head of the main queue in as close to original FIFO order as possible

            for (QueueMessage invisibleMessage = inflightQueues.get(queueName).peek(); invisibleMessage != null; invisibleMessage = inflightQueues.get(queueName).peek()) {
                if (now() - invisibleMessage.getVisibilityTimeoutFrom() > visibilityTimeoutMillis) {
                    // if the message on the head of the invisible queue's visibility timeout has elapsed, dequeue and push onto temp stack
                    visibleAgain.push(inflightQueues.get(queueName).poll());
                } else {
                    break; // if the the message on the head's invisible queu is still valid then stop
                }
            }

            // finally pop from the temp stack and add to the head of the regular queue
            while (!visibleAgain.empty()) {
                queues.get(queueName).addFirst(visibleAgain.pop());
            }
        }
    }
}
