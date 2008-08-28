package configuration;

public class ConfigurationValidationException extends Exception {
	
	private String reason;
	
	public ConfigurationValidationException(String reason) {
		this.reason = reason;
	}
	
	@Override
	public String toString() {
		return this.reason;
	}
}
