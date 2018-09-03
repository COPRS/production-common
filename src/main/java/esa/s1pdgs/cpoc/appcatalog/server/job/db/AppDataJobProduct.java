package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Object used for persisting useful information of input product for a job
 * 
 * @author Viveris Technologies
 */
public class AppDataJobProduct {

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
     * Instrument configuration id (in metadata). -1 if not exist
     */
    private int insConfId;

    /**
     * Its type (in metadata)
     */
    private String productType;

    /**
     * List of raws for channel 1
     */
    private List<AppDataJobFile> raws1;

    /**
     * List of raws for channel 2
     */
    private List<AppDataJobFile> raws2;

    /**
     * Acquisition
     */
    private String acquisition;

    /**
     * Data take identifier
     */
    private String dataTakeId;

    /**
     * Slice number
     */
    private int numberSlice;

    /**
     * Total number of slice for its segment
     */
    private int totalNbOfSlice;

    /**
     * Start date of the segment
     */
    private String segmentStartDate;

    /**
     * Stop date of the segment
     */
    private String segmentStopDate;

    /**
     * 
     */
    public AppDataJobProduct() {
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
     * @return the insConfId
     */
    public int getInsConfId() {
        return insConfId;
    }

    /**
     * @param insConfId
     *            the insConfId to set
     */
    public void setInsConfId(final int insConfId) {
        this.insConfId = insConfId;
    }

    /**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            the productType to set
     */
    public void setProductType(final String productType) {
        this.productType = productType;
    }

    /**
     * @return the raws1
     */
    public List<AppDataJobFile> getRaws1() {
        return raws1;
    }

    /**
     * @param raws1
     *            the raws1 to set
     */
    public void setRaws1(final List<AppDataJobFile> raws1) {
        this.raws1 = raws1;
    }

    /**
     * @return the raws2
     */
    public List<AppDataJobFile> getRaws2() {
        return raws2;
    }

    /**
     * @param raws2
     *            the raws2 to set
     */
    public void setRaws2(final List<AppDataJobFile> raws2) {
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
     * @return the dataTakeId
     */
    public String getDataTakeId() {
        return dataTakeId;
    }

    /**
     * @param dataTakeId
     *            the dataTakeId to set
     */
    public void setDataTakeId(final String dataTakeId) {
        this.dataTakeId = dataTakeId;
    }

    /**
     * @return the numberSlice
     */
    public int getNumberSlice() {
        return numberSlice;
    }

    /**
     * @param numberSlice
     *            the numberSlice to set
     */
    public void setNumberSlice(final int numberSlice) {
        this.numberSlice = numberSlice;
    }

    /**
     * @return the totalNbOfSlice
     */
    public int getTotalNbOfSlice() {
        return totalNbOfSlice;
    }

    /**
     * @param totalNbOfSlice
     *            the totalNbOfSlice to set
     */
    public void setTotalNbOfSlice(final int totalNbOfSlice) {
        this.totalNbOfSlice = totalNbOfSlice;
    }

    /**
     * @return the segmentStartDate
     */
    public String getSegmentStartDate() {
        return segmentStartDate;
    }

    /**
     * @param segmentStartDate
     *            the segmentStartDate to set
     */
    public void setSegmentStartDate(final String segmentStartDate) {
        this.segmentStartDate = segmentStartDate;
    }

    /**
     * @return the segmentStopDate
     */
    public String getSegmentStopDate() {
        return segmentStopDate;
    }

    /**
     * @param segmentStopDate
     *            the segmentStopDate to set
     */
    public void setSegmentStopDate(final String segmentStopDate) {
        this.segmentStopDate = segmentStopDate;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{sessionId: %s, productName: %s, satelliteId: %s, missionId: %s, startTime: %s, stopTime: %s, insConfId: %s, productType: %s, raws1: %s, raws2: %s, acquisition: %s, dataTakeId: %s, numberSlice: %s, totalNbOfSlice: %s, segmentStartDate: %s, segmentStopDate: %s}",
                sessionId, productName, satelliteId, missionId, startTime,
                stopTime, insConfId, productType, raws1, raws2, acquisition,
                dataTakeId, numberSlice, totalNbOfSlice, segmentStartDate,
                segmentStopDate);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(sessionId, productName, satelliteId, missionId, startTime,
                stopTime, insConfId, productType, raws1, raws2, acquisition,
                dataTakeId, numberSlice, totalNbOfSlice, segmentStartDate,
                segmentStopDate);
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
            AppDataJobProduct other = (AppDataJobProduct) obj;
            ret = Objects.equals(sessionId, other.sessionId)
                    && Objects.equals(productName, other.productName)
                    && Objects.equals(satelliteId, other.satelliteId)
                    && Objects.equals(missionId, other.missionId)
                    && Objects.equals(startTime, other.startTime)
                    && Objects.equals(stopTime, other.stopTime)
                    && insConfId == other.insConfId
                    && Objects.equals(productType, other.productType)
                    && Objects.equals(raws1, other.raws1)
                    && Objects.equals(raws2, other.raws2)
                    && Objects.equals(acquisition, other.acquisition)
                    && Objects.equals(dataTakeId, other.dataTakeId)
                    && numberSlice == other.numberSlice
                    && totalNbOfSlice == other.totalNbOfSlice
                    && Objects.equals(segmentStartDate, other.segmentStartDate)
                    && Objects.equals(segmentStopDate, other.segmentStopDate);
        }
        return ret;
    }

}
