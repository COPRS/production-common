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
	 * Settings for the RangeCover logic
	 * 
	 * @author Julian Kaping
	 */
	public static class RangeCoverSettings {
		private String productType;
		private long anxOffsetInS;
		private long rangeLengthInS;

		public String getProductType() {
			return productType;
		}

		public void setProductType(String productType) {
			this.productType = productType;
		}

		public long getAnxOffsetInS() {
			return anxOffsetInS;
		}

		public void setAnxOffsetInS(long anxOffsetInS) {
			this.anxOffsetInS = anxOffsetInS;
		}

		public long getRangeLengthInS() {
			return rangeLengthInS;
		}

		public void setRangeLengthInS(long rangeLengthInS) {
			this.rangeLengthInS = rangeLengthInS;
		}

		@Override
		public String toString() {
			return "RangeCoverSettings [productType=" + productType + ", anxOffsetInS=" + anxOffsetInS
					+ ", rangeLengthInS=" + rangeLengthInS + "]";
		}
	}

	/**
	 * map containing the product types for each tasktable on which the mpcSearch
	 * should be executed
	 * 
	 * key is the processor name (ex. S3A_OL1), values are the product types (ex.
	 * OL_0_EFR___)
	 */
	private Map<String, List<String>> mpcSearch = new HashMap<>();

	/**
	 * map containing the settings for the rangeCover logic
	 * 
	 * key is the processor name (ex. S3A_OL1), values are the product types (ex.
	 * OL_0_EFR___), offset and rangeLength (in seconds)
	 */
	private Map<String, RangeCoverSettings> rangeSearch = new HashMap<>();

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

	public Map<String, RangeCoverSettings> getRangeSearch() {
		return rangeSearch;
	}

	/**
	 * Returns if the RangeCoverSearch should be executed for the given productType
	 * 
	 * @param processorName tasktable processor name
	 * @param productType   productType to check (ex. OL_1_EFR___)
	 * @return true if the logic should be applied
	 */
	public boolean isRangeSearchActiveForProductType(String processorName, String productType) {
		RangeCoverSettings rcSettings = rangeSearch.get(processorName);
		if (rcSettings != null) {
			return rcSettings.getProductType().equals(productType);
		}
		return false;
	}

	public void setRangeSearch(Map<String, RangeCoverSettings> rangeSearch) {
		this.rangeSearch = rangeSearch;
	}

	public List<String> getOlciCalibration() {
		return olciCalibration;
	}

	public void setOlciCalibration(List<String> olciCalibration) {
		this.olciCalibration = olciCalibration;
	}
}
