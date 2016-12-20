package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.InvalidMessageContentsException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDeletedRecentlyException;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.UnsupportedOperationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private AmazonSQSClient sqs;
    private String groupId;

    public SqsQueueService(AmazonSQSClient sqsClient, String groupId) {
        this.sqs = sqsClient;
        this.groupId = groupId;
    }

    @Override
    public String createQueue(String queueName) {
        // Create a FIFO queue
        Map<String, String> attributes = new HashMap<String, String>();
        // A FIFO queue must have the FifoQueue attribute set to True
        attributes.put("FifoQueue", "true");
        // Generate a MessageDeduplicationId based on the content, if the user doesn't provide a MessageDeduplicationId
        attributes.put("ContentBasedDeduplication", "true");
        // The FIFO queue name must end with the .fifo suffix
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName).withAttributes(attributes);
        String queueUrl;
        try {
            queueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        } catch (QueueDeletedRecentlyException e) {
            throw new IllegalStateException(e); // TODO add to interface
        } catch (QueueNameExistsException e) {
            throw new IllegalStateException("Unexpected duplicate queue name error in SqsQueueService method createQueue", e); // TODO add to interface
        }
        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
        // List queues
        return new HashSet<>(sqs.listQueues().getQueueUrls());
    }

    @Override
    public String getQueueUrl(String queueName) {
        try {
            return sqs.getQueueUrl(queueName).getQueueUrl();
        } catch (QueueDoesNotExistException e) {
            throw new IllegalStateException(e);
        }
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
                    message.getReceiptHandle(), VISIBILITY_TIMEOUT_EMPTY); // whoops, see notes // TODO add notes
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
