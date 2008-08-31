package analize;

import org.apache.log4j.Logger;

import configuration.Configuration;
import queue.Queue;
import queue.QueueMessage;

/**
 * Deamon responsible for taking messages from queue and analizing
 * them according to given rules
 * @author zbychu
 *
 */
public class AnalyseDaemon implements Runnable {
	
	public static Logger logger = Logger.getLogger("log");
	
	private Queue queue = Queue.getQueue("in_queue");
	private Configuration configuration;
	private int SLEEP_TIME = 3000;
	
	public AnalyseDaemon(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void run() {
		
		logger.info("Starting analize deamon ...");
		
		while(true) {
			
			Object queueElement = queue.getMessageFromQueue();
			if (queueElement!=null) {
				// starting analysing thread
				QueueMessage queueMessage = (QueueMessage) queueElement;
				AnalyseThread analizeThread = new AnalyseThread(queueMessage, configuration);
				Thread analizer = new Thread(analizeThread);
				analizer.start();
			} else {
				// nothing in the queue
				logger.info("Nothing in in_queue - sleeping for " + SLEEP_TIME + " ");
				try {
					Thread.sleep(this.SLEEP_TIME);
				} catch (InterruptedException e) {
					logger.error("Nie udalo sie uspic watku");
					continue;
				}
			}
		}
	}
}
