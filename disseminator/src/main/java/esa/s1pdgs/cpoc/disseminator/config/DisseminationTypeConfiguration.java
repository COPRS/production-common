package esa.s1pdgs.cpoc.disseminator.config;

public class DisseminationTypeConfiguration
{
	private String family;
	private String regex;
	
	public String getFamily() {
		return family;
	}
	
	public void setFamily(String family) {
		this.family = family;
	}
	
	public String getRegex() {
		return regex;
	}
	
	public void setRegex(String regex) {
		this.regex = regex;
	}

	@Override
	public String toString() {
		return "DisseminationTypeConfiguration [family=" + family + ", regex=" + regex + "]";
	}
}