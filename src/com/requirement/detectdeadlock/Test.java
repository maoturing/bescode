package com.deadlock3;

import java.util.concurrent.TimeUnit;

public class Test {
	public static void main(String[] args) {
		DeadlockDetector deadlockDetector = new DeadlockDetector(1, TimeUnit.SECONDS);
		deadlockDetector.start();
		final Object lock1 = new Object();
		final Object lock2 = new Object();
		final Object lock3 = new Object();

		Thread thread1 = new Thread() {
			@Override
			public void run() {
				synchronized (lock1) {
					System.out.println("Thread1 acquired lock1");
					synchronized (lock2) {
						System.out.println("Thread1 acquired lock2");
						// } //为什么synchronized (lock2)在这里结束无法产生死锁
						try {
							TimeUnit.MILLISECONDS.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						synchronized (lock3) {
							System.out.println("Thread1 acquired lock3");
						}

					}

				}
			}
		};
		thread1.setName("Thread_1");
		thread1.start();

		Thread thread2 = new Thread() {
			public void run() {
				synchronized (lock3) {
					System.out.println("Thread2 acquired lock3");
					synchronized (lock1) {
						System.out.println("Thread2 acquired lock1");
					}
				}
			}
		};
		thread2.setName("Thread_2");
		thread2.start();

		Thread thread3 = new Thread() {
			@Override
			public void run() {
				synchronized (lock3) {
					System.out.println("Thread3 acquired lock3");

					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
					}
					synchronized (lock2) {
						System.out.println("Thread3 acquired lock2");
					}

				}

			}
		};
		thread3.setName("Thread_3");
		thread3.start();

		// 第二个锁
		final Object lock11 = new Object();
		final Object lock22 = new Object();
		final Object lock33 = new Object();

		Thread thread11 = new Thread() {
			@Override
			public void run() {
				synchronized (lock11) {
					System.out.println("Thread11 acquired lock11");
					synchronized (lock22) {
						System.out.println("Thread11 acquired lock22");

						try {
							TimeUnit.MILLISECONDS.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						synchronized (lock33) {
							System.out.println("Thread1 acquired lock33");
						}
					}

				}

			}
		};
		thread11.setName("Thread_11");
		thread11.start();

		Thread thread22 = new Thread() {
			public void run() {
				synchronized (lock33) {
					System.out.println("Thread22 acquired lock33");
					synchronized (lock11) {
						System.out.println("Thread22 acquired lock11");
					}

				}

			}
		};
		thread22.setName("Thread_22");
		thread22.start();
	}
}