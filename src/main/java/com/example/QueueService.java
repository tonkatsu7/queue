package com.example;

import java.util.List;

public interface QueueService {

  //
  // Task 1: Define me.
  //
  // This interface should include the following methods.  You should choose appropriate
  // signatures for these methods that prioritise simplicity of implementation for the range of
  // intended implementations (in-memory, file, and SQS).  You may include additional methods if
  // you choose.
  //
  // - push
  //   pushes a message onto a queue.
  // - pull
  //   retrieves a single message from a queue.
  // - delete
  //   deletes a message from the queue that was received by pull().
  //

    // CreateQueueResult	createQueue(String queueName)
    QueueAttributes createQueue(String queueName);

    // CreateQueueResult	createQueue(CreateQueueRequest createQueueRequest)
    QueueAttributes createQueue(QueueAttributes queueAttributes);

    // ListQueuesResult	listQueues()
    List<QueueAttributes> listQueues();

    // GetQueueUrlResult	getQueueUrl(String queueName)
    String getQueueUrl(String queueName);

    // DeleteQueueResult	deleteQueue(String queueUrl)
    boolean deleteQueue(String queueUrl);

    // SendMessageResult	sendMessage(String queueUrl, String messageBody)
    QueueMessage push(String queueUrl, String message);

    // ReceiveMessageResult	receiveMessage(String queueUrl)
    QueueMessage pull(String queueUrl);

    // DeleteMessageResult	deleteMessage(String queueUrl, String receiptHandle)
    boolean delete(String queueUrl, String receiptId);

    // ChangeMessageVisibilityResult	changeMessageVisibility(String queueUrl, String receiptHandle, Integer visibilityTimeout)
    boolean changeMessageVisibilitiy(String queueUrl, String receiptId, Integer visibilityTimeout);
}
