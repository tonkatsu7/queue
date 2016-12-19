package com.example;

import com.google.common.base.Throwables;

import static com.example.QueueServiceUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileQueueService implements QueueService {
    static Logger logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);

    // Constants
    private static String MESSAGE_FILE_NAME = "messages";
    private static String TEMP_MESSAGE_FILE_NAME = "messages.temp";
    private static String LOCK_DIR_NAME = ".lock";

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
         * Queue is defined as...
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
//            lock(rootLock);
            // if queue name dir doesn't exist
            if (!queueDir.exists()) {
                // create queue name dir
                queueDir.mkdir();
                messagesFile.createNewFile();
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
//            unlock(rootLock);
//        } catch (InterruptedException e) {
//            Throwables.propagate(e);//e.printStackTrace(); // TODO
//        }


        return queueUrl;
    }

    @Override
    public Set<String> listQueues() {
        // count the directory names under root dir
        File rootDir = getRootDir();
        File rootLock = getRootLock();
        Set<String> queueUrls = new HashSet<>();

//        try {
//            lock(rootLock);
            File[] directories = rootDir.listFiles(File::isDirectory);

            for (File dir : directories) {
                String directory = dir.getName();
                if (!directory.startsWith(".")) {
                    queueUrls.add(toUrl(directory));
                }
            }

//            Files.list(rootDir.toPath())
//                    .filter(p -> !p.getFileName().toString().startsWith("."))
//                    .collect(Collectors.toSet());
//
//        } catch (IOException e) {
//            e.printStackTrace(); // TODO
//        } catch (InterruptedException e) {
//            Throwables.propagate(e);//e.printStackTrace(); // TODO
//        } finally {
//            unlock(rootLock);
//        }

        return queueUrls;
    }

    @Override
    public String getQueueUrl(String queueName) {
        checkQueueName(queueName);

        if (getQueueDir(queueName).exists()) {
            return toUrl(queueName);
        } else {
            throw new IllegalStateException(QUEUE_NAME_DOES_NOT_EXIST);
        }
    }

    @Override
    public void deleteQueue(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);
        File queueDir = getQueueDir(fromUrl(queueUrl));
        File queueLock = getQueueLock(queueName);
        File rootLock = getRootLock();

//        try {
//            lock(rootLock);
            try {
                if (queueDir.exists()) {
                    lock(queueLock);
                    Files.walk(queueDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);

                }
            } catch (IOException e) {
                Throwables.propagate(e);//e.printStackTrace(); // TODO
            } catch (InterruptedException e) {
                Throwables.propagate(e);//e.printStackTrace();; // TODO
            } finally {
                unlock(queueLock);
            }
//        } catch (InterruptedException e) {
//            Throwables.propagate(e);//e.printStackTrace(); // TODO
//        } finally {
//            unlock(rootLock);
//        }
    }

    @Override
    public QueueMessage push(String queueUrl, String message) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);
        File messagesFile = getMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);

        QueueMessage pushMessage = new QueueMessage(message, generateMessageId());

        if (!getQueueDir(queueName).exists()) {
            throw new IllegalStateException(QUEUE_URL_DOES_NOT_EXIST);
        }

        try {
            lock(queueLock);
            try (PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))) {
                pw.println(createVisibleQueueRecord(pushMessage));
            } catch (IOException e) {
                Throwables.propagate(e);//e.printStackTrace(); // TODO
            } finally {
                unlock(queueLock); // TODO
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);//e.printStackTrace(); // TODO
        }
        return pushMessage;
    }

    @Override
    public QueueMessage pull(String queueUrl) {
        checkQueueUrl(queueUrl);

        String queueName = fromUrl(queueUrl);
        File messagesFile = getMessagesFile(queueName);
        File tempMessagesFile = getTempMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);

        QueueMessage dequeued = new QueueMessage();

        if (!getQueueDir(queueName).exists()) {
            throw new IllegalStateException(QUEUE_URL_DOES_NOT_EXIST);
        }

        // start timer for wait time for pulling
        long startTime = now();
        while (dequeued.isEmpty()) {
            try {
                lock(queueLock);

                // rename messsages file
                messagesFile.renameTo(tempMessagesFile);

                // then read from temp file
                try (BufferedReader br = new BufferedReader(new FileReader(tempMessagesFile));
                     PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))
                ) {
                    boolean visibleMessageFound = false;
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        String[] parts = line.split(":");

                        // e.g. 0:0:614c58b8-c319-4137-a1da-eb0b75fa19a2:02fa4094-2a2d-4677-a1c9-89bf9420cb1a:{"media":"MABJsxUmBps",...}
                        boolean inFlight = (Integer.parseInt(parts[0]) == 1) ? true : false;// [0] in flight 0=false, 1=true
                        long visibilityTimeoutFrom = Long.parseLong(parts[1]);// [1] visibility timeout from
                        String receiptId = parts[2];// [2] receipt id
                        QueueMessage message = new QueueMessage(parts[4], parts[3]); // [3] message id, [4] message

                        if (inFlight) { // in flight
                            long elapsed = now() - visibilityTimeoutFrom;
                            System.out.println("pull elapsed=" + elapsed);
                            if (elapsed > visibilityTimeoutMillis) { // visibility timeout has elasped
                                if (!visibleMessageFound) { // newly visibile message returned
                                    dequeued = new QueueMessage(message, generateReceiptId(), now());
                                }
                                pw.println(createVisibleQueueRecord(message));
                            } else {
                                pw.println(line); // remains in flight
                            }
                        } else if (!visibleMessageFound && !inFlight) { // we found the first visible message
                            visibleMessageFound = true;
                            dequeued = new QueueMessage(message, generateReceiptId(), now());
                            pw.println(createInvisibleQueueRecord(dequeued));
                        } else { // and simply reprint the remaining messages
                            pw.println(line);
                        }
                    }
//            } catch (FileNotFoundException e) {
//                Throwables.propagate(e);// e.printStackTrace(); // TODO
                } catch (IOException e) {
                    Throwables.propagate(e);//e.printStackTrace(); // TODO
                } finally {
                    tempMessagesFile.delete();
                    unlock(queueLock);
                }
            } catch (InterruptedException e) {
                Throwables.propagate(e);//e.printStackTrace(); // TODO
            }
            // if time elpased is greater than wait time
            if ((now() - startTime) > pullWaitTimeMillis) {
                return new QueueMessage();
            }
        }

        return dequeued;
    }

    @Override
    public boolean deleteMessage(String queueUrl, String receiptId) {
        checkQueueUrl(queueUrl);
        checkReceiptId(receiptId);

        String queueName = fromUrl(queueUrl);
        File messagesFile = getMessagesFile(queueName);
        File tempMessagesFile = getTempMessagesFile(queueName);
        File queueLock = getQueueLock(queueName);

        QueueMessage dequeued = new QueueMessage();

        if (!getQueueDir(queueName).exists()) {
            throw new IllegalStateException(QUEUE_URL_DOES_NOT_EXIST);
        }

        boolean messageDeleted = false;

        try {
            lock(queueLock);

            // rename messsages file
            messagesFile.renameTo(tempMessagesFile);

            // then read from temp file
            try (BufferedReader br = new BufferedReader(new FileReader(tempMessagesFile));
                 PrintWriter pw = new PrintWriter(new FileWriter(messagesFile, true))
            ) {

                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    String[] parts = line.split(":");

                    // e.g. 0:0:614c58b8-c319-4137-a1da-eb0b75fa19a2:02fa4094-2a2d-4677-a1c9-89bf9420cb1a:{"media":"MABJsxUmBps",...}
                    boolean inFlight = (Integer.parseInt(parts[0]) == 1) ? true : false;// [0] in flight 0=false, 1=true
                    long visibilityTimeoutFrom = Long.parseLong(parts[1]);// [1] visibility timeout from
                    String currReceiptId = parts[2];// [2] receipt id
                    QueueMessage message = new QueueMessage(parts[4], parts[3]); // [3] message id, [4] message

                    if (inFlight) { // in flight
                        long elapsed = now() - visibilityTimeoutFrom;
                        System.out.println("delete elapsed=" + elapsed);
                        if (elapsed > visibilityTimeoutMillis) { // visibility timeout has elapsed
                            pw.println(createVisibleQueueRecord(message));
                        } else if (currReceiptId.equals(receiptId)) { // if receipt id matches
                            messageDeleted = true;
                        } else {
                            pw.println(line); // remains in flight
                        }
                    } else { // and simply reprint the remaining messages
                        pw.println(line);
                    }
                }
//            } catch (FileNotFoundException e) {
//                Throwables.propagate(e);// e.printStackTrace(); // TODO
            } catch (IOException e) {
                Throwables.propagate(e);//e.printStackTrace(); // TODO
            } finally {
                tempMessagesFile.delete();
                unlock(queueLock);
            }
        } catch (InterruptedException e) {
            Throwables.propagate(e);//e.printStackTrace(); // TODO
        }

        return messageDeleted;
    }

    // Private methods

    private String toUrl(String queueName) {
        return urlPrefix + queueName;
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
//        System.out.println(fileRecord.toString());
        return fileRecord.toString();
    }
}
