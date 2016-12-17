package com.example;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

/**
 * Created by sipham on 15/12/16.
 */
public class QueueMessage {
    private static String EMPTY = "";

    private String messageBody; // RFC1321, 256KB
    private String messageId; // 100 char
    private String receiptId; // 1024 char
//    private String deduplicationId; // optional, 128 char, [a-zA-Z0-9!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~]
//    private String groupId; // same as deduplicationId, required by SQS for FIFO

    /**
     * Intended for when pull operation receives no message or when push is unable to
     * send a message
     */
    public QueueMessage() {
        messageBody = EMPTY;
        messageId = EMPTY;
        receiptId = EMPTY;
    }

    /**
     * Intended to be used when a message has been pushed successfully to the queue.
     *
     * @param messageBody the message body of the pushed queue message
     * @param messageId the message id of the pushed queue message
     * @throws NullPointerException if the specified message body or message ID is null
     * @throws IllegalArgumentException if the specified message body or message ID is
     *         an empty string
     */
    public QueueMessage(String messageBody, String messageId) {
        checkNotNull(messageBody, "Message body cannot be null");
        checkArgument(!messageBody.trim().isEmpty(), "Message body cannot be an empty string");
        checkNotNull(messageId, "Message ID cannot be null");
        checkArgument(!messageId.trim().isEmpty(), "Message ID cannot be an empty string");

        this.messageBody = messageBody;
        this.messageId = messageId;
        this.receiptId = EMPTY;
    }

    /**
     * Intended to be used when a message has been pulled successfully from the queue.
     *
     * @param dequeued
     * @param receiptId the receipt ID of a pulled message
     * @throws NullPointerException if the specified receipt ID is null
     * @throws IllegalArgumentException if the specified receipt ID is an empty string
     */
    public QueueMessage(QueueMessage dequeued, String receiptId) {
        this(checkNotNull(dequeued, "Dequeued cannot be null").getMessageBody(),
                dequeued.getMessageId());
        checkNotNull(receiptId, "Receipt ID cannot be null");
        checkArgument(!receiptId.trim().isEmpty(), "Receipt ID cannot be an empty string");

        this.receiptId = receiptId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    /**
     * @return if queue message is empty
     */
    public boolean isEmpty() {
        return messageId.equals(EMPTY);
    }

    /**
     *
     * @param obj the other queue message to test equality
     * @return true if both message bodies are equal
     */
//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof QueueMessage))
//            return false;
//
//        QueueMessage other = (QueueMessage) obj;
//
//        return other.getMessageId().equalsIgnoreCase(messageId) && other.getMessageBody().equals(messageBody);
//        return false;
//    }
}
