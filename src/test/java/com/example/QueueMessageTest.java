package com.example;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;

/**
 * Created by sipham on 15/12/16.
 */
public class QueueMessageTest {
    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = null;

    private static final String MESSAGE_BODY_1 = "My message body";
    private static final String MESSAGE_ID_1 = "5fea7756-0ea4-451a-a703-a558b933e274";
    private static final String RECEIPT_ID_1 = "MbZj6wDWli+JvwwJaBV+3dcjk2YW2vA3+STFFljTM8tJJg6HRG6PYSasuWXPJB+CwLj1FjgXUv1uSj1gUPAWV66FU/WeR4mq2OKpEGYWbnLmpRCJVAyeMjeU5ZBdtcQ+QEauMZc8ZRv37sIW2iJKq3M9MFx1YvV11A2x/KSbkJ0=";
    private static final long VISIBILITY_TIMEOUT = 0;

    private QueueMessage target;

    private QueueMessage setupEmptyQueueMessage() {
        return new QueueMessage();
    }

    private QueueMessage setupPushedQueueMessage() {
        return new QueueMessage(MESSAGE_BODY_1, MESSAGE_ID_1);
    }

    private QueueMessage setupPulledQueueMessage() {
        return new QueueMessage(setupPushedQueueMessage(), RECEIPT_ID_1, VISIBILITY_TIMEOUT); // TODO
    }

    // Constructor: QueueMessage()

    @Test
    public void can_instantiate_empty_queue_message() {
        // Given nothing

        // When calling no args constructor
        target = new QueueMessage();

        // Then the message is empty if all fields are empty strings
        Assert.assertEquals("Test target is not null when calling constructor", true, !Objects.isNull(target));
        Assert.assertEquals("Empty queue message should have an empty message body", EMPTY_STRING, target.getMessageBody());// message body is empty
        Assert.assertEquals("Empty queue message should have an empty message ID", EMPTY_STRING, target.getMessageId());// message id is empty
        Assert.assertEquals("Empty queue message should have anempty receipt ID", EMPTY_STRING, target.getReceiptId());// receipt id is empty
    }

    // Constructor: QueueMessage(String messageBody, String messageId)

    @Test
    public void can_instantiate_pushed_queue_message_happy() {
        // Given a valid message body and id strings

        // When calling the (String, String) message constructor
        target = new QueueMessage(MESSAGE_BODY_1, MESSAGE_ID_1);

        // Then the string is assigned to the message body and message id fields
        Assert.assertEquals("Test target is not null when calling constructor", true, !Objects.isNull(target));
        Assert.assertEquals("Message body field after queue message instantiation", MESSAGE_BODY_1, target.getMessageBody());
        Assert.assertEquals("Message ID field after queue message instantiation", MESSAGE_ID_1, target.getMessageId());
        Assert.assertEquals("Receipt ID field after queue message instantiation should be empty", EMPTY_STRING, target.getReceiptId());
    }

    @Test(expected = NullPointerException.class)
    public void cannot_instantiate_pushed_message_with_null_body() {
        // Given a null message body but valid message id string

        // When calling the (String, String) args constructor
        target = new QueueMessage(NULL_STRING, MESSAGE_ID_1);

        // Then a null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_instantiate_pushed_message_with_empty_body() {
        // Given an empty message body but valid message id string

        // When calling the (String, String) args constructor
        target = new QueueMessage(EMPTY_STRING, MESSAGE_ID_1);

        // Then a null pointer exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_instantiate_pushed_message_with_null_message_id() {
        // Given a null message body but valid message id string

        // When calling the (String, String) args constructor
        target = new QueueMessage(MESSAGE_BODY_1, NULL_STRING);

        // Then a null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_instantiate_pushed_message_with_empty_message_id() {
        // Given an empty message body but valid message id string

        // When calling the (String, String) args constructor
        target = new QueueMessage(MESSAGE_BODY_1, EMPTY_STRING);

        // Then a null pointer exception
    }

    // Constructor: QueueMessage(QueueMessage dequeued, String receiptId)

    @Test
    public void can_instantiate_pulled_queue_message_happy() {
        // Given a valid message body, message id and receipt id strings
        QueueMessage dequeuedMessageFixture = setupPushedQueueMessage();

        // When calling the (QueueMessage, String) messaged constructor
        target = new QueueMessage(dequeuedMessageFixture, RECEIPT_ID_1, VISIBILITY_TIMEOUT);

        // Then the string is assigned to the message body, message id and receipt id fields
        Assert.assertEquals("Test target is not null when calling constructor", true, !Objects.isNull(target));
        Assert.assertEquals("Message body field after queue message instantiation", MESSAGE_BODY_1, target.getMessageBody());
        Assert.assertEquals("Message ID field after queue message instantiation", MESSAGE_ID_1, target.getMessageId());
        Assert.assertEquals("Receipt ID field after queue message instantiation should be empty", RECEIPT_ID_1, target.getReceiptId());
    }

    @Test(expected = NullPointerException.class)
    public void cannot_instantiate_pulled_queue_message_with_null_dequeued() {
        // Given a null dequeued queue message object but valid receipt id
        QueueMessage nullQueueMessageFixture = null;

        // When calling the (QueueMessage, String) messaged constructor
        target = new QueueMessage(nullQueueMessageFixture, RECEIPT_ID_1, VISIBILITY_TIMEOUT);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_instantiate_pulled_queue_message_with_empty_dequeued() {
        // Given a null dequeued queue message object but valid receipt id
        QueueMessage emptyQueueMessageFixture = setupEmptyQueueMessage();

        // When calling the (QueueMessage, String) messaged constructor
        target = new QueueMessage(emptyQueueMessageFixture, RECEIPT_ID_1, VISIBILITY_TIMEOUT);

        // Then null pointer exception
    }

    @Test(expected = NullPointerException.class)
    public void cannot_instantiate_pulled_queue_message_with_null_receipt_id() {
        // Given a valid dequeued queue message object but null receipt id
        QueueMessage dequeuedMessageFixture = setupPushedQueueMessage();

        // When calling the (QueueMessage, String) messaged constructor
        target = new QueueMessage(dequeuedMessageFixture, NULL_STRING, VISIBILITY_TIMEOUT);

        // Then null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannot_instantiate_pulled_queue_message_with_empty_receipt_id() {
        // Given a valid dequeued queue message object but null receipt id
        QueueMessage dequeuedMessageFixture = setupPushedQueueMessage();

        // When calling the (QueueMessage, String) messaged constructor
        target = new QueueMessage(dequeuedMessageFixture, EMPTY_STRING, VISIBILITY_TIMEOUT);

        // Then null pointer exception
    }

    // TODO visibility timeout is +1, 0, -1

    // Getter: String getMessageBody() has no logic so we won't test

    // Getter: String getMessageId() has no logic so we won't test

    // Getter: String getReceiptId() has no logic so we won't test

    // Method: boolean isEmpty()

    @Test
    public void empty_message_is_empty() {
        // Given an empty queue message object
        target = new QueueMessage();

        // When testing isEmpty
        boolean actual = target.isEmpty();

        // Then true
        Assert.assertEquals("Queue message object is empty", true, actual);
    }

    @Test
    public void pushed_queue_message_is_not_empty() {
        // Given a valid pushed queue message
        target = setupPushedQueueMessage();

        // When testing isEmpty
        boolean actual = target.isEmpty();

        // Then false
        Assert.assertEquals("Queue message object is empty", false, actual);
    }

    @Test
    public void pulled_queue_message_is_not_empty() {
        // Given a valid pulled queue message
        target = setupPulledQueueMessage();

        // When testing isEmpty
        boolean actual = target.isEmpty();

        // Then false
        Assert.assertEquals("Queue message object is empty", false, actual);
    }

    // Method: boolean equals(Object obj)

    // empty is equal to empty

    // pushed is equal to pushed if body==body and id==id

    // pushed is not equal if body!=body but id==id

    // pushed is not equal if body==body but id!=id

    // pulled is equal to pulled

    // pulled is equal if


}
