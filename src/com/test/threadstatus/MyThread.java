package com.test.threadstatus;

/**
 * Created by User on 2017/8/16.
 */
public class MyThread {

    public static void main(String[] args) {
        MyThread.WAITING();
    }

    static void NEW() {
        Thread t = new Thread();
        System.out.println(t.getState());
    }

//    RUNNABLE, 也简单, 让一个thread start, 同时代码里面不要sleep或者wait等

    private static void RUNNABLE() {
        Thread t = new Thread() {

            public void run() {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    System.out.println(i);
                }
            }

        };

        t.start();
    }


//  3. BLOCKED, 这个就必须至少两个线程以上, 然后互相等待synchronized 块

    private static void BLOCKED() {

        final Object lock = new Object();

        Runnable run = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {

                    synchronized (lock) {
                        System.out.println(i);
                    }

                }
            }
        };

        Thread t1 = new Thread(run);
        t1.setName("t1");
        Thread t2 = new Thread(run);
        t2.setName("t2");

        t1.start();
        t2.start();

    }


//    4. WAITING, 这个需要用到生产者消费者模型, 当生产者生产过慢的时候, 消费者就会等待生产者的下一次notify

    private static void WAITING() {

        final Object lock = new Object();
        Thread t1 = new Thread() {
            @Override
            public void run() {

                int i = 0;

                while (true) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                        }
                        System.out.println(i++);
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {

                while (true) {
                    synchronized (lock) {
                        for (int i = 0; i < 10000000; i++) {
                            System.out.println(i);
                        }
                        lock.notifyAll();
                    }

                }
            }
        };

        t1.setName("^^t1^^");
        t2.setName("^^t2^^");

        t1.start();
        t2.start();
    }


//    5. TIMED_WAITING, 这个仅需要在4的基础上, 在wait方法加上一个时间参数进行限制就OK了.

    private static void TIMED_WAITING() {

        final Object lock = new Object();
        Thread t1 = new Thread() {
            @Override
            public void run() {

                int i = 0;

                while (true) {
                    synchronized (lock) {
                        try {
                            lock.wait(60 * 1000L);
                        } catch (InterruptedException e) {
                        }
                        System. out .println(i++);
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {

                while (true) {
                    synchronized (lock) {
                        for (int i = 0; i < 10000000; i++) {
                            System.out.println(i);
                        }
                        lock.notifyAll();
                    }

                }
            }
        };

        t1.setName("^^t1^^");
        t2.setName("^^t2^^");

        t1.start();
        t2.start();
    }

//6. TERMINATED, 这个状态只要线程结束了run方法, 就会进入了…

    private static void TERMINATED() {
        Thread t1 = new Thread();
        t1.start();
        System. out.println(t1.getState());
        try {
            Thread. sleep(1000L);
        } catch (InterruptedException e) {
        }
        System. out.println(t1.getState());
    }

}
