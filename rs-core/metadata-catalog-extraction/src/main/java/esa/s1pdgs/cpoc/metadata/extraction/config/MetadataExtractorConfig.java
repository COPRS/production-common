/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.metadata.extraction.config;

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
    
    private Map<String,String> fieldTypes;
    
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

    public Map<String,String> getFieldTypes() {
    	return fieldTypes;
    }
    
    public void setFieldTypes(Map<String,String> fieldTypes) {
    	this.fieldTypes = fieldTypes;
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
