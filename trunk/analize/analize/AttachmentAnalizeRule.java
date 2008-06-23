package analize;

public class AttachmentAnalizeRule extends Rule {
	
	private String fileExtension;
	
	public AttachmentAnalizeRule(String name, String fileExtension, boolean present, boolean filterOut) {
		this.name = name;
		this.present = present;
		this.filterOut = filterOut;
		this.fileExtension = fileExtension;
		this.ruleType = RuleType.ATTACHMENT_ANALIZE;
	}

	public String getFileExtension() {
		return fileExtension;
	}
	
}
