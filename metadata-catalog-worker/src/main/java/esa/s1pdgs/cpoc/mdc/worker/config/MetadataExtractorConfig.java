package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Olivier Bex-Chauvet
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mdextractor")
public class MetadataExtractorConfig {
    /**
     * Map of all the overlap for the different slice type
     */
    private Map<String, Float> typeOverlap;

    /**
     * Map of all the length for the different slice type
     */
    private Map<String, Float> typeSliceLength;

    private String xsltDirectory;
    
    private Map<String,String> packetStoreTypes;
    private Map<String,String> packetstoreTypeTimelinesses;
    private List<String> timelinessPriorityFromHighToLow;
    
    public MetadataExtractorConfig() {
    }

    /**
     * @return the typeOverlap
     */
    public Map<String, Float> getTypeOverlap() {
        return typeOverlap;
    }

    /**
     * @param typeOverlap
     *            the typeOverlap to set
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
     * @param typeSliceLength
     *            the typeSliceLength to set
     */
    public void setTypeSliceLength(Map<String, Float> typeSliceLength) {
        this.typeSliceLength = typeSliceLength;
    }

    /**
     * @return the xsltDirectory
     */
    public String getXsltDirectory() {
        return xsltDirectory;
    }

    /**
     * @param xsltDirectory
     *            the xsltDirectory to set
     */
    public void setXsltDirectory(String xsltDirectory) {
        this.xsltDirectory = xsltDirectory;
    }

	/**
	 * @return the packetStoreTypes
	 */
	public Map<String,String> getPacketStoreTypes() {
		return packetStoreTypes;
	}

	/**
	 * @param packetStoreTypes the packetStoreTypes to set
	 */
	public void setPacketStoreTypes(Map<String,String> packetStoreTypes) {
		this.packetStoreTypes = packetStoreTypes;
	}

	/**
	 * @return the packetstoreTypeTimelinesses
	 */
	public Map<String,String> getPacketstoreTypeTimelinesses() {
		return packetstoreTypeTimelinesses;
	}

	/**
	 * @param packetstoreTypeTimelinesses the packetstoreTypeTimelinesses to set
	 */
	public void setPacketstoreTypeTimelinesses(Map<String,String> packetstoreTypeTimelinesses) {
		this.packetstoreTypeTimelinesses = packetstoreTypeTimelinesses;
	}

	/**
	 * @return the timelinessPriorityFromHighToLow
	 */
	public List<String> getTimelinessPriorityFromHighToLow() {
		return timelinessPriorityFromHighToLow;
	}

	/**
	 * @param timelinessPriorityFromHighToLow the timelinessPriorityFromHighToLow to set
	 */
	public void setTimelinessPriorityFromHighToLow(List<String> timelinessPriorityFromHighToLow) {
		this.timelinessPriorityFromHighToLow = timelinessPriorityFromHighToLow;
	}
}
