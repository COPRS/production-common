package de.werum.coprs.cadip.client.model;

import java.time.LocalDateTime;

public interface CadipQualityInfo {

	Long getChannel();

	String getSessionId();

	Long getAcquiredTFs();

	Long getErrorTFs();

	Long getCorrectedTFs();

	Long getUncorrectableTFs();

	Long getDataTFs();

	Long getErrorDataTFs();

	Long getCorrectedDataTFs();

	Long getUncorrectableDataTFs();

	LocalDateTime getDeliveryStart();

	LocalDateTime getDeliveryStop();

	Long getTotalChunks();

	Long getTotalVolume();
}
