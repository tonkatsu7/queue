package com.example;

import java.util.List;

public class FileQueueService implements QueueService {
  //
  // Task 3: Implement me if you have time.
  //


    @Override
    public String createQueue(String queueName) {
        return null;
    }

    @Override
    public List<String> listQueues() {
        return null;
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
    public boolean deleteMessage(String queueUrl, String receiptId) {
        return false;
    }
}
