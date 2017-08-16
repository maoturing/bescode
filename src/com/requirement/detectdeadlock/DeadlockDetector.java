package com.deadlock3;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlockDetector {

	// private final DeadlockHandler deadlockHandler;
	private final long period;
	private final TimeUnit unit;
	private final ThreadMXBean mBean = ManagementFactory.getThreadMXBean();

	private final ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);

	public DeadlockDetector(final long period, final TimeUnit unit) {
		this.period = period;
		this.unit = unit;
	}

	final Runnable deadlockCheck = new Runnable() {

		@Override
		public void run() {
			long[] deadlockedThreads = DeadlockDetector.this.mBean.findDeadlockedThreads(); /// ������Ϊ�ȴ���ö�����������ӵ��ͬ��������������״̬���߳�ѭ���������߳�ID

			ThreadInfo[] threadInfos = DeadlockDetector.this.mBean.getThreadInfo(deadlockedThreads, true, false); // ���������߳�id�õ������߳���Ϣ

			// ���������Ϣ
			handleDeadLock(threadInfos);

			Map map = new HashMap();
			try {
				map = getDeadlockedThreadIds();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Object in : map.keySet()) {
				// map.keySet()���ص�������key��ֵ
				Integer gid = (Integer) map.get(in);// �õ�ÿ��key�����value��ֵ
				System.out.println("�߳�id:" + in + "     ����" + gid);
			}
		}

	};

	public void start() {
		this.schedule.schedule(this.deadlockCheck, this.period, this.unit);
	}

	// �õ��̵߳�������Ϣ
	public void handleDeadLock(ThreadInfo[] deadLockThreads) {
		if (deadLockThreads != null) {
			System.err.println("Deadlock detected!");

			for (ThreadInfo threadInfo : deadLockThreads) {
				if (threadInfo != null) {
					// SecurityException
					for (Thread thread : Thread.getAllStackTraces().keySet()) {
						if (thread.getId() == threadInfo.getThreadId()) {
							System.out.println();
							System.out.println("id:" + threadInfo.getThreadId());
							System.out.println("����:" + threadInfo.getThreadName());
							System.out.print("״̬��" + threadInfo.getLockName());
							System.out.println("�ϵ�" + threadInfo.getThreadState());
							System.out.println("ӵ����:" + threadInfo.getLockOwnerName());
							System.out.println("����ֹ��:" + threadInfo.getBlockedCount());
							System.out.println("�ܵȴ���:" + threadInfo.getWaitedCount());
							System.out.println("״̬��" + threadInfo.toString());

							String name = mBean.getThreadInfo(threadInfo.getLockOwnerId()).getLockName();
							System.out.println("������:" + name);
							int i = 0;

							MonitorInfo[] monitors = threadInfo.getLockedMonitors();

							for (StackTraceElement ste : thread.getStackTrace()) {
								System.err.println("��ջ���:"+thread.getStackTrace().length);
								System.err.println("��ջ��Ϣ:"+ste.toString());

								System.out.println("ƴ�ӵĶ�ջ��Ϣ:"+ste.getClassName() + ste.getMethodName() + ste.getFileName()
										+ ste.getLineNumber());
								selectThread(threadInfo.getThreadId());
								if (monitors != null) {
									for (MonitorInfo mi : monitors) {
										
										//�����������ж�,����ֶ������������,ԭ����
										if (mi.getLockedStackDepth() == 0) {
											System.out.println("������1:"+mi.toString());
										}
									}
								} else {
									System.out.println("monitorΪ��");
								}

							}
						}

					}
					System.out.println("==================");
				}
			}

		} else {
			System.out.println("δ��⵽�����߳�");
		}

	}

	// �õ��߳������Ķ���
	public void selectThread(long selected) {
		final long threadID = selected;
		StringBuilder sb = new StringBuilder();
		ThreadInfo ti = null;
		MonitorInfo[] monitors = null;
		if (mBean.isObjectMonitorUsageSupported()) {
			// VMs that support the monitor usage monitoring
			// ThreadInfo[] infos = mBean.dumpAllThreads(true, false);
			// //�������л�̵߳��߳���Ϣ�������ж�ջ���ٺ�ͬ����Ϣ ���Ϊ true����ת�����������ļ�������
			long[] deadlockedThreads = DeadlockDetector.this.mBean.findDeadlockedThreads(); /// ������Ϊ�ȴ���ö�����������ӵ��ͬ��������������״̬���߳�ѭ���������߳�ID

			ThreadInfo[] infos = DeadlockDetector.this.mBean.getThreadInfo(deadlockedThreads, true, false); // ���������߳�id�õ������߳���Ϣ

			for (ThreadInfo info : infos) {
				if (info.getThreadId() == threadID) {
					ti = info;
					monitors = info.getLockedMonitors();
					break;
				}
			}
			System.out.println("support");
		} else {
			// VM doesn't support monitor usage monitoring
			ti = mBean.getThreadInfo(threadID, Integer.MAX_VALUE);
		}
		if (ti != null) {
			int index = 0;
			if (monitors != null) {
				for (MonitorInfo mi : monitors) {
					if (mi.getLockedStackDepth() == index) {
						System.out.println("������2:" + mi.toString());
					}
				}
				index++;
			}
		}

	}

	// �õ���������, keyΪ�߳�id��valueΪ������������
	public Map getDeadlockedThreadIds() throws IOException {

		long[] ids = mBean.findDeadlockedThreads();
		if (ids == null) {
			return null;
		}
		ThreadInfo[] infos = mBean.getThreadInfo(ids, Integer.MAX_VALUE);

		List<Long[]> dcycles = new ArrayList<Long[]>();
		List<Long> cycle = new ArrayList<Long>();
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		int gid = 1;

		// keep track of which thread is visited
		// one thread can only be in one cycle
		boolean[] visited = new boolean[ids.length];

		int index = -1; // Index into arrays
		while (true) {
			if (index < 0) {
				if (map.size() > 0) {
					// a cycle found
					// dcycles.add(cycle.toArray(new Long[0]));
					gid++;
					// cycle = new ArrayList<Long>();
				}
				// start a new cycle from a non-visited thread
				for (int j = 0; j < ids.length; j++) {
					if (!visited[j]) {
						index = j;
						visited[j] = true;
						break;
					}
				}

				// �������߳̾������ʹ�,�˳�whileѭ��
				if (index < 0) {
					// done
					break;
				}
			}

			// cycle.add(ids[index]);
			map.put(ids[index], gid);
			long nextThreadId = infos[index].getLockOwnerId();

			for (int j = 0; j < ids.length; j++) {
				ThreadInfo ti = infos[j];
				if (ti.getThreadId() == nextThreadId) {
					if (visited[j]) {
						index = -1;
					} else {
						index = j;
						visited[j] = true;
					}
					break;
				}
			}
		}
		// ���ض�ά���飬��һά�����������飬�ڶ�λ���������µ��̣߳�length�Ϳɻ����������
		// return dcycles.toArray(new Long[0][0]);
		return map;
	}

}