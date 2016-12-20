package com.example;

import static com.google.common.base.Preconditions.*;

public class QueueMessage {
    private static String EMPTY = "";

    private String messageBody; // RFC1321, 256KB
    private String messageId; // 100 char
    private String receiptId; // 1024 char
//    private String deduplicationId; // optional, 128 char, [a-zA-Z0-9!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~]
//    private String groupId; // same as deduplicationId, required by SQS for FIFO
    private long visibilityTimeoutFrom;

    /**
     * Intended for when pull operation receives no message or when push is unable to
     * send a message
     */
    public QueueMessage() {
        messageBody = EMPTY;
        messageId = EMPTY;
        receiptId = EMPTY;
        visibilityTimeoutFrom = 0;
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
        this.messageBody = QueueServiceUtil.checkMessageBody(messageBody);
        this.messageId = QueueServiceUtil.checkMessageId(messageId);
        this.receiptId = EMPTY;
        this.visibilityTimeoutFrom = 0;
    }

    public QueueMessage(String messageBody, String messageId, String receiptId) {
        this(messageBody, messageId);
        this.receiptId = QueueServiceUtil.checkReceiptId(receiptId);
        this.visibilityTimeoutFrom = 0;
    }

    /**
     * Intended to be used when a message has been pulled successfully from the queue.
     *
     * @param dequeued
     * @param receiptId the receipt ID of a pulled message
     * @param visibilityTimeoutFrom
     * @throws NullPointerException if the specified receipt ID is null
     * @throws IllegalArgumentException if the specified receipt ID is an empty string
     */
    public QueueMessage(QueueMessage dequeued, String receiptId, long visibilityTimeoutFrom) {
        this(checkNotNull(dequeued, "Dequeued cannot be null").getMessageBody(),
                dequeued.getMessageId());

        this.receiptId = QueueServiceUtil.checkReceiptId(receiptId);

        checkArgument(visibilityTimeoutFrom >= 0, "Visibility timeout from cannot be a negative number");
        this.visibilityTimeoutFrom = visibilityTimeoutFrom;
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

    protected long getVisibilityTimeoutFrom() { return visibilityTimeoutFrom; }

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
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueueMessage))
            return false;

        QueueMessage other = (QueueMessage) obj;

        return other.getMessageId().equalsIgnoreCase(messageId) && other.getMessageBody().equals(messageBody);
    }

    @Override
    public String toString() {
        return "QueueMessage{" +
                "messageBody='" + messageBody + '\'' +
                ", messageId='" + messageId + '\'' +
                ", receiptId='" + receiptId + '\'' +
                ", visibilityTimeoutFrom=" + visibilityTimeoutFrom +
                '}';
    }
}
