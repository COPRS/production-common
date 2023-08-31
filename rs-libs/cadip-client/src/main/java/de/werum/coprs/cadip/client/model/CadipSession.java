package de.werum.coprs.cadip.client.model;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CadipSession {

	UUID getId();

	String getSessionId();

	Long getNumChannels();

	LocalDateTime getPublicationDate();
	
	String getSatellite();

	String getStationUnitId();

	Long getDownlinkOrbit();

	String getAcquisitionId();

	String getAntennaId();

	String getFrontEndId();

	Boolean getRetransfer();

	Boolean getAntennaStatusOK();

	Boolean getFrontEndStatusOK();

	LocalDateTime getPlannedDataStart();

	LocalDateTime getPlannedDataStop();

	LocalDateTime getDownlinkStart();

	LocalDateTime getDownlinkStop();

	Boolean getDownlinkStatusOK();

	Boolean getDeliveryPushOK();
}
