package analize;

/**
 * Class represends rule which is used while analizing messages
 * Rules are created from configuration file (can also be 
 * made by hand for tests)
 * @author zbychu
 *
 */

public class Rule {

	public enum RuleType { HEADER_PRESENCE, LOOK_KEYWORD, HOST_LOOK_KEYWORD, 
		PHRASE_LOOK_KEYWORD, ADDRESS_LOOK_KEYWORD, MAX_SIZE_PART, ATTACHMENT_ANALIZE }
	
	protected RuleType ruleType; 
	
	/**
	 * rule name
	 */
	protected String name;
	
	/**
	 * content of the rule - from config file
	 * maybe i'll put some parse method here
	 */
	protected String content;
	
	/**
	 * tells if messages applies to this rule shit be filtered out
	 * if true message ARE filtered out
	 */
	protected boolean filterOut;
	
	/**
	 * tells if we are looking for messages that contains this rule
	 * or not
	 * For example:
	 * if we're looking for content that contains some keyword (than
	 * present is true) or content that doesn't have it (false)
	 * 
	 * look for more explanation in constructor of appropriate rule
	 */
	protected boolean present;
	
	public RuleType getRuleType() {
		return ruleType;
	}


	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public boolean isFilterOut() {
		return filterOut;
	}

	public boolean isPresent() {
		return present;
	}


	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Rule) {
			Rule r = (Rule) obj;
			if (this.ruleType == r.ruleType && this.getName().equals(r.getName())) return true;
		}
		
		return false;
	}
}
