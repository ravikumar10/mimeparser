package system;

import org.apache.log4j.Logger;

import analize.AnalyseDaemon;
import configuration.Configuration;
import configuration.ConfigurationValidationException;
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

	public static Logger logger = Logger.getLogger("log");
	
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
		
		logger.info("Reading configuration ...");
		Configuration configuration = new Configuration("configuration/conf.conf");
		//Configuration configuration = new Configuration("configuration/configuration/conf.conf");
		
		logger.info("Validating configuration ...");
		try {
			configuration.validateConfiguration();
		} catch (ConfigurationValidationException e) {
			logger.error("Configuration not valid: " + e.toString());
			return;
		}
		
		FilterDaemon s = new FilterDaemon(configuration);
		
		logger.info("Starting filter ...");
		s.start();
		
	}
	
}
