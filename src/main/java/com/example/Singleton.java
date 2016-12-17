package com.example;

/**
 * Created by sipham on 17/12/16.
 */
public class Singleton {
//    INSTANCE;

    private static int count;
    private static int[] buffer;
    private static Object lock = new Object();

    public Singleton () {
        count = 0;
        buffer = new int[100];
    }

    public void increment() throws InterruptedException {
        synchronized (lock) {
            while (isFull(buffer)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer[count++] = 1;
            System.out.println("Incrementing buffer to " + count);
            lock.notifyAll();
        }
    }

    public void increment2() throws InterruptedException {
        synchronized (lock) {
            while (isFull(buffer)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer[count++] = 1;
            System.out.println("Incrementing buffer to " + count);
            lock.notifyAll();
        }
        Thread.sleep(2);
    }

    public void decrement() {
        synchronized (lock) {
            while (isEmpty(buffer)) {
                try {
                    System.out.println("waiting inn decrement");
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer[--count] = 0;
            System.out.println("Decrementing buffer to " + count);
            lock.notifyAll();
        }
    }

    public int getCount() {
        return count;
    }

    private boolean isEmpty(int[] buffer) {
        return count == 0;
    }

    private boolean isFull(int[] buffer) {
        return count == buffer.length;
    }
}
