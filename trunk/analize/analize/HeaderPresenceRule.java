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
	protected String[] headers;
	
	/**
	 * @param name - name of the rule (like rule01)
	 * @param header - names of the headers we're looking for
	 * @param present - if true means that we are filtering message that
	 * contains this header; false means that we are filtering message
	 * that doesn't contain this header
	 * @param filterOut - should this rule cause message to be dropped
	 */
	public HeaderPresenceRule(String name, String[] headers, boolean present, boolean filterOut) {
		this.headers=headers;
		this.name=name;
		this.ruleType=RuleType.HEADER_PRESENCE;
		this.filterOut=filterOut;
		this.present=present;
	}

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}
	
	@Override
	public String toString() {
		String headersString = "";
		for (String h : headers) 
			headersString+=h + " , ";
		return "HEADER_PRESENCE: " + name + " present: " + present + " filter_out: " + filterOut + " headers: " + headersString;
	}

}
