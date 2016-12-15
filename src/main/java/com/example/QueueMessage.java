package com.example;

/**
 * Created by sipham on 15/12/16.
 */
public class QueueMessage {

    private String messageBody; // RFC1321, 256KB
    private String messageId; // 100 char
    private String receiptId; // 1024 char
    private String deduplicationId; // optional, 128 char, [a-zA-Z0-9!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~]
    private String groupId; // same as deduplicationId

    public QueueMessage(String messageBody) {

    }

    public QueueMessage(String messageBody, String messageId) {

    }

    public QueueMessage(String messageBody, String messageId, String receiptId) {

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

    public String getDeduplicationId() {
        return deduplicationId;
    }

    public String getGroupId() {
        return groupId;
    }
}
