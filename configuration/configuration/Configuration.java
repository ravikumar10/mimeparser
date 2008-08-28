package configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import mail.util.LineInputStream;

import analize.MaxPartSizeRule;
import analize.MimeMessageHeaderPresenceRule;
import analize.KeywordSearchHeaderRule;
import analize.Rule;

/**
 * Class resposible for reading the configuration file 
 * creating from it rules, and set some infos for system
 * (like memory to use, amount of max threads to run etc.)
 * @author zbychu
 *
 */
public class Configuration {

	private String pathToConfigurationFile;
	
	private List<Rule> rules = new ArrayList<Rule>();
	
	private static HashMap<String, Object> systemSettings = new HashMap<String, Object>();
	
	static {
		
		systemSettings.put("smtp_listen_port", null);
		systemSettings.put("max_smtp_clients", null);
		systemSettings.put("max_analyse_threads", null);
		systemSettings.put("add_header", null);
		systemSettings.put("max_smtp_sending_clients", null);
	}
	
	
	public Configuration(String configurationFilename) {
		this.pathToConfigurationFile=configurationFilename;
		loadConfiguration();
	}
	
	public void validateConfiguration() throws ConfigurationValidationException {
		
		for (String o : systemSettings.keySet()) {
			if (systemSettings.get(o)==null) {
				throw new ConfigurationValidationException("No " + o + " key in configuration");
			}
		}
		
		if (rules.size()==0) {
			throw new ConfigurationValidationException("No rules in system!");
		}
	}
	
	/**
	 * parses configuration file and 
	 * creates rules and sets some info 
	 * about system
	 */
	public void loadConfiguration() {
		
		File conf = new File(pathToConfigurationFile);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(conf)));
			String line=null;
		
			while ((line=br.readLine())!=null) {
				
				if (line.contains("//")) //delete all after //
					line = line.substring(0, line.indexOf("//"));
				line = line.trim();
				
				//getting info about system
				//String[] setting = line.split("=");
				Pattern pattern = Pattern.compile("([a-zA-Z_0-9 ][^#]+)=([a-zA-Z_0-9 ][^#]+)");
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) { // normal settings
					parseSystemSettingsLine(line);
				} else if (line.contains("#")){ // rule
					Rule rule = parseRuleLine(new String(line.getBytes()));
					if (rule != null) rules.add(rule);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void parseSystemSettingsLine(String line) {
		
		String[] lineSegments = line.split("=");
		if (lineSegments.length==2) {
			String option = lineSegments[0].trim();
			if (option.equals("smtp_listen_port")) {
				try {
					Integer port = new Integer(lineSegments[1].trim());
					systemSettings.put("smtp_listen_port", port.intValue());
				} catch (NumberFormatException e) {
					return;
				}
			} else if (option.equals("max_smtp_clients")) {
				try {
					Integer max_smtp_clients = new Integer(lineSegments[1].trim());
					systemSettings.put("max_smtp_clients", max_smtp_clients.intValue());
				} catch (NumberFormatException e) {
					return;
				}
			} else if (option.equals("max_analyse_threads")) {
				try {
					Integer max_analyse_threads = new Integer(lineSegments[1].trim());
					systemSettings.put("max_analyse_threads", max_analyse_threads.intValue());
				} catch (NumberFormatException e) {
					return;
				}
			} else if (option.equals("max_smtp_sending_clients")) {
				try {
					Integer max_smtp_sending_clients = new Integer(lineSegments[1].trim());
					systemSettings.put("max_smtp_sending_clients", max_smtp_sending_clients.intValue());
				} catch (NumberFormatException e) {
					return;
				}
			} else if (option.equals("add_header")) {
				Boolean add_header = new Boolean(lineSegments[1].trim());
				systemSettings.put("add_header", add_header.booleanValue());
			}
		}
		
	}
	
	public static Rule parseRuleLine(String ruleLine) {
		
		Rule ret = null;
		String[] ruleSegments = ruleLine.split("#");
		if (ruleSegments.length>=3) {
			String ruleName = ruleSegments[0];
			String ruleType = ruleSegments[1];
			
			
			int ruleTypeInt = 0; 
			try{
				ruleTypeInt = new Integer(ruleType).intValue();
			} catch (NumberFormatException ex) {
				//loggowanie ze blad w zaczytywaniu configuracji
				return ret;
			}
			
			switch (ruleTypeInt) {
			case 1:// header presence
				if (ruleSegments.length==5) {
					String[] listOfHeaders = ruleSegments[2].split(",");
					return new MimeMessageHeaderPresenceRule(ruleName,listOfHeaders, new Boolean(ruleSegments[3]), new Boolean(ruleSegments[4]));
				}
			case 2:// keyword search in headers of messages
				if (ruleSegments.length==6) {
					String[] listOfHeaders = ruleSegments[2].split(",");
					return new KeywordSearchHeaderRule(ruleName, listOfHeaders, ruleSegments[3], new Boolean(ruleSegments[4]), new Boolean(ruleSegments[5]));
				}
			case 5://keyword search in headers of parts
				if (ruleSegments.length==6) {
					String[] listOfHeaders = ruleSegments[2].split(",");
					return new KeywordSearchHeaderRule(ruleName, listOfHeaders, ruleSegments[3], new Boolean(ruleSegments[4]), new Boolean(ruleSegments[5]), true);
				}
			case 6:
				if (ruleSegments.length==5) 
					return new KeywordSearchHeaderRule(ruleName, new String[]{"Content-type"}, ruleSegments[2], new Boolean(ruleSegments[3]), new Boolean(ruleSegments[4]), true);
			case 7:
				if (ruleSegments.length==5) {
					int maxFileSize = new Integer(ruleSegments[2]).intValue();
					return new MaxPartSizeRule(ruleName, maxFileSize, new Boolean(ruleSegments[3]), new Boolean(ruleSegments[4]));
				}
			default:
				break;
			}
				
		}
		return ret;
	}
	
	public String getPathToConfigurationFile() {
		return pathToConfigurationFile;
	}

	public void setPathToConfigurationFile(String pathToConfigurationFile) {
		this.pathToConfigurationFile = pathToConfigurationFile;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		String rules = "";
		for (Rule r : this.rules) {
			rules+=r.toString()+"\n";
		}
		return rules;
	}
	
	public static void main(String[] args) {

		Configuration c = new Configuration("configuration/configuration/example_configuration_file.conf");
		
		try {
			c.validateConfiguration();
		} catch (ConfigurationValidationException e) {
			System.err.println("Blad w konfiguracji " + e.toString());
		}
	}
	
	
	
}
