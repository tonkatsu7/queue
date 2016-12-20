package com.example;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class QueueServiceUtilTest {
    private static final String QUEUE_NAME = "MyQueue";
    private static final String QUEUE_URL = "http://sqs.us-east-2.amazonaws.com/123456789012/MyQueue";
    private static final String MESSAGE_BODY_1 = "My message body";
    private static final String MESSAGE_ID_1 = "5fea7756-0ea4-451a-a703-a558b933e274";
    private static final String RECEIPT_ID_1 = "MbZj6wDWli+JvwwJaBV+3dcjk2YW2vA3+STFFljTM8tJJg6HRG6PYSasuWXPJB+CwLj1FjgXUv1uSj1gUPAWV66FU/WeR4mq2OKpEGYWbnLmpRCJVAyeMjeU5ZBdtcQ+QEauMZc8ZRv37sIW2iJKq3M9MFx1YvV11A2x/KSbkJ0=";
    private static final String EMPTY_STRING = " ";

    // Method: String checkQueueName(String queueName)

    @Test
    public void valid_queue_name() {
        // Given a valid queue name
        // When check
        String actual = QueueServiceUtil.checkQueueName(QUEUE_NAME);

        // Then queue name returned
        Assert.assertEquals("Successful queue name check returns the queue name value", QUEUE_NAME, actual);
    }

    @Test
    @Ignore
    public void invalid_queue_name() {
        // Given an invalid queue name
        // When check
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void queue_name_cannot_be_null() {
        // Given a null queue name
        // When check
        QueueServiceUtil.checkQueueName(null);

        // The null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void queue_name_cannot_be_empty() {
        // Given a null queue name
        // When check
        QueueServiceUtil.checkQueueName(EMPTY_STRING);

        // The illegal exception
    }

    // Method: String checkQueueUrl(String queueUrl)

    @Test
    public void valid_queue_url() {
        // Given a valid queue url
        // When check
        String actual = QueueServiceUtil.checkQueueUrl(QUEUE_URL);

        // Then queue name returned
        Assert.assertEquals("Successful queue URL check returns the queue URL value", QUEUE_URL, actual);
    }

    @Test
    @Ignore
    public void invalid_queue_url() {
        // Given an invalid queue url
        // When check
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void queue_url_cannot_be_null() {
        // Given a null queue name
        // When check
        QueueServiceUtil.checkQueueUrl(null);

        // The null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void queue_url_cannot_be_empty() {
        // Given a null queue name
        // When check
        QueueServiceUtil.checkQueueUrl(EMPTY_STRING);

        // The illegal exception
    }

    // Method: String checkMessageBody(String message)

    @Test
    public void valid_message_body() {
        // Given a valid message body
        // When check
        String actual = QueueServiceUtil.checkMessageBody(MESSAGE_BODY_1);

        // Then queue name returned
        Assert.assertEquals("Successful message body check returns the message body value", MESSAGE_BODY_1, actual);
    }

    @Test
    @Ignore
    public void invalid_message_body() {
        // Given an invalid message body
        // When check
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void message_body_cannot_be_null() {
        // Given a null message body
        // When check
        QueueServiceUtil.checkMessageBody(null);

        // The null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void message_body_cannot_be_empty() {
        // Given a null message body
        // When check
        QueueServiceUtil.checkMessageBody(EMPTY_STRING);

        // The illegal exception
    }

    // Method: String checkMessageId(String messageId)

    @Test
    public void valid_message_id() {
        // Given a valid message id
        // When check
        String actual = QueueServiceUtil.checkMessageId(MESSAGE_ID_1);

        // Then queue name returned
        Assert.assertEquals("Successful message id check returns the message id value", MESSAGE_ID_1, actual);
    }

    @Test
    @Ignore
    public void invalid_message_id() {
        // Given an invalid message id
        // When check
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void message_id_cannot_be_null() {
        // Given a null message id
        // When check
        QueueServiceUtil.checkMessageId(null);

        // The null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void message_id_cannot_be_empty() {
        // Given a null message id
        // When check
        QueueServiceUtil.checkMessageId(EMPTY_STRING);

        // The illegal exception
    }
    
    // Method: String checkReceiptId(String receiptId)

    @Test
    public void valid_receipt_id() {
        // Given a valid receipt id
        // When check
        String actual = QueueServiceUtil.checkReceiptId(RECEIPT_ID_1);

        // Then queue name returned
        Assert.assertEquals("Successful receipt id check returns the receipt id value", RECEIPT_ID_1, actual);
    }

    @Test
    @Ignore
    public void invalid_receipt_id() {
        // Given an invalid receipt id
        // When check
        // Then illegal argument exception
    }

    @Test(expected = NullPointerException.class)
    public void receipt_id_cannot_be_null() {
        // Given a null receipt id
        // When check
        QueueServiceUtil.checkReceiptId(null);

        // The null pointer exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void receipt_id_cannot_be_empty() {
        // Given a null receipt id
        // When check
        QueueServiceUtil.checkReceiptId(EMPTY_STRING);

        // The illegal exception
    }

    // Method: String fromUrl(String queueUrl)

    // Happy test

    // Unhappy test
}
