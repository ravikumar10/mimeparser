package system;

import analize.AnalyseDaemon;
import configuration.Configuration;
import smtp.client.SMTPClientDeamon;
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
		startAnalizeDaemon();
		startSendingDaemon();
		
	}
	
	/**
	 * starts analysing daemon
	 */
	private void startAnalizeDaemon() {
		
		AnalyseDaemon analizeDeamon = new AnalyseDaemon(configuration);
		Thread thread = new Thread(analizeDeamon);
		thread.start();
	}
	
	/**
	 * Starts smtp server
	 */
	private void startSMTPServer() {
		
		SMTPServer smtpServer = new SMTPServer();
		Thread thread = new Thread(smtpServer);
		thread.start();
	}
	
	/**
	 * Starts sending daemon
	 */
	public void startSendingDaemon() {
		
		SMTPClientDeamon smtpClientDeamon = new SMTPClientDeamon();
		Thread thread = new Thread(smtpClientDeamon);
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
