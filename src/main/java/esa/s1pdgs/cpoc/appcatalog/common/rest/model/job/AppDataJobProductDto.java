package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Object used for persisting useful information of input product for a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJobProductDto {

    /**
     * Session identifier
     */
    private String sessionId;

    /**
     * Product name
     */
    private String productName;

    /**
     * Satellite identifier: A or B
     */
    private String satelliteId;

    /**
     * Mission identifier: S1
     */
    private String missionId;

    /**
     * 
     */
    private Date startTime;

    /**
     * 
     */
    private Date stopTime;

    /**
     * List of raws for channel 1
     */
    private List<String> raws1;

    /**
     * List of raws for channel 2
     */
    private List<String> raws2;

    /**
     * Acquisition
     */
    private String acquisition;

    /**
     * 
     */
    public AppDataJobProductDto() {
        super();
        this.raws1 = new ArrayList<>();
        this.raws2 = new ArrayList<>();
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId
     *            the sessionId to set
     */
    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     *            the productName to set
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @return the satelliteId
     */
    public String getSatelliteId() {
        return satelliteId;
    }

    /**
     * @param satelliteId
     *            the satelliteId to set
     */
    public void setSatelliteId(final String satelliteId) {
        this.satelliteId = satelliteId;
    }

    /**
     * @return the missionId
     */
    public String getMissionId() {
        return missionId;
    }

    /**
     * @param missionId
     *            the missionId to set
     */
    public void setMissionId(final String missionId) {
        this.missionId = missionId;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the stopTime
     */
    public Date getStopTime() {
        return stopTime;
    }

    /**
     * @param stopTime
     *            the stopTime to set
     */
    public void setStopTime(final Date stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * @return the raws1
     */
    public List<String> getRaws1() {
        return raws1;
    }

    /**
     * @param raws1
     *            the raws1 to set
     */
    public void setRaws1(final List<String> raws1) {
        this.raws1 = raws1;
    }

    /**
     * @return the raws2
     */
    public List<String> getRaws2() {
        return raws2;
    }

    /**
     * @param raws2
     *            the raws2 to set
     */
    public void setRaws2(final List<String> raws2) {
        this.raws2 = raws2;
    }

    /**
     * @return the acquisition
     */
    public String getAcquisition() {
        return acquisition;
    }

    /**
     * @param acquisition
     *            the acquisition to set
     */
    public void setAcquisition(final String acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{sessionId: %s, productName: %s, satelliteId: %s, missionId: %s, startTime: %s, stopTime: %s, raws1: %s, raws2: %s, acquisition: %s}",
                sessionId, productName, satelliteId, missionId, startTime,
                stopTime, raws1, raws2, acquisition);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(sessionId, productName, satelliteId, missionId,
                startTime, stopTime, raws1, raws2, acquisition);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            AppDataJobProductDto other = (AppDataJobProductDto) obj;
            ret = Objects.equals(sessionId, other.sessionId)
                    && Objects.equals(productName, other.productName)
                    && Objects.equals(satelliteId, other.satelliteId)
                    && Objects.equals(missionId, other.missionId)
                    && Objects.equals(startTime, other.startTime)
                    && Objects.equals(stopTime, other.stopTime)
                    && Objects.equals(raws1, other.raws1)
                    && Objects.equals(raws2, other.raws2)
                    && Objects.equals(acquisition, other.acquisition);
        }
        return ret;
    }

}
