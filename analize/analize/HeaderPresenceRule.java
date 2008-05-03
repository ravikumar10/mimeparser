package analize;

/**
 * Class represents rule that filter out messages that
 * doesn't contain (or contain) some header
 * @author zbychu
 *
 */
public class HeaderPresenceRule extends Rule {
	
	/**
	 * Which header we're looking for
	 */
	protected String headerName;
	
	public HeaderPresenceRule(String header, String name, boolean exclude) {
		this.headerName=name;
		if (name==null)
			this.name="search for header: " + header;
		else
			this.name=name;
		this.name=name;
		this.ruleType=RuleType.HEADER_PRESENCE;
		this.exclude=exclude;
	}
	
	
	
}
