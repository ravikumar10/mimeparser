package analize;

import java.util.List;

/**
 * Class represents a single search rule for MimeMessageHeader
 * which tries to filter out message that doesn't contain 
 * or contains some keyword
 * @author zbychu
 *
 */
public class KeywordHeaderRule extends Rule {
	
	/**
	 * keyword which we're looking for
	 */
	String keywordToSerch;
	
	/**
	 * list of header to which we are trying to
	 * apply our filter
	 * 
	 */
	List<String> headersToLookUp;
	
	/**
	 * Constructor 
	 * 
	 * @param keyword - keyword which we're lookig for
	 * @param headers - list of headers if null all headers
	 * are search through
	 */
	public KeywordHeaderRule(String keyword, List<String> headers, String name, boolean exclude) {
		if (name==null)
			this.name="search for keyword: " + keyword;
		else
			this.name=name;
		this.keywordToSerch=keyword;
		this.headersToLookUp=headers;
		this.ruleType=RuleType.HOST_LOOK_KEYWORD;
		this.exclude=exclude;
	}
}
