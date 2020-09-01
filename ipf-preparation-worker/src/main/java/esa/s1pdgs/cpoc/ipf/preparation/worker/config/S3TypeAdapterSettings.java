package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Additional settings used to configure the S3 type adapter
 * 
 * @author Julian Kaping
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "s3-type-adapter")
public class S3TypeAdapterSettings {

	/**
	 * map containing the product types for each tasktable on which the mpcSearch
	 * should be executed
	 * 
	 * key is the processor name (ex. S3A_OL1), values are the product types (ex.
	 * OL_0_EFR___)
	 */
	private Map<String, List<String>> mpcSearch = new HashMap<>();

	/**
	 * list of processors on which the additional logic for OLCI calibration should
	 * be executed
	 */
	private List<String> olciCalibration = new ArrayList<>();

	public Map<String, List<String>> getMpcSearch() {
		return mpcSearch;
	}

	/**
	 * Get the list of product types for a given processor name. In case there is no
	 * entry in the map an empty list is returned
	 * 
	 * @param key processor name for which the product types should be retrieved
	 * @return list of product types, or empty list if no entry in map
	 */
	public List<String> getMpcSearch(String key) {
		List<String> result = mpcSearch.get(key);
		if (result == null) {
			return new ArrayList<>();
		}
		return result;
	}

	public void setMpcSearch(Map<String, List<String>> mpcSearch) {
		this.mpcSearch = mpcSearch;
	}

	public List<String> getOlciCalibration() {
		return olciCalibration;
	}

	public void setOlciCalibration(List<String> olciCalibration) {
		this.olciCalibration = olciCalibration;
	}
}
