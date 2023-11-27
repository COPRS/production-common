package de.werum.coprs.cadip.client.odata.model;

import java.time.LocalDateTime;

import de.werum.coprs.cadip.client.model.CadipQualityInfo;

public class CadipOdataQualityInfo implements CadipQualityInfo {

	public static final String CHANNEL_ATTRIBUTE = "Channel";
	public static final String SESSION_ID_ATTRIBUTE = "SessionId";
	public static final String ACQUIRED_TFS_ATTRIBUTE = "AcquiredTFs";
	public static final String ERROR_TFS_ATTRIBUTE = "ErrorTFs";
	public static final String CORRECTED_TFS_ATTRIBUTE = "CorrectedTFs";
	public static final String UNCORRECTABLE_TFS_ATTRIBUTE = "UncorrectableTFs";
	public static final String DATA_TFS_ATTRIBUTE = "DataTFs";
	public static final String ERROR_DATA_TFS_ATTRIBUTE = "ErrorDataTFs";
	public static final String CORRECTED_DATA_TFS_ATTRIBUTE = "CorrectedDataTFs";
	public static final String UNCORRECTABLE_DATA_TFS_ATTRIBUTE = "UncorrectableDataTFs";
	public static final String DELIVERY_START_ATTRIBUTE = "DeliveryStart";
	public static final String DELIVERY_STOP_ATTRIBUTE = "DeliveryStop";
	public static final String TOTAL_CHUNKS_ATTRIBUTE = "TotalChunks";
	public static final String TOTAL_VOLUME_ATTRIBUTE = "TotalVolume";
	
	private Long channel;
	private String sessionId;
	private Long acquiredTFs;
	private Long errorTFs;
	private Long correctedTFs;
	private Long uncorrectableTFs;
	private Long dataTFs;
	private Long errorDataTFs;
	private Long correctedDataTFs;
	private Long uncorrectableDataTFs;
	private LocalDateTime deliveryStart;
	private LocalDateTime deliveryStop;
	private Long totalChunks;
	private Long totalVolume;

	@Override
	public Long getChannel() {
		return channel;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public Long getAcquiredTFs() {
		return acquiredTFs;
	}

	@Override
	public Long getErrorTFs() {
		return errorTFs;
	}

	@Override
	public Long getCorrectedTFs() {
		return correctedTFs;
	}

	@Override
	public Long getUncorrectableTFs() {
		return uncorrectableTFs;
	}

	@Override
	public Long getDataTFs() {
		return dataTFs;
	}

	@Override
	public Long getErrorDataTFs() {
		return errorDataTFs;
	}

	@Override
	public Long getCorrectedDataTFs() {
		return correctedDataTFs;
	}

	@Override
	public Long getUncorrectableDataTFs() {
		return uncorrectableDataTFs;
	}

	@Override
	public LocalDateTime getDeliveryStart() {
		return deliveryStart;
	}

	@Override
	public LocalDateTime getDeliveryStop() {
		return deliveryStop;
	}

	@Override
	public Long getTotalChunks() {
		return totalChunks;
	}

	@Override
	public Long getTotalVolume() {
		return totalVolume;
	}

	public void setChannel(Long channel) {
		this.channel = channel;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setAcquiredTFs(Long acquiredTFs) {
		this.acquiredTFs = acquiredTFs;
	}

	public void setErrorTFs(Long errorTFs) {
		this.errorTFs = errorTFs;
	}

	public void setCorrectedTFs(Long correctedTFs) {
		this.correctedTFs = correctedTFs;
	}

	public void setUncorrectableTFs(Long uncorrectableTFs) {
		this.uncorrectableTFs = uncorrectableTFs;
	}

	public void setDataTFs(Long dataTFs) {
		this.dataTFs = dataTFs;
	}

	public void setErrorDataTFs(Long errorDataTFs) {
		this.errorDataTFs = errorDataTFs;
	}

	public void setCorrectedDataTFs(Long correctedDataTFs) {
		this.correctedDataTFs = correctedDataTFs;
	}

	public void setUncorrectableDataTFs(Long uncorrectableDataTFs) {
		this.uncorrectableDataTFs = uncorrectableDataTFs;
	}

	public void setDeliveryStart(LocalDateTime deliveryStart) {
		this.deliveryStart = deliveryStart;
	}

	public void setDeliveryStop(LocalDateTime deliveryStop) {
		this.deliveryStop = deliveryStop;
	}

	public void setTotalChunks(Long totalChunks) {
		this.totalChunks = totalChunks;
	}

	public void setTotalVolume(Long totalVolume) {
		this.totalVolume = totalVolume;
	}
}
