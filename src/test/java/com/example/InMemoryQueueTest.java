package com.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.List;

public class InMemoryQueueTest {
    private static final String FIRST_QUEUE_NAME = "MyQueue";
    private static final String FIRST_QUEUE_URL = "MyQueue";
    private static final String SECOND_QUEUE_NAME = "MySecondQueue";
    private static final String SECOND_QUEUE_URL = "MySecondQueue";
    private static final String QUEUE_MESSAGE_1 = "My queue message";
    private static final String INVALID_QUEUE_NAME = "My invalid#queue$name";
    private static final String EMPTY_QUEUE_NAME = "";

    private InMemoryQueueService target = null;


    @Before
    public void setup() {
        target = new InMemoryQueueService();
    }

    private String setupFirstQueue() {
        return target.createQueue(FIRST_QUEUE_NAME);
    }

    private void setupFirstAndSecondQueue() {
        setupFirstQueue();
        target.createQueue(SECOND_QUEUE_NAME);
    }

    // Method: String createQueue(String queueName);

    @Test
    public void can_create_a_single_queue() {
        // Given a queue service

        // When create queue with a valid name
        String actualQueueUrl = target.createQueue(FIRST_QUEUE_NAME);

        // Then a string representing the queue URL
        Assert.assertEquals("Queue name should equal queue URL", FIRST_QUEUE_NAME, actualQueueUrl);
        Assert.assertEquals("", 1, target.listQueues().size());
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

    @Test
    @Ignore
    public void can_create_2_queues() {
        // Given a queue service with a single queue
        // When creating queue with a new name
        // Then service contains 2 queues
    }

    @Test
    @Ignore
    public void can_create_5_queues() {
        // Given a queue service with 2 queues
        // When creating queue with a new name
        // Then service contains 3 queues
    }

    // Method: List<String> listQueues();

    @Test
    public void service_with_no_queues_returns_empty_list() {
        // Given a service with no queues

        // When list
        List<String> actualQueueNameList = target.listQueues();

        // Then list of queue names is empty
        Assert.assertEquals("Listing all queue names should be empty",
                true, actualQueueNameList.isEmpty());
    }

    @Test
    public void service_with_single_queue_returns_list_of_1() {
        // Given a service with a single queue
        setupFirstQueue();

        // When list
        List<String> result = target.listQueues();

        // Then list of queue names is size 1 with correct queue name
        Assert.assertEquals("Number of queues after creating a single queue",
                1, result.size()); // queue size is 1
        Assert.assertEquals("Newly created queue name exists in listing",
                true, result.contains(FIRST_QUEUE_NAME)); // queue's name equals the original queue name
    }

    @Test
    @Ignore
    public void service_with_2_queues_returns_list_of_2() {
        // Given a service with 2 queues
        // When list
        // Then list of queue names is size 2 with correct corresponding queue names
    }

    @Test
    @Ignore
    public void service_with_5_queues_returns_list_of_5() {
        // Given a service with 2 queues
        // When list
        // Then list of queue names is size 2 with correct corresponding queue names
    }

    // Method: String getQueueUrl(String queueName);

    @Test
    @Ignore
    public void cannot_lookup_url_for_service_with_no_queues() {
        
    }

    // Method: deleteQueue()

    @Test
    public void can_delete_queue_from_single_queue_service() {
        // Given a service with a single queue
        String queueUrlFixture = setupFirstQueue();

        // When deleteMessage queue with valid queue url
        boolean result = target.deleteQueue(queueUrlFixture);

        // Then list queue will not appear when queried
        Assert.assertTrue(result);

        List<String> list = target.listQueues();
        Assert.assertEquals("Deleted queue does is removed from the list of queue names",
                true, !list.contains(queueUrlFixture));
    }

    // BAD TEST
    @Test(expected = IllegalStateException.class)
    public void cannot_delete_non_existant_queue() {
        // Given a queue name
        String queueNameFixture = "NonExistant";

        // When deleteMessage name
        boolean result = target.deleteQueue(queueNameFixture);

        // Then result is illegal state exception
    }

    @Test(expected = NullPointerException.class)
    @Ignore
    public void cannot_delete_queue_with_null_as_name() {
        // Given a queue service with a name


    }

    @Test
    @Ignore
    public void cannot_delete_queue_with_empty_name() {
        //
    }

    // Method: push()

    @Test
    public void can_push_one_message() {
        // Given a queue url and message
        setupFirstQueue();

        // When push a message
        QueueMessage result = target.push(FIRST_QUEUE_URL, QUEUE_MESSAGE_1);

        // Then message object returned with a message id in addition to the original message body
        String actualMessageId = result.getMessageId();
        Assert.assertEquals("Message ID has been assigned with up to 100 chars",
                true, !actualMessageId.isEmpty() && (actualMessageId.length() <= 100));
        Assert.assertEquals("Message body of the pushed message equals that sent",
                QUEUE_MESSAGE_1, result.getMessageBody());
    }

    @Test(expected = NullPointerException.class)
    public void cannot_push_when_queueUrl_is_null() {
        // Given only a message body
        String messageFixture = QUEUE_MESSAGE_1;

        // When pushing a message with null url
        target.push(null, messageFixture);

        // Then illegal argument exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_push_when_queueUrl_is_empty() {
        // Given only a message body
        String emptyQueueUrlFixture = "";
        String messageFixture = QUEUE_MESSAGE_1;

        // When pushing a message with null url
        target.push(emptyQueueUrlFixture, messageFixture);

        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_push_when_message_is_null() {
        // Given only a message body
        String queueUrlFixture = FIRST_QUEUE_NAME;

        // When pushing a message with null url
        target.push(queueUrlFixture, null);

        // Then illegal argument exception
    }

    @Test
    @Ignore
    public void cannot_push_to_non_existant_queue() {
        // Given a queue service with queue A
        String queueNameAFixture = "MyQueueA";
        String queueNameBFixture = "MyQueueB";
        target.createQueue(queueNameAFixture);

        // When push a message to a queue B

    }

    // Method: pull()

    @Test
    public void can_pull_one_message() {
        // Given a queue with one message
        setupFirstQueue();
        target.push(FIRST_QUEUE_URL,  QUEUE_MESSAGE_1);

        // When pull a message
        QueueMessage actualMessage = target.pull(FIRST_QUEUE_URL);

        // Then pulled message is the same message
        Assert.assertNotNull(actualMessage);
    }

    // Method: deleteMessage()
}
