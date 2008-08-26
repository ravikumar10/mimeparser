package system;

import analize.AnalyseDaemon;
import configuration.Configuration;
import smtp.server.SMTPServer;

/**
 * FilterDeamon responsible for starting all of daemons:
 * - smtp daemon (simple smtp server)
 * - analyse daemon
 * @author zbychu
 *
 */
public class FilterDaemon {

	private Configuration configuration;
	
	public FilterDaemon(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public void start() {
		
		startSMTPServer();
		startAnalizeDeamon();
		startSendingDeamon();
		
	}
	/**
	 * starts sending daemon
	 */
	private void startSendingDeamon() {
		
	}
	
	/**
	 * starts analysing daemon
	 */
	private void startAnalizeDeamon() {
		
		AnalyseDaemon analizeDeamon = new AnalyseDaemon(configuration);
		Thread thread = new Thread(analizeDeamon);
		thread.start();
	}
	
	private void startSMTPServer() {
		
		SMTPServer smtpServer = new SMTPServer();
		Thread thread = new Thread(smtpServer);
		thread.start();
	}
	
	public static void main(String[] args) {
		
		System.out.println("Reading configuration ...");
		Configuration configuration = new Configuration("configuration/configuration/example_configuration_file.con");
		
		System.out.println("Validating configuration ...");
		
		//TODO!! validate configuration !!!
		
		FilterDaemon s = new FilterDaemon(configuration);
		
		System.out.println("Starting filter ...");
		s.start();
		
	}
	
}
