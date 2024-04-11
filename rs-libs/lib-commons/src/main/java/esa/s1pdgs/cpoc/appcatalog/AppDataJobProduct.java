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

package esa.s1pdgs.cpoc.appcatalog;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Object used for persisting useful information of input product for a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJobProduct {

    /**
     * Time formatter used
     */
    public final static DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
    
    public static final String DEFAULT_PROCESS_MODE = "NOMINAL";
    public static final String DEFAULT_POLARISTATION = "NONE";

	private Map<String,Object> metadata = new LinkedHashMap<>();    
	private Map<String, List<AppDataJobFile>> inputs = new LinkedHashMap<>();  
	
	public Map<String, List<AppDataJobFile>> getInputs() {
		return inputs;
	}
	
	public void setInputs(final Map<String, List<AppDataJobFile>> inputProducts) {
		this.inputs = inputProducts;
	}
	
	public Map<String, Object> getMetadata() {
		return metadata;
	}
	
	public void setMetadata(final Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputs, metadata);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AppDataJobProduct other = (AppDataJobProduct) obj;
		return Objects.equals(inputs, other.inputs) && 
				Objects.equals(metadata, other.metadata);
	}

	@Override
	public String toString() {
		return "AppDataJobProduct [metadata=" + metadata + ", inputs=" + inputs + "]";
	}
}
