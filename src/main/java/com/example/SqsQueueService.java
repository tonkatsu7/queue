package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;

import java.util.List;

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
