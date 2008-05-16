package analize;

/**
 * Class represents a single search rule for MimeMessageHeader
 * which tries to filter out message that doesn't contain 
 * or contains some keyword
 * @author zbychu
 *
 */
public class KeywordSearchHeaderRule extends Rule {
	
	/**
	 * keyword which we're looking for
	 */
	String keywordToSerch;
	
	/**
	 * list of header to which we are trying to
	 * apply our filter
	 * 
	 */
	String[] headersToLookUp;
	
	/**
	 * Constructor 
	 * 
	 * @param name - name of the rule (like rule01)
	 * @param keyword - keyword which we're lookig for
	 * @param headers - list of headers to look for
	 * @param present - if we're searching for headers that contains this word (true) or not (false)
	 * @param filterOut - should this rule cause message to be dropped
	 * are search through
	 */
	public KeywordSearchHeaderRule(String name, String[] headers, String keyword, boolean present, boolean filterOut) {
		this.name=name;
		this.keywordToSerch=keyword;
		this.headersToLookUp=headers;
		this.ruleType=RuleType.HOST_LOOK_KEYWORD;
		this.filterOut=filterOut;
		this.present=present;
	}

	public String getKeywordToSerch() {
		return keywordToSerch;
	}
	
	public String[] getHeadersToLookUp() {
		return headersToLookUp;
	}

	@Override
	public String toString() {
		String headersString = "";
		for (String h : headersToLookUp) 
			headersString+=h + " , ";
		return "HEADER_KEYWORD_SEARCH: " + name + " keyword: " + keywordToSerch + " present: " + present + " filter_out: " + filterOut + " headers: " + headersString;

	}
	
}
