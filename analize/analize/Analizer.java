package analize;

import java.util.HashMap;
import java.util.List;

import mail.MimeMessage;
import mail.MimeMessageHeader;
import mail.util.MimeUtility;
import mail.util.StringUtils;

import configuration.Configuration;

public class Analizer {
	
	List<Rule> rules;
	
	MimeMessage message;

	public Analizer() {}
	
	public Analizer(List<Rule> rules) {
		this.rules=rules;
	}
	
	/**
	 * Analyses message according to rules
	 * and message
	 * It decides if message should be dropped (filter out)
	 * 
	 */
	public void analize() {
		
		boolean shouldbeDropped = false;
		HashMap<String, Boolean> headerPresenceMap = new HashMap<String, Boolean>();
		
		for (MimeMessageHeader header : message.getHeaders().getHeaders()) {
			for (Rule rule : rules) {
				switch (rule.ruleType) {
				case HEADER_PRESENCE:
					HeaderPresenceRule hpr = (HeaderPresenceRule) rule;
					for (String headerString : hpr.getHeaders()) {
						if (hpr.isPresent()) { 
							if (hpr.isFilterOut() && headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase()))
								//filtering all messages that contains some header (headerString)
								shouldbeDropped = true;
						} else {
							if (hpr.isFilterOut()) {
								if (headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
									// if message contains this header we mark it and at the end we check if 
									// all headers were marked
									headerPresenceMap.put(hpr.getName(), new Boolean(true));
								} else {
									// here we mark that we haven't encounter this header yet 
									if (!headerPresenceMap.get(hpr.getName()))
										headerPresenceMap.put(hpr.getName(), new Boolean(false));
								}
							}
						}
					}
					break;
				case LOOK_KEYWORD:
					KeywordSearchHeaderRule kshr = (KeywordSearchHeaderRule) rule;
					for (String headerString : kshr.getHeadersToLookUp()) {
						//inside header maybe different encoding
						String contentOfHeader = MimeUtility.convertHeaderAccordingToInsideEncoding(header.getValue()).trim().toLowerCase();
						if (kshr.isPresent()) {
							if (headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
								//we found the header
								if (kshr.isFilterOut() && StringUtils.isPatternInText(kshr.getKeywordToSerch().trim().toLowerCase().getBytes(),contentOfHeader.getBytes())) {
									//we found in appropriate header the searched pattern
									shouldbeDropped=true;
								}
							}
						} else {
							if (headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
								//we found appriopriate header
								if (kshr.isFilterOut() && !StringUtils.isPatternInText(kshr.getKeywordToSerch().trim().toLowerCase().getBytes(),contentOfHeader.getBytes())) {
									//we didn't find in appropriate header the searched pattern
									shouldbeDropped=true;
								}
							}
						}
					}
					break;
				default:
					break;
				}
			}
		}
		
		//check if headerPresenceMap contains for every rule true
		
		
	}
	
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	
	public void setMessage(MimeMessage message) {
		this.message = message;
	}
	
	public static void main(String[] args) {
		
		Configuration c = new Configuration("configuration/configuration/example_configuration_file.con");
		Analizer analizer = new Analizer(c.getRules());
		analizer.analize();
		
	}
	
}
