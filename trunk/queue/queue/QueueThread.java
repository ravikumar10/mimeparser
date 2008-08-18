package queue;

/**
 * just for tests
 * @author zbychu
 *
 */
public class QueueThread extends Thread {
	
	Queue q = Queue.getQueue("in_queue");
	
	private String name;
	
	public QueueThread(String name) {
		this.name = name;
	}
	
	@Override
	public void run() {
		
		// TODO Auto-generated method stub
		q.addMessageToQueue(name);
		
//		while (true) {
//			System.out.println("W watku " + name);
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

}
