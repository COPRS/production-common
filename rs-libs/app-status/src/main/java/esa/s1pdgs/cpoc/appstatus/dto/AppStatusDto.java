package esa.s1pdgs.cpoc.appstatus.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

public class AppStatusDto {
	/**
     * Status of application
     */
    private AppState status;
    
    /**
     * Category
     */
    private ProductCategory category;
    
    /**
     * Sub statuses
     */
    private Map<ProductCategory, AppStatusDto> subStatuses;

    /**
     * Number of milliseconds passed for the last modification
     */
    private Long timeSinceLastChange;

    /**
     * Number of error for the last modification of status
     */
    private Integer errorCounter;
    
    public AppStatusDto() {
    	this(null);
    }

    public AppStatusDto(final ProductCategory category, final AppState state) {
    	this();
    	this.category = category; 
    }

    public AppStatusDto(final AppState state) {
        this.status = state;
        this.subStatuses = null;
        this.timeSinceLastChange = null;
        this.errorCounter = null;
        this.category = null;
    }

    public AppState getStatus() {
        return status;
    }
    
	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}
	
	public Map<ProductCategory, AppStatusDto> getSubStatuses() {
		return subStatuses;
	}
	
	public void addSubStatuses(AppStatusDto subStatusDto) {
		if (null == subStatuses) {
			this.subStatuses = new HashMap<>();
		}
		this.subStatuses.put(subStatusDto.getCategory(), subStatusDto);
	}

    public void setStatus(final AppState status) {
        this.status = status;
    }

    public Long getTimeSinceLastChange() {
        return timeSinceLastChange;
    }

    public void setTimeSinceLastChange(final Long timeLastChange) {
        this.timeSinceLastChange = timeLastChange;
    }

    public Integer getErrorCounter() {
        return errorCounter;
    }

    public void setErrorCounter(final Integer errorCounter) {
        this.errorCounter = errorCounter;
    }

    @Override
    public String toString() {
        return String.format(
                "{category: %s, status: %s, timeSinceLastChange: %d, errorCounter: %d, subStatus: %s}",
                category, status, timeSinceLastChange, errorCounter, subStatuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, status, timeSinceLastChange, errorCounter, subStatuses);
    }

    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
        	AppStatusDto other = (AppStatusDto) obj;
            // field comparison
            ret = Objects.equals(category, other.category)
            		&& Objects.equals(status, other.status)
                    && Objects.equals(timeSinceLastChange, other.timeSinceLastChange)
                    && Objects.equals(errorCounter, other.errorCounter)
                    && Objects.equals(subStatuses, other.subStatuses);
        }
        return ret;
    }

}
