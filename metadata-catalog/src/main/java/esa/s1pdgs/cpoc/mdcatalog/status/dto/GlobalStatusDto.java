package esa.s1pdgs.cpoc.mdcatalog.status.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Global status of the application
 * 
 * @author Viveris Technologies
 *
 */
public class GlobalStatusDto {

	/**
	 * Status
	 */
	private AppState globalStatus;

	/**
	 * Status per category
	 */
	private Map<ProductCategory, StatusPerCategoryDto> statusPerCategory;

	/**
	 * 
	 */
	public GlobalStatusDto() {
		super();
		this.statusPerCategory = new HashMap<>();
	}

	/**
	 * @param globalStatus
	 */
	public GlobalStatusDto(AppState globalStatus) {
		super();
		this.statusPerCategory = new HashMap<>();
		this.globalStatus = globalStatus;
	}

	/**
	 * @return the globalStatus
	 */
	public AppState getGlobalStatus() {
		return globalStatus;
	}

	/**
	 * @param globalStatus
	 *            the globalStatus to set
	 */
	public void setGlobalStatus(AppState globalStatus) {
		this.globalStatus = globalStatus;
	}

	/**
	 * @return the statusPerCategory
	 */
	public Map<ProductCategory, StatusPerCategoryDto> getStatusPerCategory() {
		return statusPerCategory;
	}

	/**
	 * @param statusPerCategory
	 *            the statusPerCategory to set
	 */
	public void addStatusPerCategory(StatusPerCategoryDto statusPerCategory) {
		this.statusPerCategory.put(statusPerCategory.getCategory(), statusPerCategory);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{globalStatus: %s, statusPerCategory: %s}", globalStatus, statusPerCategory);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(globalStatus, statusPerCategory);
	}

	/**
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			GlobalStatusDto other = (GlobalStatusDto) obj;
			// field comparison
			ret = globalStatus == other.globalStatus && Objects.equals(statusPerCategory, other.statusPerCategory);
		}
		return ret;
	}

}
