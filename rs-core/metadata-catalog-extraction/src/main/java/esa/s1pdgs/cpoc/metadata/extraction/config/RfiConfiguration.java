package esa.s1pdgs.cpoc.metadata.extraction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "rfi")
public class RfiConfiguration {
	
	private boolean enabled = false;
	private String rfiDirectoryName = "rfi";
	private String annotationDirectoryName = "annotation";
	private String hPolarisationRfiFilePattern = "rfi-s1.*-[hv]h-.*\\.xml";
	private String vPolarisationRfiFilePattern = "rfi-s1.*-[hv]v-.*\\.xml";
	private String annotationFilePattern = "s1.*-[hv][hv]-.*\\.xml";
	
	
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getRfiDirectoryName() {
		return rfiDirectoryName;
	}
	public void setRfiDirectoryName(String rfiDirectoryName) {
		this.rfiDirectoryName = rfiDirectoryName;
	}
	public String getAnnotationDirectoryName() {
		return annotationDirectoryName;
	}
	public void setAnnotationDirectoryName(String annotationDirectoryName) {
		this.annotationDirectoryName = annotationDirectoryName;
	}
	public String gethPolarisationRfiFilePattern() {
		return hPolarisationRfiFilePattern;
	}
	public void sethPolarisationRfiFilePattern(String hPolarisationRfiFilePattern) {
		this.hPolarisationRfiFilePattern = hPolarisationRfiFilePattern;
	}
	public String getvPolarisationRfiFilePattern() {
		return vPolarisationRfiFilePattern;
	}
	public void setvPolarisationRfiFilePattern(String vPolarisationRfiFilePattern) {
		this.vPolarisationRfiFilePattern = vPolarisationRfiFilePattern;
	}
	public String getAnnotationFilePattern() {
		return annotationFilePattern;
	}
	public void setAnnotationFilePattern(String annotationFilePattern) {
		this.annotationFilePattern = annotationFilePattern;
	}
	

}
