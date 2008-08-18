package queue;

import java.util.Hashtable;

/** 
 * It manages {@link Queue} queues
 * @author zbychu
 *
 */
public class QueueManager {
	
	private Hashtable<String,Queue> queues = new Hashtable<String,Queue>();
	
	private static QueueManager manager;
	
	/**
	 * initialization of manager
	 */
	static {
		manager = new QueueManager();
	}
	
	public static QueueManager getQueueManager() {
		if (manager!=null) {
			return manager;
		} else throw new RuntimeException();
	}
	
	/**
	 * Returns queue with given name. If such queue
	 * does not exist yet it creates it.
	 */
	public synchronized Queue getQueue(String name) {
		
		Queue queue = queues.get(name);
		if (queue!=null) return queue;
		Queue newQueue = new Queue(name);
		queues.put(name, newQueue);
		return newQueue;
	}
	
	public static void main(String[] args) {
		
		QueueThread q1 = new QueueThread("A");
		QueueThread q2 = new QueueThread("B");
		
		q1.start();
		q2.start();
		try {
			q1.join();
			q2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("All");
	}
	
}
