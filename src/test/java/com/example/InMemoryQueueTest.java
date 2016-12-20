package com.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class InMemoryQueueTest {
    private static final long PULL_WAIT_TIME_MILLIS = 100L;
    private static final long VISIBILITY_TIMEOUT_MILLIS = 500L;
    private static final String URL_PREFIX = "http://sqs.us-east-2.amazonaws.com/123456789012/";

    private static final String FIRST_QUEUE_NAME = "MyQueue";
    private static final String FIRST_QUEUE_URL = URL_PREFIX + FIRST_QUEUE_NAME;
    private static final String SECOND_QUEUE_NAME = "MyQueue2";
    private static final String SECOND_QUEUE_URL = URL_PREFIX + SECOND_QUEUE_NAME;
    private static final String[] QUEUE_NAMES = new String [] {FIRST_QUEUE_NAME, SECOND_QUEUE_NAME,
                                                                FIRST_QUEUE_NAME + "3", FIRST_QUEUE_NAME + "4", FIRST_QUEUE_NAME + "5"};
    private static final String[] QUEUE_URLS = new String [] {FIRST_QUEUE_URL, SECOND_QUEUE_URL,
                                                                FIRST_QUEUE_URL + "3", FIRST_QUEUE_URL + "4", FIRST_QUEUE_URL + "5"};
    private static final String NON_EXISTENT_QUEUE_URL = "non-existent-url";
    private static final String EMPTY_QUEUE_URL = " ";

    private static final String QUEUE_MESSAGE_1 = "My queue message";
    private static final String QUEUE_MESSAGE_2 = "{\"message\":\"My message 2\",...}";
    private static final String[] QUEUE_MESSAGES = new String [] {QUEUE_MESSAGE_1, QUEUE_MESSAGE_2,
                                                                    QUEUE_MESSAGE_1 + " 3", QUEUE_MESSAGE_1 + " 4", QUEUE_MESSAGE_1 + " 5"};


    private static final String INVALID_QUEUE_NAME = "My invalid#queue$name";
    private static final String EMPTY_QUEUE_NAME = " ";

    private static final int MESSAGE_ID_MAX_LENGTH = 100;

    private InMemoryQueueService target = null;

    @Before
    public void setup() {
        target = new InMemoryQueueService(URL_PREFIX, PULL_WAIT_TIME_MILLIS, VISIBILITY_TIMEOUT_MILLIS);
    }

    @After
    public void teardown() { target = null; }

    private String setupFirstQueue() {
        return target.createQueue(FIRST_QUEUE_NAME);
    }

    private void setupFirstAndSecondQueues() {
        setupFirstQueue();
        target.createQueue(SECOND_QUEUE_NAME);
    }

    private void setUp5Queues() {
        for (String queueName : QUEUE_NAMES
                ) {
            target.createQueue(queueName);
        }
    }

    private QueueMessage setupAPulledMessageForTheFirstQueue() {
        target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1);
        return target.pull(FIRST_QUEUE_URL);
    }

    private QueueMessage setupAPulledMessageForTheSecondQueue() {
        target.push(SECOND_QUEUE_URL, SECOND_QUEUE_NAME + QUEUE_MESSAGE_1);
        return target.pull(SECOND_QUEUE_URL);
    }

    // Method: String createQueue(String queueName);

    @Test
    public void can_create_a_single_queue_service() {
        // Given a queue service

        // When create queue with a valid name
        String actualQueueUrl = target.createQueue(FIRST_QUEUE_NAME);

        // Then a string representing the queue URL
        Assert.assertEquals("Queue URL", FIRST_QUEUE_URL, actualQueueUrl);
        Assert.assertEquals("Number of queues", 1, target.listQueues().size());
    }

    @Test
    public void can_create_a_service_with_2_queues() {
        // Given a queue service with a single queue
        setupFirstQueue();

        // When creating queue with a new name
        String actualQueueUrl = target.createQueue(SECOND_QUEUE_NAME);

        // Then service contains 2 queues
        Assert.assertEquals("Queue URL", SECOND_QUEUE_URL, actualQueueUrl);
        Assert.assertEquals("Number of queues", 2, target.listQueues().size());
    }

    @Test
    public void can_create_a_service_with_5_queues() {
        // Given a queue service with 2 queues
        setupFirstAndSecondQueues();

        // When create another 3 queues
        Set<String> expectedQueueUrls = new HashSet<>();
        Set<String> actualQueueUrls = new HashSet<>();
        for (int i=3; i<=5; i++) {
            expectedQueueUrls.add(QUEUE_URLS[i-1]);
            actualQueueUrls.add(target.createQueue(QUEUE_NAMES[i-1]));
        }

        // Then service contains 5 queues
        Assert.assertEquals("Set of 3 new queue URLs", expectedQueueUrls, actualQueueUrls);
        Assert.assertEquals("Number of queues", 5, target.listQueues().size());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_create_queue_with_invalid_name() {
        // Given a queue service

        // When create queue with an invalid name
        target.createQueue(INVALID_QUEUE_NAME);

        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_create_queue_with_null_name() {
        // Given a queue service

        // When create queue with null name
        target.createQueue(null);

        // Then illegal argument exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_create_queue_with_empty_name() {
        // Given a queue service


        // When create queue with an empty name
        target.createQueue(EMPTY_QUEUE_NAME);

        // Then illegal argument exception
    }

    // Method: List<String> listQueues();

    @Test
    public void service_with_no_queues_returns_empty_list() {
        // Given a service with no queues

        // When list
        Set<String> actualQueueUrls = target.listQueues();

        // Then list of queue names is empty
        Assert.assertEquals("Listing all queue names should be empty",true, actualQueueUrls.isEmpty());
    }

    @Test
    public void service_with_single_queue_returns_list_of_1() {
        // Given a service with a single queue
        setupFirstQueue();

        // When list
        Set<String> actualQueueUrls = target.listQueues();

        // Then list of queue names is size 1 with correct queue name
        Assert.assertEquals("Number of queues after creating a single queue", 1, actualQueueUrls.size()); // queue size is 1
        Assert.assertEquals("Newly created queue URL exists in listing",
                true, actualQueueUrls.contains(FIRST_QUEUE_URL)); // queue's name equals the original queue name
    }

    @Test
    public void service_with_2_queues_returns_list_of_2() {
        // Given a service with 2 queues
        setupFirstAndSecondQueues();

        // When list
        Set<String> actualQueueUrls = target.listQueues();

        // Then list of queue names is size 2 with correct corresponding queue names
        Assert.assertEquals("Number of queues after creating a 2 queues", 2, actualQueueUrls.size());
        Assert.assertEquals("Newly created queue URLs exists in listing",
                true, actualQueueUrls.containsAll(Arrays.asList(FIRST_QUEUE_URL, SECOND_QUEUE_URL)));
    }

    @Test
    public void service_with_5_queues_returns_list_of_5() {
        // Given a service with 5 queues
        for (String queueName : QUEUE_NAMES
                ) {
            target.createQueue(queueName);
        }

        // When list
        Set<String> actualQueueUrls = target.listQueues();

        // Then list of queue names is size 5 with correct corresponding queue names
        Assert.assertEquals("Number of queues after creating a 5 queues", 5, actualQueueUrls.size());
        Assert.assertEquals("Newly created queue URLs exists in listing",
                true, actualQueueUrls.containsAll(Arrays.asList(QUEUE_URLS)));
    }

    // Method: String getQueueUrl(String queueName);

    @Test
    public void can_lookup_url_for_service_with_a_single_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When lookup using the valid queue name
        String actualUrl = target.getQueueUrl(FIRST_QUEUE_NAME);

        // Then the correct url is returned
        Assert.assertEquals("The correct URL is retrieved for the first queue", FIRST_QUEUE_URL, actualUrl);
    }

    @Test
    public void can_lookup_url_for_service_with_2_queues() {
        // Given a service with 2 queues
        setupFirstAndSecondQueues();

        // When lookup using the valid queue name for 2nd queues
        String actualUrl = target.getQueueUrl(SECOND_QUEUE_NAME);

        // Then the correct urls are returned
        Assert.assertEquals("The correct URL is retrieved for the second queue", SECOND_QUEUE_URL, actualUrl);
    }

    @Test
    public void can_lookup_url_for_service_with_5_queues() {
        // Given a service with 5 queues
        setUp5Queues();
        int fourthQueue = 3;

        // When lookup using the valid queue name for 4th queues
        String actualUrl = target.getQueueUrl(QUEUE_NAMES[fourthQueue]);

        // Then the correct urls are returned
        Assert.assertEquals("The correct URL is retrieved for the second queue", QUEUE_URLS[fourthQueue], actualUrl);
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_lookup_url_for_service_with_no_queues() {
        // Given a service with no queues

        // When lookup using a non-existent queue name
        target.getQueueUrl(NON_EXISTENT_QUEUE_URL);

        // Then illegal state exception
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_lookup_url_for_service_using_non_existent_name() {
        // Given a service with a single queue
        setupFirstQueue();

        // When lookup using a non-existent queue name
        target.getQueueUrl(FIRST_QUEUE_NAME + "_incorrect");

        // Then illegal state exception
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_lookup_url_using_invalid_name() {
        // Given service with a single queue
        // When lookup url using a invalid queue name
        // Then null pointer exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_lookup_url_using_null_name() {
        // Given service with a single queue
        setupFirstQueue();

        // When lookup url using a null queue name
        target.getQueueUrl(null);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_lookup_url_using_empty_name() {
        // Given service with a single queue
        setupFirstAndSecondQueues();

        // When lookup url using a empty queue name
        target.getQueueUrl(EMPTY_QUEUE_NAME);

        // Then null pointer exception
    }

    // Method: boolean deleteQueue(String queueUrl);

    @Test
    public void can_delete_queue_from_service_with_a_single_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When deleteMessage queue with valid queue url
        target.deleteQueue(FIRST_QUEUE_URL);

        // Then list queue will not appear when queried
        Assert.assertEquals("Deleted queue does is removed from the list of queue names",
                true, !target.listQueues().contains(FIRST_QUEUE_URL));
        Assert.assertEquals("Number of queues remaining after delete operation", 0, target.listQueues().size());
    }

    @Test
    public void can_delete_queue_from_service_with_2_queues() {
        // Given a service with 2 queues
        setupFirstAndSecondQueues();

        // When delete one of the queues
        target.deleteQueue(SECOND_QUEUE_URL);

        // Then service contains a single queue
        Assert.assertEquals("Deleted queue does is removed from the list of queue names",
                true, !target.listQueues().contains(SECOND_QUEUE_URL));
        Assert.assertEquals("Number of queues remaining after delete operation", 1, target.listQueues().size());
    }

    @Test
    public void can_delete_queues_from_service_with_5_queues() {
        // Given a service with 5 queues
        setUp5Queues();

        // When delete 2 of the queues
        target.deleteQueue(FIRST_QUEUE_URL);
        target.deleteQueue(SECOND_QUEUE_URL);

        // Then service contains 3 queues
        Assert.assertEquals("Deleted queue does is removed from the list of queue names",
                true, !target.listQueues().containsAll(Arrays.asList(FIRST_QUEUE_URL, SECOND_QUEUE_URL)));
        Assert.assertEquals("Number of queues remaining after delete operation", 3, target.listQueues().size());

    }

    @Test
    public void cannot_delete_queue_from_service_with_no_queues() {
        // Given a service with no queues

        // When delete queue with non-existent url
        target.deleteQueue(NON_EXISTENT_QUEUE_URL);

        // Then result is considered still successful
        Assert.assertEquals("Delete queue operation on a service with no queues leaves number of queues unchanged", 0, target.listQueues().size());
    }

    @Test
    public void cannot_delete_non_existant_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When delete queue with non-existent url
        target.deleteQueue(FIRST_QUEUE_URL + "_incorrect");

        // Then result is illegal state exception
        Assert.assertEquals("Delete queue operation on a service with 1 queues leaves number of queues unchanged", 1, target.listQueues().size());
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_delete_queue_with_invalid_url() {
        // Given a service with a single queue
        // When delete queue with an invalid url
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_delete_queue_with_null_url() {
        // Given a service with a single queue
        setupFirstQueue();

        // When delete queue with a null url
        target.deleteQueue(null);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_delete_queue_with_empty_url() {
        // Given a service with a single queue
        setupFirstQueue();

        // When delete queue with an empty url
        target.deleteQueue(EMPTY_QUEUE_URL);

        // Then illegal argument exception
    }

    // Method: QueueMessage push(String queueUrl, String message);

    @Test
    public void can_push_a_single_message_to_a_single_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When push a valid message
        QueueMessage actual = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1);

        // Then message object returned with a message id in addition to the original message body
        Assert.assertEquals("Message ID has been assigned with up to 100 chars",
                true, !actual.getMessageId().trim().isEmpty() && (actual.getMessageId().length() <= MESSAGE_ID_MAX_LENGTH));
        Assert.assertEquals("Message body of the pushed message equals that sent", QUEUE_MESSAGE_1, actual.getMessageBody());
    }

    @Test
    public void can_push_2_message_to_a_single_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When push 2 messages
        QueueMessage actualMessage1 = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1);
        QueueMessage actualMessage2 = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_2);

        // Then 2 unique messgae IDs
        Assert.assertEquals("Message ID of the first pushed message has been assigned with up to 100 chars",
                true, !actualMessage1.getMessageId().trim().isEmpty() && (actualMessage1.getMessageId().length() <= MESSAGE_ID_MAX_LENGTH));
        Assert.assertEquals("Message body of the first pushed message equals that sent", QUEUE_MESSAGE_1, actualMessage1.getMessageBody());
        Assert.assertEquals("Message ID of the second pushed message has been assigned with up to 100 chars",
                true, !actualMessage2.getMessageId().trim().isEmpty() && (actualMessage2.getMessageId().length() <= MESSAGE_ID_MAX_LENGTH));
        Assert.assertEquals("Message body of the second pushed message equals that sent", QUEUE_MESSAGE_2, actualMessage2.getMessageBody());
        Assert.assertEquals("Pushing 2 messages results in 2 distinct message IDs",
                true, actualMessage1.getMessageId()!=actualMessage2.getMessageId());
    }

    @Test
    public void can_push_5_message_to_a_single_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When push 5 messages
        Set<String> actualMessageIds = new HashSet<>();
        for (String messageBody : QUEUE_MESSAGES
                ) {
            actualMessageIds.add(target.push(FIRST_QUEUE_URL, messageBody).getMessageId());
        }

        // Then 5 unique messgae IDs
        Assert.assertEquals("Pushing 5 messages results in 5 distinct message IDs", 5, actualMessageIds.size());
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_push_to_a_non_existent_url() {
        // Given a service with no queues

        // When pushing a message to a non-existent url
        QueueMessage actual = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1);

        // Then new queue created
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_push_with_invalid_url() {
        // Given a service with a single queue
        // When pushing a message with an invalid url
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_push_with_null_url() {
        // Given a service with a single queue

        // When pushing a message with null url
        target.push(null, QUEUE_MESSAGE_1);

        // Then illegal argument exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_push_with_empty_url() {
        // Given only a message body

        // When pushing a message with null url
        target.push(EMPTY_QUEUE_URL, QUEUE_MESSAGE_1);

        // Then illegal argument exception
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_push_with_invalid_message_body() {
        // Given a service with a single queue
        // When pushing a message with an invalid message body
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_push_with_null_messsage_body() {
        // Given a service with a single queue

        // When pushing a message with null message body
        target.push(FIRST_QUEUE_URL, null);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_push_with_empty_message_body() {
        // Given a service with a single queue
        setupFirstQueue();

        // When pushing a message with an empty message body
        target.push(FIRST_QUEUE_URL, " ");

        // Then illegal argument exception
    }

    // Method: QueueMessage pull(String queueUrl);

    @Test
    public void can_pull_a_single_message_from_a_single_queue() {
        // Given a queue with one message pushed
        setupFirstQueue();
        QueueMessage pushedMessageFixture = target.push(FIRST_QUEUE_URL,  QUEUE_MESSAGE_1);

        // When pull a message
        QueueMessage actualMessage = target.pull(FIRST_QUEUE_URL);

        // Then pulled message is the same message with a receipt id
        Assert.assertEquals("Pulled message equals the pushed message", pushedMessageFixture, actualMessage);
        Assert.assertEquals("Pulled message contains a receipt ID", true, !actualMessage.getReceiptId().isEmpty());
    }

    @Test
    public void can_pull_messages_from_2_queues() {
        // Given a service with 2 queues with 1 message each
        setupFirstAndSecondQueues();
        QueueMessage pushedToFirstFixture = target.push(FIRST_QUEUE_URL, FIRST_QUEUE_NAME + QUEUE_MESSAGE_1);
        QueueMessage pushedToSecondFixture = target.push(SECOND_QUEUE_URL, SECOND_QUEUE_NAME + QUEUE_MESSAGE_1);

        // When pull 1 message each from 2 queues
        QueueMessage actualPulledFromFirst = target.pull(FIRST_QUEUE_URL);
        QueueMessage actualPulledFromSecond = target.pull(SECOND_QUEUE_URL);

        // Then pulled messages correspond to the pushed messages
        Assert.assertEquals("Message pulled from first queue should equal original", pushedToFirstFixture, actualPulledFromFirst);
        Assert.assertEquals("Message pulled from first queue should equal original", pushedToSecondFixture, actualPulledFromSecond);
    }

    @Test
    public void can_pull_messages_from_5_queues() {
        // Given a service with 5 queues with 5 messages each
        setUp5Queues();
        for (String url : QUEUE_URLS
                ) {
            for (String messageBody : QUEUE_MESSAGES
                    ) {
                target.push(url, messageBody);
            }
        }

        // When pull respective messages each from all queues
        int[] actualPulledMessageCount = new int[QUEUE_URLS.length];
        for (int i=0; i<actualPulledMessageCount.length; i++) {
            actualPulledMessageCount[i] = 0;
        }
        int i=0;
        for (String url : QUEUE_URLS
                ) {
            for (String messageBody : QUEUE_MESSAGES
                    ) {
                QueueMessage pulledMessage = target.pull(url);
                if (!pulledMessage.isEmpty())
                    actualPulledMessageCount[i]++;
            }
            i++;
        }

        // Then pulled messages correspond to the pushed messages
        for (int j=0; j<QUEUE_URLS.length; j++) {
            Assert.assertEquals("Number of messages pulled from queue " + j, QUEUE_MESSAGES.length, actualPulledMessageCount[j]);
        }
    }

    @Test
    public void pulling_from_a_queue_with_no_messages_returns_empty_message() {
        // Given a service with a single queue but no messages
        setupFirstQueue();

        // When pull
        QueueMessage actualMessage = target.pull(FIRST_QUEUE_URL);

        // Then empty message
        Assert.assertEquals("Pulling form an empty queue returns an empty message", true, actualMessage.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_pull_from_service_with_no_queues() {
        // Given a service with no queues

        // When pull
        target.pull(FIRST_QUEUE_URL);

        // Then illegal state exception
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_pull_from_non_existent_queue() {
        // Given a service with a single queue
        setupFirstQueue();

        // When pull
        target.pull(FIRST_QUEUE_URL + "_incorrect");

        // Then illegal state exception
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_pull_with_invalid_url() {
        // Given a service with a single queue
        // When pull with invalid url
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_pull_with_null_url() {
        // Given a service with a single queue
        setupFirstQueue();

        // When pull with null url
        target.pull(null);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_pull_with_empty_url() {
        // Given a service with a single queue
        setupFirstQueue();

        // When pull with empty url
        target.pull(EMPTY_QUEUE_URL);

        // Then illegal argument exception
    }

    // java.util.ConcurrentModificationException ???

    // Method: boolean deleteMessage(String queueUrl, String receiptId);

    @Test
    public void can_delete_a_single_message_from_a_single_queue() {
        // Given a service with a single queue and a single pushed then pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete
        boolean actual = target.deleteMessage(FIRST_QUEUE_URL, pulledMessageFixture.getReceiptId());

        // Then subsequent pull returns empty message
        Assert.assertEquals("Delete message operation should be successful", true, actual);
        Assert.assertEquals("Pulling from a queue of 1 message after a delete should return an empty message", true, target.pull(FIRST_QUEUE_URL).isEmpty());
    }

    @Test
    public void can_delete_messages_from_2_queues() {
        // Given a service with 2 queues each with 1 message pushed and pulled
        setupFirstAndSecondQueues();
        QueueMessage pulledFromFirstFixture = setupAPulledMessageForTheFirstQueue();
        QueueMessage pulledFromSecondFixture = setupAPulledMessageForTheSecondQueue();

        // When delete from each queue
        boolean actualFromFirst = target.deleteMessage(FIRST_QUEUE_URL, pulledFromFirstFixture.getReceiptId());
        boolean actualFromSecond = target.deleteMessage(SECOND_QUEUE_URL, pulledFromSecondFixture.getReceiptId());

        // Then subsequent pulls returns empty messages
        Assert.assertEquals("Delete message operation from first queue should be successful", true, actualFromFirst);
        Assert.assertEquals("Pulling from a queue of 1 message after a delete should return an empty message", true, target.pull(FIRST_QUEUE_URL).isEmpty());
        Assert.assertEquals("Delete message operation from second queue should be successful", true, actualFromSecond);
        Assert.assertEquals("Pulling from a queue of 1 message after a delete should return an empty message", true, target.pull(SECOND_QUEUE_URL).isEmpty());
    }

    @Test
    @Ignore
    public void can_delete_messages_from_5_queues() {
        // Given
        // When
        // Then
    }

    @Test
    public void can_delete_message_just_before_visibility_timout_elapsed() throws InterruptedException, ExecutionException {
        // Given a service with a single queue and a single pushed then pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete after visibility timeout
        Callable<Object> deleteAfterVisibilityTimeout = () -> {
            return target.deleteMessage(FIRST_QUEUE_URL, pulledMessageFixture.getReceiptId());
        };
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(deleteAfterVisibilityTimeout,
                                                                            VISIBILITY_TIMEOUT_MILLIS - 100,
                                                                            TimeUnit.MILLISECONDS);
        boolean actualResult = (boolean) scheduledFuture.get();
        scheduledExecutorService.shutdown();

        // Then subsequent pull returns empty message
        Assert.assertEquals("Delete message operation should be successful", true, actualResult);
        Assert.assertEquals("Pulling from a queue of 1 message after a delete should return an empty message", true, target.pull(FIRST_QUEUE_URL).isEmpty());
    }

    @Test
    public void cannot_delete_message_after_visibility_timout_elapsed() throws InterruptedException, ExecutionException {
        // Given a service with a single queue and a single pushed then pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete after visibility timeout
        Callable<Object> deleteAfterVisibilityTimeout = () -> {
            return target.deleteMessage(FIRST_QUEUE_URL, pulledMessageFixture.getReceiptId());
        };
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(deleteAfterVisibilityTimeout,
                                                                            VISIBILITY_TIMEOUT_MILLIS + 100,
                                                                            TimeUnit.MILLISECONDS);
        boolean actualResult = (boolean) scheduledFuture.get();
        scheduledExecutorService.shutdown();

        QueueMessage actualMessage = target.pull(FIRST_QUEUE_URL);
        // Then subsequent pull returns empty message
        Assert.assertEquals("Delete message operation should be unsuccessful", false, actualResult);
        Assert.assertEquals("Pulling again after visibility timeout elapsed should result in same message", pulledMessageFixture, actualMessage);
        Assert.assertEquals("Receipt ID should be different", true, pulledMessageFixture.getReceiptId()!=actualMessage.getReceiptId());
        Assert.assertEquals("New visibility timeout should be later that the first", true, actualMessage.getVisibilityTimeoutFrom() > pulledMessageFixture.getVisibilityTimeoutFrom());
    }

    @Test
    public void cannot_delete_message_with_incorrect_receipt_id() throws InterruptedException, ExecutionException {
        // Given a service with a single queue and a single pushed then pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with incorrect receipt id
        boolean actual = target.deleteMessage(FIRST_QUEUE_URL, "473249807efdsujfhdsfs7df9dfs");

        // Then false
        Assert.assertEquals("Delete message operation should be unsuccessful", false, actual);
        // Fetch after visibility timeout should pull a message with same message body and message id but different receipt id
        Callable<QueueMessage> pullAfterVisibilityTimeout = () -> {
            return target.pull(FIRST_QUEUE_URL);
        };
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(pullAfterVisibilityTimeout,
                                                                            VISIBILITY_TIMEOUT_MILLIS + 1,
                                                                            TimeUnit.MILLISECONDS);
        QueueMessage actualMessage = (QueueMessage) scheduledFuture.get();
        scheduledExecutorService.shutdown();
        Assert.assertEquals("Pulling from a queue of 1 message after a unsuccessful once visibility timeout has elapsed should return the same message", pulledMessageFixture, actualMessage);
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_delete_message_from_a_service_with_no_queues() {
        // Given a service with a single queue and a pushed and pulled message

        // When delete message with non-existent queue url but valid receipt it
        target.deleteMessage(FIRST_QUEUE_URL + "_incorrect", "fdsf79d97f9sd67ds6gfds7g6df6s9");

        // Then illegal state exception
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_delete_message_from_a_non_existent_queues() {
        // Given a service with a single queue and a pushed and pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with non-existent queue url but valid receipt it
        target.deleteMessage(FIRST_QUEUE_URL + "_incorrect", pulledMessageFixture.getReceiptId());

        // Then illegal state exception
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_delete_message_with_invalid_url() {
        // Given
        // When
        // Then
    }

    @Test(expected = NullPointerException.class)
    public void cannot_delete_message_with_null_url() {
        // Given a service with a single queue and a pushed and pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with null url
        target.deleteMessage(null, pulledMessageFixture.getReceiptId());

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_delete_message_with_empty_url() {
        // Given a service with a single queue and a pushed and pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with null url
        target.deleteMessage(EMPTY_QUEUE_URL, pulledMessageFixture.getReceiptId());

        // Then illegal argument exception
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_delete_message_with_invalid_receipt_id() {
        // Given
        // When
        // Then
    }

    @Test(expected = NullPointerException.class)
    public void cannot_delete_message_with_null_receipt_id() {
        // Given a service with a single queue and a pushed and pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with null url
        target.deleteMessage(FIRST_QUEUE_URL, null);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_delete_message_with_empty_receipt_id() {
        // Given a service with a single queue and a pushed and pulled message
        setupFirstQueue();
        QueueMessage pulledMessageFixture = setupAPulledMessageForTheFirstQueue();

        // When delete message with null url
        target.deleteMessage(EMPTY_QUEUE_URL, " ");

        // Then illegal argument exception
    }

    // java.util.ConcurrentModificationException ???

    // Concurrency tests

    @Test
    public void a_single_queue_supports_multiple_producers_and_consumers() throws InterruptedException, ExecutionException {
        // Given 2 producers producing 9 messages each
        setupFirstQueue();
        Set<QueueMessage> pushedMessagesSetFixture = new HashSet<>();
        Runnable producer = () -> {
            for (int i=0; i<15; i++) {
                target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1 + i);
            }
        };

        // When 3 consumers
        Set<QueueMessage> actualPulledMessagesSet = new HashSet<>();
        Runnable consumer = () -> {
            for (int i=0; i<15; i++) {
                QueueMessage message = target.pull(FIRST_QUEUE_URL);
                if (!message.isEmpty())
                    target.deleteMessage(FIRST_QUEUE_URL, message.getReceiptId());

            }
        };

        Thread producerThread1 = new Thread(producer, "Producer1");
        Thread producerThread2 = new Thread(producer, "Producer2");
        Thread consumerThread1 = new Thread(consumer, "Consumer1");
        Thread consumerThread2 = new Thread(consumer, "Consumer2");
        Thread consumerThread3 = new Thread(consumer, "Consumer3");

        producerThread1.start();
        producerThread2.start();
        consumerThread1.start();
        consumerThread2.start();
        consumerThread3.start();

        producerThread1.join();
        producerThread2.join();
        consumerThread1.join();
        consumerThread2.join();
        consumerThread3.join();
//
        // Then resulting pulled messages should equal those originally pushed
//        Assert.assertEquals("The set of pushed messages is a subset of the pulled messages (but likely the equivalent set)",
//                true, actualPulledMessagesSet.containsAll(pushedMessagesSetFixture));
        // And there are no remaining messages in the queue
        Assert.assertEquals("Any subsequent pulls returns an empty queue message because the queue should be empty",
                true, target.pull(FIRST_QUEUE_URL).isEmpty());
    }

    @Test
    public void multiple_queues_supports_multiple_producers_and_consumers() throws InterruptedException {
        // Given 2 producers for 2 queues
        setupFirstAndSecondQueues();
        Set<QueueMessage> pushedMessagesSetFixture = new HashSet<>();
        Runnable producerFirstQueue = () -> {
            for (int i=0; i<15; i++) {
                QueueMessage message = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1 + i);
            }
        };

        Runnable producerSecondQueue = () -> {
            for (int i=0; i<15; i++) {
                QueueMessage message = target.push(SECOND_QUEUE_URL, QUEUE_MESSAGE_1 + i);
            }
        };

        // When 2 consumers on each queue
        Set<QueueMessage> actualPulledMessagesSet = new HashSet<>();
        Runnable consumerFirstQueue = () -> {
            for (int i=0; i<15; i++) {
                QueueMessage message = target.pull(FIRST_QUEUE_URL);
                if (!message.isEmpty())
                    target.deleteMessage(FIRST_QUEUE_URL, message.getReceiptId());
            }
        };

        Runnable consumerSecondQueue = () -> {
            for (int i=0; i<15; i++) {
                QueueMessage message = target.pull(SECOND_QUEUE_URL);
                if (!message.isEmpty())
                    target.deleteMessage(SECOND_QUEUE_URL, message.getReceiptId());
            }
        };

        Thread producerThread1 = new Thread(producerFirstQueue, "Producer1");
        Thread producerThread2 = new Thread(producerSecondQueue, "Producer2");
        Thread consumerThread1 = new Thread(consumerFirstQueue, "Consumer1");
        Thread consumerThread2 = new Thread(consumerFirstQueue, "Consumer2");
        Thread consumerThread3 = new Thread(consumerSecondQueue, "Consumer3");
        Thread consumerThread4 = new Thread(consumerSecondQueue, "Consumer4");

        producerThread1.start();
        producerThread2.start();
        consumerThread1.start();
        consumerThread2.start();
        consumerThread3.start();
        consumerThread4.start();

        producerThread1.join();
        producerThread2.join();
        consumerThread1.join();
        consumerThread2.join();
        consumerThread3.join();
        consumerThread4.join();

        // Then resulting pulled messages should equal those originally pushed
//        Assert.assertEquals("The set of pushed messages is a subset of the pulled messages (but likely the equivalent set)",
//                true, actualPulledMessagesSet.containsAll(pushedMessagesSetFixture));
        // And there are no remaining messages in the queue
        Assert.assertEquals("Any subsequent pulls returns an empty queue message because the queue should be empty",
                true, target.pull(FIRST_QUEUE_URL).isEmpty());
        Assert.assertEquals("Any subsequent pulls returns an empty queue message because the queue should be empty",
                true, target.pull(SECOND_QUEUE_URL).isEmpty());
    }
}
