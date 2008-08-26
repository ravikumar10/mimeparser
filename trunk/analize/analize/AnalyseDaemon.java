package analize;

import configuration.Configuration;
import queue.Queue;

/**
 * Deamon responsible for taking messages from queue and analizing
 * them according to given rules
 * @author zbychu
 *
 */
public class AnalyseDaemon implements Runnable {

	private Queue queue = Queue.getQueue("in_queue");
	private Configuration configuration;
	private int SLEEP_TIME = 3000;
	
	public AnalyseDaemon(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void run() {
		
		System.out.println("Starting analize deamon ...");
		
		while(true) {
			
			Object queueElement = queue.getMessageFromQueue();
			if (queueElement!=null) {
				// starting analysing thread
				byte[] messageBuffer = (byte[]) queueElement;
				AnalyseThread analizeThread = new AnalyseThread(messageBuffer, configuration);
				Thread analizer = new Thread(analizeThread);
				analizer.start();
			} else {
				// nothing in the queue
				System.out.println("Nothing in queue - sleeping for " + SLEEP_TIME + " ...");
				try {
					Thread.sleep(this.SLEEP_TIME);
				} catch (InterruptedException e) {
					System.err.println("Nie udalo sie uspic watku");
					continue;
				}
			}
		}
	}
}
