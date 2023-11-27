package de.werum.coprs.cadip.client.odata.model;

import java.time.LocalDateTime;
import java.util.UUID;

import de.werum.coprs.cadip.client.model.CadipSession;

public class CadipOdataSession implements CadipSession {

	public static final String ENTITY_SET_NAME = "Sessions";
	public static final String ID_ATTRIBUTE = "Id";
	public static final String SESSION_ID_ATTRIBUTE = "SessionId";
	public static final String NUM_CHANNELS_ATTRIBUTE = "NumChannels";
	public static final String PUBLICATION_DATE_ATTRIBUTE = "PublicationDate";
	public static final String SATELLITE_ATTRIBUTE = "Satellite";
	public static final String STATION_UNIT_ID_ATTRIBUTE = "StationUnitId";
	public static final String DOWNLINK_ORBIT_ATTRIBUTE = "DownlinkOrbit";
	public static final String ACQUISITION_ID_ATTRIBUTE = "AcquisitionId";
	public static final String ANTENNA_ID_ATTRIBUTE = "AntennaId";
	public static final String FRONT_END_ID_ATTRIBUTE = "FrontEndId";
	public static final String RETRANSFER_ATTRIBUTE = "Retransfer";
	public static final String ANTENNA_STATUS_OK_ATTRIBUTE = "AntennaStatusOK";
	public static final String FRONT_END_STATUS_OK_ATTRIBUTE = "FrontEndStatusOK";
	public static final String PLANNED_DATA_START_ATTRIBUTE = "PlannedDataStart";
	public static final String PLANNED_DATA_STOP_ATTRIBUTE = "PlannedDataStop";
	public static final String DOWNLINK_START_ATTRIBUTE = "DownlinkStart";
	public static final String DOWNLINK_STOP_ATTRIBUTE = "DownlinkStop";
	public static final String DOWNLINK_STATUS_OK_ATTRIBUTE = "DownlinkStatusOK";
	public static final String DELIVERY_PUSH_OK_ATTRIBUTE = "DeliveryPushOK";
	
	private UUID id;
	private String sessionId;
	private Long numChannels;
	private LocalDateTime publicationDate;
	private String satellite;
	private String stationUnitId;
	private Long downlinkOrbit;
	private String acquisitionId;
	private String antennaId;
	private String frontEndId;
	private Boolean retransfer;
	private Boolean antennaStatusOK;
	private Boolean frontEndStatusOK;
	private LocalDateTime plannedDataStart;
	private LocalDateTime plannedDataStop;
	private LocalDateTime downlinkStart;
	private LocalDateTime downlinkStop;
	private Boolean downlinkStatusOK;
	private Boolean deliveryPushOK;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public Long getNumChannels() {
		return numChannels;
	}

	@Override
	public LocalDateTime getPublicationDate() {
		return publicationDate;
	}

	@Override
	public String getSatellite() {
		return satellite;
	}

	@Override
	public String getStationUnitId() {
		return stationUnitId;
	}

	@Override
	public Long getDownlinkOrbit() {
		return downlinkOrbit;
	}

	@Override
	public String getAcquisitionId() {
		return acquisitionId;
	}

	@Override
	public String getAntennaId() {
		return antennaId;
	}

	@Override
	public String getFrontEndId() {
		return frontEndId;
	}

	@Override
	public Boolean getRetransfer() {
		return retransfer;
	}

	@Override
	public Boolean getAntennaStatusOK() {
		return antennaStatusOK;
	}

	@Override
	public Boolean getFrontEndStatusOK() {
		return frontEndStatusOK;
	}

	@Override
	public LocalDateTime getPlannedDataStart() {
		return plannedDataStart;
	}

	@Override
	public LocalDateTime getPlannedDataStop() {
		return plannedDataStop;
	}

	@Override
	public LocalDateTime getDownlinkStart() {
		return downlinkStart;
	}

	@Override
	public LocalDateTime getDownlinkStop() {
		return downlinkStop;
	}

	@Override
	public Boolean getDownlinkStatusOK() {
		return downlinkStatusOK;
	}

	@Override
	public Boolean getDeliveryPushOK() {
		return deliveryPushOK;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setNumChannels(Long numChannels) {
		this.numChannels = numChannels;
	}

	public void setPublicationDate(LocalDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public void setSatellite(String satellite) {
		this.satellite = satellite;
	}

	public void setStationUnitId(String stationUnitId) {
		this.stationUnitId = stationUnitId;
	}

	public void setDownlinkOrbit(Long downlinkOrbit) {
		this.downlinkOrbit = downlinkOrbit;
	}

	public void setAcquisitionId(String acquisitionId) {
		this.acquisitionId = acquisitionId;
	}

	public void setAntennaId(String antennaId) {
		this.antennaId = antennaId;
	}

	public void setFrontEndId(String frontEndId) {
		this.frontEndId = frontEndId;
	}

	public void setRetransfer(Boolean retransfer) {
		this.retransfer = retransfer;
	}

	public void setAntennaStatusOK(Boolean antennaStatusOK) {
		this.antennaStatusOK = antennaStatusOK;
	}

	public void setFrontEndStatusOK(Boolean frontEndStatusOK) {
		this.frontEndStatusOK = frontEndStatusOK;
	}

	public void setPlannedDataStart(LocalDateTime plannedDataStart) {
		this.plannedDataStart = plannedDataStart;
	}

	public void setPlannedDataStop(LocalDateTime plannedDataStop) {
		this.plannedDataStop = plannedDataStop;
	}

	public void setDownlinkStart(LocalDateTime downlinkStart) {
		this.downlinkStart = downlinkStart;
	}

	public void setDownlinkStop(LocalDateTime downlinkStop) {
		this.downlinkStop = downlinkStop;
	}

	public void setDownlinkStatusOK(Boolean downlinkStatusOK) {
		this.downlinkStatusOK = downlinkStatusOK;
	}

	public void setDeliveryPushOK(Boolean deliveryPushOK) {
		this.deliveryPushOK = deliveryPushOK;
	}
}
