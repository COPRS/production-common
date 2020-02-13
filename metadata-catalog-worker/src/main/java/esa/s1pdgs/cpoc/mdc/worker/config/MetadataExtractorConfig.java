package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
    
    private List<String> timelinessPriorityFromHighToLow;

    @NestedConfigurationProperty
    PacketStoreType packetStoreType; 
    
    public static class PacketStoreType {
    	private Map<String, String> s1a;
    	private Map<String, String> s1b;
    	private Map<String, String> toTimeliness;

    	public Map<String, String> getS1a() {
			return s1a;
		}

    	public void setS1a(Map<String, String> s1a) {
			this.s1a = s1a;
		}
		
    	public Map<String, String> getS1b() {
			return s1b;
		}

    	public void setS1b(Map<String, String> s1b) {
			this.s1b = s1b;
		}

    	public Map<String, String> getToTimeliness() {
			return toTimeliness;
		}

    	public void setToTimeliness(Map<String, String> toTimeliness) {
			this.toTimeliness = toTimeliness;
		}
    	
    }
    
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
	 * @return the packetStoreType
	 */
	public PacketStoreType getPacketStoreType() {
		return packetStoreType;
	}

	/**
	 * @param packetStoreType the packetStoreType to set
	 */
	public void setPacketStoreType(PacketStoreType packetStoreType) {
		this.packetStoreType = packetStoreType;
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
