package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;

import java.util.Set;

public class SqsQueueService implements QueueService {
  //
  // Task 4: Optionally implement parts of me.
  //
  // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
  // primarily so you can quickly assess your choices for method signatures in QueueService in
  // terms of how well they map to the implementation intended for a production environment.
  //

    public SqsQueueService(AmazonSQSClient sqsClient) {
    }

    @Override
    public String createQueue(String queueName) {
    return null;
    }

    @Override
    public Set<String> listQueues() {
    return null;
    }

    @Override
    public String getQueueUrl(String queueName) {
    return null;
    }

    @Override
    public void deleteQueue(String queueUrl) {

    }

    @Override
    public QueueMessage push(String queueUrl, String message) {
//        // Send a message
//        System.out.println("Sending a message to MyFifoQueue.fifo.\n");
//        SendMessageRequest sendMessageRequest = new SendMessageRequest(myQueueUrl, "This is my message text.");
//        // You must provide a non-empty MessageGroupId when sending messages to a FIFO queue
//        sendMessageRequest.setMessageGroupId("messageGroup1");
//        // Uncomment the following to provide the MessageDeduplicationId
//        //sendMessageRequest.setMessageDeduplicationId("1");
//        SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
//        String sequenceNumber = sendMessageResult.getSequenceNumber();
//        String messageId = sendMessageResult.getMessageId();
//        System.out.println("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
        return null;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
//        // Receive messages
//        System.out.println("Receiving messages from MyFifoQueue.fifo.\n");
//        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
//        // Uncomment the following to provide the ReceiveRequestDeduplicationId
//        //receiveMessageRequest.setReceiveRequestAttemptId("1");
//        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
//        for (Message message : messages) {
//        System.out.println("  Message");
//        System.out.println("    MessageId:     " + message.getMessageId());
//        System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//        System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//        System.out.println("    Body:          " + message.getBody());
//        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
//        System.out.println("  Attribute");
//        System.out.println("    Name:  " + entry.getKey());
//        System.out.println("    Value: " + entry.getValue());
//        }
//        }
//        System.out.println();
        return null;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
//        // Delete the message
//        System.out.println("Deleting the message.\n");
//        String messageReceiptHandle = messages.get(0).getReceiptHandle();
//        sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));
        return false;
    }
}
