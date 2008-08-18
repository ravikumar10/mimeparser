package tests.queue;

import queue.Queue;
import junit.framework.TestCase;

public class QueueTest extends TestCase {
	
	Queue testQueue = Queue.getQueue("in_queue");
	
	/**
	 * test of actions made on queue - puts
	 * and gets for queue
	 */
	public void testActionsForQueue() {
		
		
		// getting from empty queue
		Object message = testQueue.getMessageFromQueue();
		assertNull(message);
		
		// putting an object to queue
		String o = new String("test");
		boolean result = testQueue.addMessageToQueue(o);
		assertEquals(true, result);
		
		// changing capacity and trying to put more tham max elem
		testQueue.changeQueueCapacity(1);
		result = testQueue.addMessageToQueue(o);
		assertEquals(false, result);
		
		// changing capacity putting more elements and taking the first one
		testQueue.changeQueueCapacity(2);
		String o1 = new String("test2");
		testQueue.addMessageToQueue(o1);
		
		//getting test
		String s = (String)testQueue.getMessageFromQueue();
		assertEquals("test", s);
		
		//getting test2
		s = (String)testQueue.getMessageFromQueue();
		assertEquals("test2", s);
		
		//getting null
		s = (String)testQueue.getMessageFromQueue();
		assertNull(s);
		
	}
	
}
