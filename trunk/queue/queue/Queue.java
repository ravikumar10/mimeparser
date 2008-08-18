package queue;

import java.util.ArrayList;
import java.util.List;

/**
 * Represends queue itself. It enables putting and getting
 * type of objects you desire to it (filter uses this
 * to put messages so that method are so called)
 * @author zbychu
 */
public class Queue {
	
	final static int MAX_CAPACITY = 20;
	
	int capacity = MAX_CAPACITY;
	
	List<Object> queueElements = new ArrayList<Object>();
	
	public static synchronized Queue getQueue (String name) {
		QueueManager manager = QueueManager.getQueueManager();
		Queue q = manager.getQueue(name);
		return q;
	}
	
	private String name;
	
	public Queue(String name) {
		this.name = name;
	}
	
	/**
	 * only for tests shouldn't be used to change maximum
	 * capacity
	 */
	public void changeQueueCapacity(int newCapacity) {
		this.capacity = newCapacity;
	}
	
	/**
	 * adds message to queue if capacity of queue is achieved
	 * then no message is inserted and false is returned
	 * 
	 * @return true if succeded false if failed
	 */
	public boolean addMessageToQueue(Object e) {
		
		synchronized(this) {
			if (queueElements.size()<capacity) {
				queueElements.add(e);
				return true;
			} else return false;
		}
	}
	
	
	/**
	 * gets the newest message from the queue
	 * if no message in the queue then it returns
	 * null
	 * 
	 * @return object or null if there are no elements
	 */
	public Object getMessageFromQueue() {
		synchronized(this) {
			if (!queueElements.isEmpty()) {
				return queueElements.remove(0);
			}
			return null;
		}
	}

}
