package esa.s1pdgs.cpoc.mdc.worker.extraction;

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

}
