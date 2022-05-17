package esa.s1pdgs.cpoc.preparation.worker.config.type;

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
public class S3TypeAdapterProperties {

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
	 * Settings for the MultipleCoverSearch
	 * 
	 * @author Julian Kaping
	 */
	public static class MPCSearchSettings {
		private List<String> productTypes;
		private boolean disableFirstLastWaiting = false;
		private double gapThreshold = 0.0;

		public List<String> getProductTypes() {
			return productTypes;
		}

		public void setProductTypes(List<String> productTypes) {
			this.productTypes = productTypes;
		}

		public boolean isDisableFirstLastWaiting() {
			return disableFirstLastWaiting;
		}

		public void setDisableFirstLastWaiting(boolean disableFirstLastWaiting) {
			this.disableFirstLastWaiting = disableFirstLastWaiting;
		}
		
		public double getGapThreshold() {
			return gapThreshold;
		}

		public void setGapThreshold(double gapThreshold) {
			this.gapThreshold = gapThreshold;
		}

		@Override
		public String toString() {
			return "MPCSearchSettings [productTypes=" + productTypes + ", disableFirstLastWaiting="
					+ disableFirstLastWaiting + ", gapThreshold=" + gapThreshold + "]";
		}
	}

	/**
	 * map containing the product types for each tasktable on which the mpcSearch
	 * should be executed
	 * 
	 * key is the processor name (ex. S3A_OL1), values are the product types (ex.
	 * OL_0_EFR___) and information if the first and last granules should be waiting
	 * for neighbors
	 */
	private Map<String, MPCSearchSettings> mpcSearch = new HashMap<>();

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

	/**
	 * map for dynamic process parameters which are not part of the metadata (ex.
	 * facilityName)
	 */
	private Map<String, String> dynProcParams = new HashMap<>();

	/**
	 * Enable optional outputs for tasktable key = task table name value = list of
	 * optional outputs to enable
	 */
	private Map<String, List<String>> optionalOutputs = new HashMap<>();

	public Map<String, MPCSearchSettings> getMpcSearch() {
		return mpcSearch;
	}

	/**
	 * Returns if the MultipleProductCoverSearch should be executed for the given
	 * productType
	 * 
	 * @param processorName tasktable processor name
	 * @param productType   productType to check (ex. OL_1_EFR___)
	 * @return true if the logic should be applied
	 */
	public boolean isMPCSearchActiveForProductType(String processorName, String productType) {
		MPCSearchSettings mpcSettings = mpcSearch.get(processorName);
		if (mpcSettings != null) {
			return mpcSettings.getProductTypes().contains(productType);
		}
		return false;
	}

	public void setMpcSearch(Map<String, MPCSearchSettings> mpcSearch) {
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

	public Map<String, String> getDynProcParams() {
		return dynProcParams;
	}

	public void setDynProcParams(Map<String, String> dynProcParams) {
		this.dynProcParams = dynProcParams;
	}

	public Map<String, List<String>> getOptionalOutputs() {
		return optionalOutputs;
	}

	public void setOptionalOutputs(Map<String, List<String>> optionalOutputs) {
		this.optionalOutputs = optionalOutputs;
	}

	/**
	 * Get the list of additional output types for a given processor name. In case
	 * there is no entry in the map an empty list is returned
	 * 
	 * @param key processor name for which the additional output types should be
	 *            retrieved
	 * @return list of additional output types, or empty list if no entry in map
	 */
	public List<String> getOptionalOutputsForTaskTable(String taskTable) {
		List<String> result = optionalOutputs.get(taskTable);
		if (result == null) {
			return new ArrayList<>();
		}
		return result;
	}
}
