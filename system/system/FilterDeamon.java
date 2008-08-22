package system;

import server.SMTPServer;

/**
 * FilterDeamon responsible for starting all 
 * of 
 * @author zbychu
 *
 */
public class FilterDeamon {

	
	
	public FilterDeamon() {
		
	}
	
	public void start() {
		
		SMTPServer smtpServer = new SMTPServer();
		Thread thread = new Thread(smtpServer);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.err.println("Problem z dojoinowaniem sie do watku servera SMTP");
			return;
		}
	}
	
	public static void main(String[] args) {
		
		FilterDeamon s = new FilterDeamon();
		System.out.println("Starting filter ...");
		s.start();
		
	}
	
}
