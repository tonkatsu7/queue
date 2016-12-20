package com.example;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.InvalidMessageContentsException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.UnsupportedOperationException;

import java.util.List;
import java.util.Set;

public class SqsQueueService implements QueueService {
  //
  // Task 4: Optionally implement parts of me.
  //
  // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
  // primarily so you can quickly assess your choices for method signatures in QueueService in
  // terms of how well they map to the implementation intended for a production environment.
  //

    // CONSTANTS
    private static long VISIBILITY_TIMEOUT_EMPTY;

    // Fields
    private AWSCredentials credentials;
    private AmazonSQSClient sqs;
    private String groupId;

    public SqsQueueService(AmazonSQSClient sqsClient) {
//        try {
//            credentials = new ProfileCredentialsProvider().getCredentials();
//        } catch (Exception e) {
//            throw new AmazonClientException(
//                    "Can't load the credentials from the credential profiles file. " +
//                    "Please make sure that your credentials file is at the correct " +
//                    "location (~/.aws/credentials), and is a in valid format.",
//                    e);
//        }
//        sqs = new AmazonSQSClient(credentials);
//        sqs.setEndpoint("https://sqs.us-east-2.amazonaws.com");
    }

    @Override
    public String createQueue(String queueName) {
//        // Create a FIFO queue
//        System.out.println("Creating a new Amazon SQS FIFO queue called MyFifoQueue.fifo.\n");
//        Map<String, String> attributes = new HashMap<String, String>();
//        // A FIFO queue must have the FifoQueue attribute set to True
//        attributes.put("FifoQueue", "true");
//        // Generate a MessageDeduplicationId based on the content, if the user doesn't provide a MessageDeduplicationId
//        attributes.put("ContentBasedDeduplication", "true");
//        // The FIFO queue name must end with the .fifo suffix
//        CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyFifoQueue.fifo").withAttributes(attributes);
//        String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
//
//        // List queues
//        System.out.println("Listing all queues in your account.\n");
//        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
//            System.out.println("  QueueUrl: " + queueUrl);
//        }
//        System.out.println();
        return null;
    }

    @Override
    public Set<String> listQueues() {
//        // List queues
//        System.out.println("Listing all queues in your account.\n");
//        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
//            System.out.println("  QueueUrl: " + queueUrl);
//        }
//        System.out.println();
        return null;
    }

    @Override
    public String getQueueUrl(String queueName) {
        return sqs.getQueueUrl(queueName).getQueueUrl();
    }

    @Override
    public void deleteQueue(String queueUrl) {
        // Delete the queue
        sqs.deleteQueue(new DeleteQueueRequest(queueUrl));
    }

    @Override
    public QueueMessage push(String queueUrl, String message) {
        // Send a message
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, message);
        // You must provide a non-empty MessageGroupId when sending messages to a FIFO queue
        sendMessageRequest.setMessageGroupId(groupId);
        // Send message
        SendMessageResult sendMessageResult;
        try {
            sendMessageResult = sqs.sendMessage(sendMessageRequest);
        } catch (InvalidMessageContentsException e) {
            throw new IllegalArgumentException(e);
        } catch (UnsupportedOperationException e) {
            throw new java.lang.UnsupportedOperationException(e); // TODO add to interface
        }
        // If successful, return message id
        return new QueueMessage(message, sendMessageResult.getMessageId());
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        // Receive messages
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        // Set messages to fetch to 1
        receiveMessageRequest.setMaxNumberOfMessages(1);
        // Receive message
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

        if (messages.size() == 0) {
            return new QueueMessage(); // empty message
        } else if (messages.size() > 1) {
            throw new IllegalStateException("Requested 1 message from SQS but received " + messages.size());
        }

        QueueMessage result = new QueueMessage();

        for (Message message : messages) {
            return new QueueMessage(new QueueMessage(message.getBody(), message.getMessageId()),
                    message.getReceiptHandle(), VISIBILITY_TIMEOUT_EMPTY); // whoops, see notes // TODO
        }

        return result;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        // Delete the message
        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptId));
        return true; // whoops, see notes // TODO remove from interface
    }
}
