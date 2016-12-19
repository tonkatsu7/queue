package com.example;

import java.io.IOException;
import java.util.Set;

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
  // - deleteMessage
  //   deletes a message from the queue that was received by pull().
  //

    /**
     * Maps to {@code CreateQueueResult	createQueue(String queueName)}
     * in SQS.
     *
     * <p>Creates a new message queue with the specified queue name and
     * returns the URL of the queue which is used to all other queue
     * and message operations.
     *
     * <p>Calling this operation on an existing queue will simply
     * return the URL (the SQS QueueAlreadyExists error is characterised
     * by a request for the same name but different attributes - since
     * name is the only configurable attribute then we'll regard
     * subsequent attempts to create the same queue as consistent).
     *
     * @param queueName the common name for the queue
     * @return the URL of the newly created queue which is used in all
     *         other queue and message methods
     * @throws NullPointerException if the specified queue name is null
     * @throws IllegalArgumentException if the specified queue name is
     *         an empty string
     */
    String createQueue(String queueName);

    /**
     * Maps to {@code ListQueuesResult	listQueues()} in SQS.
     *
     * <p>Fetches a list of all the the queues URLs. The queue URL can
     * be used with message operations.
     *
     * @return a list of queue names
     */
    Set<String> listQueues();

    /**
     * Maps to {@code GetQueueUrlResult	getQueueUrl(String queueName)}
     * in SQS.
     *
     * <p>Gets the queueUrl for the specified queue name. The queue URL
     * is used with message operations.
     *
     * <p>
     *
     * @param queueName the common name of the queue
     * @return the queue URL
     * @throws NullPointerException if the specified queue name is null
     * @throws IllegalArgumentException is the specified name is an
     *         empty string
     * @throws IllegalStateException if the specified queue name does
     *         not exist
     */
    String getQueueUrl(String queueName);

    /**
     * Maps to {@code DeleteQueueResult	deleteQueue(String queueUrl)} in
     * SQS.
     *
     * <p>Deletes a queue with the specified queue URL even if the queue
     * is empty. Even if the queue URL doesn't exist a successful result
     * is returned.
     *
     * <p>Expect some time for this operation to occur - this is consistent
     * with SQS behaviour. Any messages remaining in the queue will be lost.
     *
     * @param queueUrl the queue URL
     * @throws NullPointerException if the specified queue URL is null
     * @throws IllegalArgumentException is the specified queue URL is an
     *         empty string
     */
    void deleteQueue(String queueUrl);

    /**
     * Maps to {@code SendMessageResult	sendMessage(String queueUrl, String messageBody)}
     * in SQS.
     *
     * <p>Pushes a message to the specified queue.
     *
     * @param queueUrl the queue URL
     * @param message the message body
     * @return a queue message containing the message body and a system
     *         assigned message ID to indicate that the message was
     *         successfully accepted
     * @throws NullPointerException if either the specified queue URL or
     *         message body is null
     * @throws IllegalArgumentException if either the specifed queue URL
     *         or message body is empty
     * @throws IllegalStateException if the specified queue URL does not
     *         exist
     */
    QueueMessage push(String queueUrl, String message);

    /**
     * Maps to {@code ReceiveMessageResult	receiveMessage(String queueUrl)}
     * in SQS.
     *
     * <p>Receives one message form the specified queue. The message is
     * populated with a receipt id that can be used for the deleteMessage
     * operation.
     *
     * @param queueUrl the queue URL
     * @return a queue message containing the message body, message id and
     *         a receipt id
     * @throws NullPointerException if the specified queue URL is null
     * @throws IllegalArgumentException is the specified queue URL is an
     *         empty string
     * @throws IllegalStateException if the specified queue URL does not
     *         exist
     */
    QueueMessage pull(String queueUrl);

    /**
     * Maps to {@code DeleteMessageResult	deleteMessage(String queueUrl, String receiptHandle)}
     * in SQS.
     *
     * <p>Deletes the specified message from the specified queue using the
     * receipt ID supplied in a pulled message. An invalid receipt handle
     * meaning that the visibility timeout has expired on a message.
     *
     * @param queueUrl the queue URL
     * @param receiptId the receipt ID
     * @return false if the receipt handle isn't valid
     * @throws NullPointerException if either the specified queue URL or
     *         receipt ID is null
     * @throws IllegalArgumentException if either the specifed queue URL
     *         or receipt ID is empty
     * @throws IllegalStateException if the specified queue URL does not
     *         exist
     */
    boolean deleteMessage(String queueUrl, String receiptId);

}
