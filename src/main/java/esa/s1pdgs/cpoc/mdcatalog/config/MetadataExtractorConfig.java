package esa.s1pdgs.cpoc.mdcatalog.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author Olivier Bex-Chauvet
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mdextractor")
public class MetadataExtractorConfig {
	
	private String typeoverlapstr;
	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;
	
	private String typeslicelengthstr;
	/**
	 * Map of all the length for the different slice type
	 */
	private Map<String, Float> typeSliceLength;
	
	private String xsltDirectory;
	
	public MetadataExtractorConfig() {
		this.typeOverlap = new HashMap<>();
		this.typeSliceLength = new HashMap<>();
	}
	
	/**
	 * Function which construct the 2 hashMaps typeOverlap and typeSliceLength
	 */
	@PostConstruct
	public void initMaps() {
		// TypeOverlap
		if (!StringUtils.isEmpty(this.typeoverlapstr)) {
			String[] paramsTmp = this.typeoverlapstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.typeOverlap.put(tmp[0], Float.valueOf(tmp[1]));
					}
				}
			}
		}
		// TypeSliceLength
		if (!StringUtils.isEmpty(this.typeslicelengthstr)) {
			String[] paramsTmp = this.typeslicelengthstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.typeSliceLength.put(tmp[0], Float.valueOf(tmp[1]));
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MetadataExtractorConfig [typeoverlapstr=" + typeoverlapstr + ", typeOverlap=" + typeOverlap
				+ ", typeslicelengthstr=" + typeslicelengthstr + ", typeSliceLength=" + typeSliceLength + "]";
	}

	/**
	 * @return the typeOverlap
	 */
	public Map<String, Float> getTypeOverlap() {
		return typeOverlap;
	}

	/**
	 * @param typeOverlap the typeOverlap to set
	 */
	public void setTypeOverlap(Map<String, Float> typeOverlap) {
		this.typeOverlap = typeOverlap;
	}

	/**
	 * @return the typeSliceLength
	 */
	public Map<String, Float> getTypeSliceLength() {
		return typeSliceLength;
	}

	/**
	 * @param typeSliceLength the typeSliceLength to set
	 */
	public void setTypeSliceLength(Map<String, Float> typeSliceLength) {
		this.typeSliceLength = typeSliceLength;
	}

	/**
	 * @return the typeoverlapstr
	 */
	public String getTypeoverlapstr() {
		return typeoverlapstr;
	}

	/**
	 * @param typeoverlapstr the typeoverlapstr to set
	 */
	public void setTypeoverlapstr(String typeoverlapstr) {
		this.typeoverlapstr = typeoverlapstr;
	}

	/**
	 * @return the typeslicelengthstr
	 */
	public String getTypeslicelengthstr() {
		return typeslicelengthstr;
	}

	/**
	 * @param typeslicelengthstr the typeslicelengthstr to set
	 */
	public void setTypeslicelengthstr(String typeslicelengthstr) {
		this.typeslicelengthstr = typeslicelengthstr;
	}

	/**
	 * @return the xsltDirectory
	 */
	public String getXsltDirectory() {
		return xsltDirectory;
	}

	/**
	 * @param xsltDirectory the xsltDirectory to set
	 */
	public void setXsltDirectory(String xsltDirectory) {
		this.xsltDirectory = xsltDirectory;
	}
	
}
