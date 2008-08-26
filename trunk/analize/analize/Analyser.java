package analize;

import helpers.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mail.MimeMessage;
import mail.MimeMessageHeader;
import mail.MimeMessageHeaders;
import mail.MimeMultiPart;
import mail.MimePart;
import mail.Part;
import mail.util.MimeUtility;
import mail.util.StringUtils;

import configuration.Configuration;

public class Analyser {
	
	List<Rule> rules;
	
	List<Rule> droppingRules = new ArrayList<Rule>();
	
	MimeMessage message;

	public Analyser() {}
	
	public Analyser(List<Rule> rules) {
		this.rules=rules;
	}
	
	/**
	 * Analyses message according to rules
	 * and message
	 * It decides if message should be dropped (filter out)
	 * 
	 */
	public void analize() {
		//clearing dropping rules
		this.droppingRules.clear();
		//analysing new rules
		analizeParts();
	}
	
	private void analizeParts() {
		
		Part p = this.message.getPart();
		List<Rule> droppingRules = analizePart(p, true);
		
		for (Rule rule : droppingRules) {
			System.out.println("Droping message cause of rule: \n" + rule.getName());
		}
		
		this.droppingRules.addAll(droppingRules);
	}
	
	private List<Rule> analizePart(Part part, boolean isRootPart) {
		
		List<Rule> ret = new ArrayList<Rule>();
		
		//analizing headers
		ret.addAll(analizeHeaders(part.getHeaders(),isRootPart));
		
		//analizing other rules
		ret.addAll(analizePartRules(part, isRootPart));
		
		if (part instanceof MimeMultiPart) {
			MimeMultiPart multipart = (MimeMultiPart) part;
			// recursive analizing			
			for (Part p : multipart.getParts())
				ret.addAll(analizePart(p, false));
		}
		return ret;
	}
	
	/**
	 * Analyses given part according to rules that applies to 
	 * parts
	 * @param part - given part
	 * @param isRootPart - is part the main part (root one)
	 * @return
	 */
	private List<Rule> analizePartRules(Part part, boolean isRootPart) {
		
		List<Rule> ret = new ArrayList<Rule>();
		
		for (Rule rule : rules) {
			switch (rule.ruleType) {
			
			case MAX_SIZE_PART:
				
				if (isRootPart) {
					MaxPartSizeRule maxPartSizeRule = (MaxPartSizeRule) rule;
					if (!maxPartSizeRule.isFilterOut()) break;
					int messageSize = 0;
					if (part instanceof MimeMultiPart) {
						MimeMultiPart multipart = (MimeMultiPart) part;
						for (Part p : multipart.getParts()) {
							messageSize = p.getContent().length;
//							System.out.println("msize:" + messageSize);
							if (messageSize>maxPartSizeRule.getMaxSizeInKilobytes()*1024 && maxPartSizeRule.isPresent())
								ret.add(maxPartSizeRule);
							else if (messageSize<maxPartSizeRule.getMaxSizeInKilobytes()*1024 && !maxPartSizeRule.isPresent())
								ret.add(maxPartSizeRule);
						}
					} else if (part instanceof MimePart) {
						MimePart mp = (MimePart) part;
						messageSize = mp.getContent().length;
//						System.out.println("msize:" + messageSize);
						if (messageSize>maxPartSizeRule.getMaxSizeInKilobytes()*1024 && maxPartSizeRule.isPresent())
							ret.add(maxPartSizeRule);
						else if (messageSize<maxPartSizeRule.getMaxSizeInKilobytes()*1024 && !maxPartSizeRule.isPresent())
							ret.add(maxPartSizeRule);
					}
				}
				
				break;
			}
		}
		
		
		return ret;
	}
	
	/**
	 * Analyses given headers according to set rules in analizer
	 * @param mimeMessageHeaders - given rules
	 * @return list of rules that causes messages to be dropped
	 */
	private List<Rule> analizeHeaders(MimeMessageHeaders mimeMessageHeaders, boolean isRootPart) {
		
		List<Rule> dropingRules = new ArrayList<Rule>();
		HashMap< Pair<Rule, String>, Boolean> headerPresenceMap = new HashMap<Pair<Rule, String>, Boolean>();
		
		for (MimeMessageHeader header : mimeMessageHeaders.getHeaders()) {
			for (Rule rule : rules) {
				switch (rule.ruleType) {
				case HEADER_PRESENCE:
					MimeMessageHeaderPresenceRule hpr = (MimeMessageHeaderPresenceRule) rule;
					for (String headerName : hpr.getHeaders()) {
						if (hpr.isPresent()) { 
							if (hpr.isFilterOut() && headerName.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
								//filtering all messages that contains some header (headerString)
								dropingRules.add(rule);
							}
						} else {
							if (hpr.isFilterOut()) {
								if (headerName.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
									// if message contains this header we mark it and at the end we check if 
									// all headers were marked
									headerPresenceMap.put(new Pair<Rule,String>(rule,headerName), new Boolean(true));
								} else {
									// here we mark that we haven't encounter this header yet
									if (headerPresenceMap.get(new Pair<Rule,String>(rule,headerName)) == null)
										headerPresenceMap.put(new Pair<Rule,String>(rule,headerName), new Boolean(false));
									else {
										if (!headerPresenceMap.get(new Pair<Rule,String>(rule,headerName)))
											headerPresenceMap.put(new Pair<Rule,String>(rule,headerName), new Boolean(false));
									}
								}
							}
						}
					}
					break;
				case LOOK_KEYWORD:
					
					//isCheckPartHeaders=true rootPart=true -> break
					//isCheckPartHeaders=false rootPart=true -> go
					//isCheckPartHeaders=true rootPart=false -> go
					//isCheckPartHeaders=false rootPart=false -> break
					KeywordSearchHeaderRule kshr = (KeywordSearchHeaderRule) rule;
					if ( (kshr.isCheckPartHeaders() && isRootPart ) ||
						 (!kshr.isCheckPartHeaders() && !isRootPart )	
						) break;
						
					for (String headerString : kshr.getHeadersToLookUp()) {
						//inside header maybe different encoding
						String contentOfHeader = MimeUtility.convertHeaderAccordingToInsideEncoding(header.getValue()).trim().toLowerCase();
						if (kshr.isPresent()) {
							if (headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
								//we found the header
								if (kshr.isFilterOut() && StringUtils.isStringPatternInStringText(kshr.getKeywordToSerch().trim().toLowerCase(),contentOfHeader)) {
									//we found in appropriate header the searched pattern
									dropingRules.add(rule);
								}
							}
						} else {
							if (headerString.trim().toLowerCase().equals(header.getName().trim().toLowerCase())) {
								//we found appriopriate header
								if (kshr.isFilterOut() && !StringUtils.isStringPatternInStringText(kshr.getKeywordToSerch().trim().toLowerCase(),contentOfHeader)) {
									//we didn't find in appropriate header the searched pattern
									dropingRules.add(rule);
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
		for (Pair<Rule, String> pair : headerPresenceMap.keySet()) {
			if (!headerPresenceMap.get(pair)) {
				dropingRules.add(pair.getFirstObject());
				break;
			}
		}
		return dropingRules;
	}
	
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	
	public void setMessage(MimeMessage message) {
		this.message = message;
	}
	
	public static void main(String[] args) {
		
		Configuration c = new Configuration("configuration/configuration/example_configuration_file.con");
//		Analizer analizer = new Analizer(c.getRules());
//		analizer.analize();
		
		Rule r  = c.getRules().get(0);
		HashMap< Pair<Rule, String>, String> headerPresenceMap = new HashMap<Pair<Rule, String>, String>();
//		Pair<Rule, String> pair = new Pair<Rule, String>(r, "dupa");
		headerPresenceMap.put(new Pair<Rule, String>(r, "dupa"),"papla");
//		headerPresenceMap.put(pair,"papla");
		
//		if (headerPresenceMap.containsKey(pair))
		if (headerPresenceMap.containsKey(new Pair<Rule, String>(r, "dupa")))
			System.out.println("chuj");
		
		
		
	}

	public List<Rule> getDroppingRules() {
		return droppingRules;
	}

	
}
