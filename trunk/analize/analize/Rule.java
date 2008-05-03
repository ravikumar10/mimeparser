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
		PHRASE_LOOK_KEYWORD, ADDRESS_LOOK_KEYWORD }
	
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
	 * if true message ARE NOT filtered out
	 */
	protected boolean exclude;
	
	@Override
	public String toString() {
		return name;
	}
	
}
