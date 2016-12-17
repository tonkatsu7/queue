package com.example;

/**
 * Created by sipham on 16/12/16.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        InMemoryQueueService p = new InMemoryQueueService();
        InMemoryQueueService c = new InMemoryQueueService();
        String url = p.createQueue("default");

        Runnable pTask = () -> {
            for (int i=0; i<110; i++) {
                QueueMessage m = p.push(url, "Message_" + i);
                System.out.println("Pushed message: " + m.getMessageBody());
            }
            System.out.println("Done pushing");
        };

        Runnable cTask = () -> {
            for (int i=0; i<59; i++) {
                QueueMessage m = c.pull(url);
                System.out.println("Pulled message: " + m.getMessageBody());
            }
            System.out.println("Done pulling");
        };

        Thread pThread = new Thread(pTask, "producer1");
        Thread pThread2 = new Thread(pTask, "producer2");
        Thread cThread = new Thread(cTask, "consumer1");
        Thread cThread2 = new Thread(cTask, "consumer2");
        Thread cThread3 = new Thread(cTask, "consumer3");

        pThread.start();
        pThread2.start();
        cThread.start();
        cThread2.start();
        cThread3.start();

        pThread.join();
        pThread2.join();
        cThread.join();
        cThread2.join();
        cThread3.join();


//        Singleton c = new Singleton();
//        Singleton p = new Singleton();
//
//        Runnable pTask = () -> {
//            for (int i=0; i<20; i++) {
//                try {
//                    p.increment();
//                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            System.out.println("Done producing");
//        };
//
//        Runnable pTask2 = () -> {
//            for (int i=0; i<20; i++) {
//                try {
//                    p.increment2();
//                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            System.out.println("Done producing");
//        };
//
//        Runnable cTask = () -> {
//            for (int i=0; i<20; i++) {
//                c.decrement();
//            }
//            System.out.println("Done consuming");
//        };
//
//        Thread pThread = new Thread(pTask, "producer1");
//        Thread pThread2 = new Thread(pTask2, "producer2");
//        Thread cThread = new Thread(cTask, "consumer1");
//        Thread cThread2 = new Thread(cTask, "consumer2");
//        Thread cThread3 = new Thread(cTask, "consumer3");
//
//        cThread.start();
//        cThread2.start();
//        pThread.start();
//        pThread2.start();
//
//        cThread.join();
//        cThread2.join();
//        pThread.join();
//        pThread2.join();
    }
}
