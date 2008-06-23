package analize;

public class MaxPartSizeRule extends Rule {
	
	private int maxSizeInKilobytes;
	
	public MaxPartSizeRule(String name, int maxSizeInKilobytes, boolean present, boolean filterOut) {
		this.name = name;
		this.present = present;
		this.filterOut = filterOut;
		this.maxSizeInKilobytes = maxSizeInKilobytes;
		this.ruleType = RuleType.MAX_SIZE_PART;
	}
	
	public int getMaxSizeInKilobytes() {
		return maxSizeInKilobytes;
	}
	
}
