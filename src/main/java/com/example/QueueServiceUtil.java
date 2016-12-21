package com.example;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class QueueServiceUtil {
    protected static final String QUEUE_NAME_CANNOT_BE_NULL = "Queue name cannot be null";
    protected static final String QUEUE_NAME_CANNOT_BE_EMPTY = "Queue name cannot be an empty string";
    protected static final String QUEUE_NAME_DOES_NOT_EXIST = "Queue name does not exist";

    protected static final String QUEUE_URL_CANNOT_BE_NULL = "Queue URL cannot be null";
    protected static final String QUEUE_URL_CANNOT_BE_EMPTY = "Queue URL cannot be an empty string";
    protected static final String QUEUE_URL_DOES_NOT_EXIST = "Queue URL does not exist";

    protected static final String MESSAGE_BODY_CANNOT_BE_NULL = "Message body cannot be null";
    protected static final String MESSAGE_BODY_CANNOT_BE_EMPTY = "Message body cannot be an empty string";

    protected static final String RECEIPT_ID_CANNOT_BE_NULL = "Receipt ID cannot be null";
    protected static final String RECEIPT_ID_CANNOT_BE_EMPTY = "Receipt ID cannot be an empty string";

    protected static final String MESSAGE_ID_CANNOT_BE_NULL = "Message ID cannot be null";
    protected static final String MESSAGE_ID_CANNOT_BE_EMPTY = "Message ID cannot be an empty string";


    public static String checkQueueName(String queueName) {
        checkNotNull(queueName, QUEUE_NAME_CANNOT_BE_NULL);
        checkArgument(!queueName.trim().isEmpty(), QUEUE_NAME_CANNOT_BE_EMPTY);
        return queueName;
    }

    public static String checkQueueUrl(String queueUrl) {
        checkNotNull(queueUrl, QUEUE_URL_CANNOT_BE_NULL);
        checkArgument(!queueUrl.trim().isEmpty(), QUEUE_URL_CANNOT_BE_EMPTY);
        return queueUrl;
    }

    public static String checkMessageBody(String message) {
        checkNotNull(message, MESSAGE_BODY_CANNOT_BE_NULL);
        checkArgument(!message.trim().isEmpty(), MESSAGE_BODY_CANNOT_BE_EMPTY);
        return message;
    }

    public static String checkMessageId(String messageId) {
        checkNotNull(messageId, MESSAGE_ID_CANNOT_BE_NULL);
        checkArgument(!messageId.trim().isEmpty(), MESSAGE_ID_CANNOT_BE_EMPTY);
        return messageId;
    }

    public static String checkReceiptId(String receiptId) {
        checkNotNull(receiptId, RECEIPT_ID_CANNOT_BE_NULL);
        checkArgument(!receiptId.trim().isEmpty(), RECEIPT_ID_CANNOT_BE_EMPTY);
        return receiptId;
    }

    public static String generateMessageId() {
        return UUID.randomUUID().toString();
    }

    public static String generateReceiptId() {
        return UUID.randomUUID().toString();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long nowNano() {
        return System.nanoTime();
    }
}
