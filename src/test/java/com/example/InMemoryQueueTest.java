package com.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.List;

public class InMemoryQueueTest {
  //
  // Implement me.
  //

    InMemoryQueueService target = null;

    @Before
    public void setup() {
        target = new InMemoryQueueService();
    }

    @Test
    @Ignore
    public void can_create_queue_from_queue_attributes() {
        // Given explicit queue properties
        // - queue name
        // - delay
        // - visibility timeout
        // - redrive max receives
        // - redrive target
        String queueNameFixture = "MyQueue";
        String redriveTargetFixture = "DeadLetterQueue";
        QueueAttributes propertiesFixture = new QueueAttributes();
        propertiesFixture.setQueueName(queueNameFixture);
        propertiesFixture.setDelaySeconds(0);
        propertiesFixture.setVisibilityTimeoutSeconds(30);
        propertiesFixture.setRedrivePolicyMaxRecieveCount(3);
        propertiesFixture.setRedrivePolicyDeadLetterTargetUrl(redriveTargetFixture);

        // When create queue
        QueueAttributes result = target.createQueue(propertiesFixture);

        // Then the queue returned has the same properties
        Assert.assertNotNull(result);
        // - queue name
        // - delay
        // - visibility timeout
        // - redrive max receives
        // - redrive target
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_create_queue_with_null_queue_attributes() {}

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void cannot_create_queue_with_negative_delay() {}

    @Test
    @Ignore
    public void can_create_queue_from_name() {
        // Given a queue name
        String queueNameFixture = "MyQueue";
        // Given default values for
        // - delay seconds
        // - visibility timeout
        // - redrive max receives
        // - redrive target

        // When create queue
        QueueAttributes result = target.createQueue(queueNameFixture);

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(queueNameFixture, result.getQueueName()); // the original queue name
        Assert.assertEquals(queueNameFixture, result.getQueueUrl()); // in memory queue is not addressible via url so just the original queue name
        // delay seconds
        // visibility timeout
        // redrive max receives
        // redrive target
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_create_queue_with_null_name() {
        // Given a null queue name
        String queueNameFixture = null;

        // When create queue
        target.createQueue(queueNameFixture);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_create_queue_with_empty_name() {
        // Given an empty queue name
        String queueNameFixture = "";

        // When create queue
        target.createQueue(queueNameFixture);

        // Then illegal argument exception
    }

    @Test
    public void can_list_service_no_queues() {
        List<QueueAttributes> result = target.listQueues();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    @Ignore
    public void can_list_service_single_queue() {
        // Given a queue name
        String queueNameFixture = "MyQueue";
        // create a queue
        target.createQueue(queueNameFixture);

        // When retrieve a list of queues
        List<QueueAttributes> result = target.listQueues();

        // Then
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size()); // queue size is 1
        // first queue's name equals the original queue name
    }

    @Test
    @Ignore
    public void can_delete_queue() {

    }

    @Test
    @Ignore
    public void can_push_one_message() {

        String queueUrlFixture = "http://sqs.us-east-2.amazonaws.com/123456789012/MyQueue";
        String messageFixture = "MyQueue";

        QueueMessage result = target.push(queueUrlFixture, messageFixture);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getMessageId());
        // result is not null
        // result message if not null or empty string
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void test_push_queueUrl_is_null() {
        String messageFixture = "My queue message";
        target.push(null, messageFixture);
    }

    @Test
    @Ignore
    public void test_push_message_is_null() {

    }


}
