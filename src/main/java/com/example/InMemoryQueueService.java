package com.example;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class InMemoryQueueService implements QueueService {
  //
  // Task 2: Implement me.
  //

    @Override
    public QueueAttributes createQueue(String queueName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(queueName), "Queue name argument cannot be null or an empty string");

        QueueAttributes queue = new QueueAttributes();
        queue.setQueueName(queueName);
        queue.setQueueUrl(queueName);

        return queue;
    }

    @Override
    public QueueAttributes createQueue(QueueAttributes queueAttributes) {
        return null;
    }

    @Override
    public List<QueueAttributes> listQueues() {
        List<QueueAttributes> queueList = new ArrayList<QueueAttributes>();
        return queueList;
    }

    @Override
    public String getQueueUrl(String queueName) {
        return null;
    }

    @Override
    public boolean deleteQueue(String queueUrl) {
        return false;
    }

    @Override
    public QueueMessage push(String queueUrl, String message) {
        return null;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        return null;
    }

    @Override
    public boolean delete(String queueUrl, String receiptId) {
        return false;
    }

    @Override
    public boolean changeMessageVisibilitiy(String queueUrl, String receiptId, Integer visibilityTimeout) {
        return false;
    }
}
