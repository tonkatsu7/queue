package com.example;

import com.google.common.base.Throwables;

import static com.example.QueueServiceUtil.*;

import static com.google.common.base.Preconditions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class FileQueueService implements QueueService {
    // Constants
    private static String MESSAGE_FILE_NAME = "messages";
    private static String TEMP_MESSAGE_FILE_NAME = "messages.temp";
    private static String LOCK_DIR_NAME = ".lock";
    private static int INT_FLIGHT_INDEX = 0;
    private static int VISIBILITY_TIMEOUT_INDEX = 1;
    private static int RECEIPT_ID_INDEX = 2;
    private static int MESSAGE_ID_INDEX = 3;
    private static int MESSAGE_BODY_INDEX = 4;

    // Fields
    private String rootDir;
    private String urlPrefix;
    private long pullWaitTimeMillis;
    private long visibilityTimeoutMillis;

    public FileQueueService(String rootDir, String urlPrefix, long pullWaitTimeMillis, long visibilityTimeoutMillis) {
        this.rootDir = rootDir;
        this.urlPrefix = urlPrefix;
        this.pullWaitTimeMillis = pullWaitTimeMillis;
        this.visibilityTimeoutMillis = visibilityTimeoutMillis;
    }

    @Override
    public String createQueue(String queueName) {
        checkQueueName(queueName);

        /*
         * Queue is defined on the file system as...
         *
         *             .rootLock
         * <rootDir> / <queueName> / .lock /
         *                            messages
         */

        String queueUrl = toUrl(queueName);
        File queueDir = getQueueDir(queueName);
        File messagesFile = getMessagesFile(queueName);
        File rootLock = getRootLock();

        try {
            // get main lock
            lock(rootLock);
            // if queue name dir doesn't exist
            try {
                if (!queueDir.exists()) {
                    // create queue name dir
                    queueDir.mkdir();
                    messagesFile.createNewFile();
                }
            } catch (IOException e) {
                Throwables.propagate(e);
            } finally {
                unlock(rootLock);
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
        // count the directory names under root dir
        File rootDir = getRootDir();
        Set<String> queueUrls = new HashSet<>();

        File[] directories = rootDir.listFiles(File::isDirectory);

        for (File dir : directories) {
            String directory = dir.getName();
            if (!directory.startsWith(".")) {
                queueUrls.add(toUrl(directory));
            }
        }

        return queueUrls;
    }

    @Override
    public String getQueueUrl(String queueName) {
        checkQueueName(queueName);

        checkState(getQueueDir(queueName).exists(), QUEUE_NAME_DOES_NOT_EXIST);

        return toUrl(queueName);
    }

    @Override
    public void deleteQueue(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);
        File queueDir = getQueueDir(fromUrl(queueUrl));
        File messagesFile = getMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);
        File rootLock = getRootLock();

        try {
            lock(rootLock);
            try {
                if (queueDir.exists()) {
                    lock(queueLock);
                    try {
                        Files.walk(queueDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                                .sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    } finally {
                        unlock(queueLock);
                    }
                }
            } catch (InterruptedException e) { // interrupted on queue lock
                Throwables.propagate(e);
            } finally {
                unlock(rootLock);
            }
        } catch (InterruptedException e) { // interrupted on root lock
            Throwables.propagate(e);
        }
    }

    @Override
    public QueueMessage push(String queueUrl, String message) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);
        File messagesFile = getMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);

        QueueMessage pushMessage = new QueueMessage(message, generateMessageId());

        checkState(getQueueDir(queueName).exists(), QUEUE_URL_DOES_NOT_EXIST);

        try {
            lock(queueLock);
            try (PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))) {
                pw.println(createVisibleQueueRecord(pushMessage));
            } catch (IOException e) {
                Throwables.propagate(e);
            } finally {
                unlock(queueLock); // TODO
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        checkQueueUrl(queueUrl);

        QueueMessage pulledMessage = new QueueMessage();

        // start timer for wait time for pulling
        long startTime = now();

        while (pulledMessage.isEmpty()) {
            pulledMessage = processMessages(queueUrl, false, null);
            // block
            if (pulledMessage.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Throwables.propagate(e);
                }
            }
            // if time elpased is greater than wait time
            if ((now() - startTime) > pullWaitTimeMillis) {
                break;
            }
        }

        return pulledMessage;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        checkQueueUrl(queueUrl);
        checkReceiptId(receiptId);

        QueueMessage deletedMessage = processMessages(queueUrl, true, receiptId);

        return deletedMessage.getReceiptId().equals(receiptId) ? true : false;
    }

    // Private methods

    private String toUrl(String queueName) {
        return urlPrefix + queueName;
    }

    public static String fromUrl(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
    }

    private File getRootDir() {
        return Paths.get(rootDir).toFile();
    }

    private File getQueueDir(String queueName) {
        return Paths.get(rootDir, queueName).toFile();
    }

    private File getRootLock() {
        return Paths.get(rootDir, LOCK_DIR_NAME).toFile();
    }

    private File getMessagesFile(String queueName) {
        return Paths.get(rootDir, queueName, MESSAGE_FILE_NAME).toFile();
    }

    private File getTempMessagesFile(String queueName) {
        return Paths.get(rootDir, queueName, TEMP_MESSAGE_FILE_NAME).toFile();
    }

    private File getQueueLock(String queueName) {
        return Paths.get(rootDir, queueName, LOCK_DIR_NAME).toFile();
    }

    private void lock(File lock) throws InterruptedException {
        while (!lock.mkdir()) {
            Thread.sleep(50);
        }
    }

    private void unlock(File lock) {
        lock.delete();
    }

    private String createVisibleQueueRecord(QueueMessage messageObj) {
        return createFileQueueRecord(false,
                0,
                "",
                messageObj.getMessageId(),
                messageObj.getMessageBody());
    }

    private String createInvisibleQueueRecord(QueueMessage messageObj) {
        return createFileQueueRecord(true,
                messageObj.getVisibilityTimeoutFrom(),
                messageObj.getReceiptId(),
                messageObj.getMessageId(),
                messageObj.getMessageBody());
    }

    private String createFileQueueRecord(boolean inFlight, long visibilityTimeoutMillis, String receiptId, String messageId, String messageBody) {
        // e.g. 0:0:614c58b8-c319-4137-a1da-eb0b75fa19a2:02fa4094-2a2d-4677-a1c9-89bf9420cb1a:{"media":"MABJsxUmBps",...}
        // [0] in flight 0=false, 1=true
        // [1] visibility timeout from
        // [2] receipt id
        // [3] message id
        // [4] message
        StringBuilder fileRecord = new StringBuilder();
        fileRecord.append((inFlight) ? 1 : 0 );
        fileRecord.append(":");
        fileRecord.append(visibilityTimeoutMillis);
        fileRecord.append(":");
        fileRecord.append(receiptId);
        fileRecord.append(":");
        fileRecord.append(messageId);
        fileRecord.append(":");
        fileRecord.append(messageBody);
        return fileRecord.toString();
    }

    private QueueMessage processMessages(String queueUrl, boolean processDelete, String receiptId) {
        String queueName = fromUrl(queueUrl);
        File messagesFile = getMessagesFile(queueName);
        File tempMessagesFile = getTempMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);

        QueueMessage pulledOrDeletedMessage = new QueueMessage();

        checkState(getQueueDir(queueName).exists(), QUEUE_URL_DOES_NOT_EXIST);

        try {
            lock(queueLock);

            if (messagesFile.length() == 0) {
                unlock(queueLock);
                return new QueueMessage();
            }

            // rename messsages file
            messagesFile.renameTo(tempMessagesFile);

            // then read from temp file
            try (BufferedReader br = new BufferedReader(new FileReader(tempMessagesFile));
                 PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))
            ) {
                pulledOrDeletedMessage = processLines(pw, br, processDelete, receiptId);
            } catch (IOException e) {
                Throwables.propagate(e);
            } finally {
                tempMessagesFile.delete();
                unlock(queueLock);
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        return pulledOrDeletedMessage;
    }

    private QueueMessage processLines(PrintWriter pw, BufferedReader br, boolean processDelete, String receiptId) throws IOException {
        QueueMessage result = new QueueMessage();

        boolean pullOrDeleteHasOccurred = false;

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] parts = line.split(":");

            // e.g. 0:0:614c58b8-c319-4137-a1da-eb0b75fa19a2:02fa4094-2a2d-4677-a1c9-89bf9420cb1a:{"media":"MABJsxUmBps",...}
            boolean inFlight = (Integer.parseInt(parts[INT_FLIGHT_INDEX]) == 1) ? true : false;// [0] in flight 0=false, 1=true
            long visibilityTimeoutFrom = Long.parseLong(parts[VISIBILITY_TIMEOUT_INDEX]);// [1] visibility timeout from
            String currReceiptId = parts[RECEIPT_ID_INDEX];// [2] receipt id
            QueueMessage message = new QueueMessage(parts[MESSAGE_BODY_INDEX], parts[MESSAGE_ID_INDEX]); // [3] message id, [4] message

            QueueMessage resultThisLine; // track the result if processing this line, an empty QueueMessage object indicates that the pull/delete was unsuccessful

            if (processDelete) {
                resultThisLine = processLineAsDelete(inFlight, visibilityTimeoutFrom, message, pw, line, currReceiptId, receiptId);
            } else {
                resultThisLine = processLineAsPull(inFlight, visibilityTimeoutFrom, pullOrDeleteHasOccurred, message, pw, line);
            }

            if (!pullOrDeleteHasOccurred && !resultThisLine.isEmpty()) { // if processing this line has produced a result
                pullOrDeleteHasOccurred = true;
                result = resultThisLine; // return either the pulled message or confirm the deleted message
                if (processDelete) {
                    result = new QueueMessage(resultThisLine, currReceiptId, visibilityTimeoutFrom);
                }
            }
        }

        return result;
    }

    private QueueMessage processLineAsPull(boolean inFlight, long visibilityTimeoutFrom, boolean visibleMessageFound, QueueMessage message, PrintWriter pw, String line) {
        QueueMessage result = new QueueMessage();

        if (inFlight) { // in flight
            if (now() - visibilityTimeoutFrom > visibilityTimeoutMillis) { // visibility timeout has elapsed
                if (!visibleMessageFound) { // return the first re-visible message
                    result = new QueueMessage(message, generateReceiptId(), now());
                }
                pw.println(createVisibleQueueRecord(message));
            } else { // else if visibility timeout not elapsed then it remains in flight
                pw.println(line);
            }
        } else if (!visibleMessageFound && !inFlight) { // we found the first visible message that's not in flight
            result = new QueueMessage(message, generateReceiptId(), now());
            pw.println(createInvisibleQueueRecord(result));
        } else { // and simply reprint the remaining messages not in flight
            pw.println(line);
        }
        return result;
    }

    private QueueMessage processLineAsDelete(boolean inFlight, long visibilityTimeoutFrom, QueueMessage message, PrintWriter pw, String line, String currReceiptId, String receiptId) {
        QueueMessage result = new QueueMessage();

        if (inFlight) { // in flight
            if (now() - visibilityTimeoutFrom > visibilityTimeoutMillis) { // visibility timeout has elapsed, including perhaps our receipt id
                pw.println(createVisibleQueueRecord(message)); // so make visible again
            } else if (currReceiptId.equals(receiptId)) { // if receipt id matches DELETE! cos we're here while visibility has not elapsed
                result = message;
            } else {
                pw.println(line); // else if visibility timeout not elapsed then it remains in flight
            }
        } else { // and simply reprint the remaining messages not in flight
            pw.println(line);
        }
        return result;
    }
}
