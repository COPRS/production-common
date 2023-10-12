package de.werum.coprs.cadip.client.odata.mapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.apache.olingo.client.api.domain.ClientCollectionValue;
import org.apache.olingo.client.api.domain.ClientComplexValue;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.domain.ClientValue;

import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.model.CadipQualityInfo;
import de.werum.coprs.cadip.client.model.CadipSession;
import de.werum.coprs.cadip.client.odata.model.CadipOdataFile;
import de.werum.coprs.cadip.client.odata.model.CadipOdataQualityInfo;
import de.werum.coprs.cadip.client.odata.model.CadipOdataSession;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class ResponseMapperUtil {

	/**
	 * Map an Odata response of an endpoint providing a list of sessions to the
	 * library own model objects
	 * 
	 * @param response odata response
	 * @return list of session objects
	 */
	public static List<CadipSession> mapResponseToListOfSessions(
			final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response) {
		final List<CadipSession> result = new ArrayList<>();

		if (null != response) {
			while (response.hasNext()) {
				final ClientEntity entity = response.next();
				final CadipOdataSession session = new CadipOdataSession();

				// @formatter:off
				session.setId(getPropertyFromEntity(entity, CadipOdataSession.ID_ATTRIBUTE, UUID::fromString));
				session.setSessionId(getPropertyFromEntity(entity, CadipOdataSession.SESSION_ID_ATTRIBUTE, String::toString));
				session.setNumChannels(getPropertyFromEntity(entity, CadipOdataSession.NUM_CHANNELS_ATTRIBUTE, Long::parseLong));
				session.setPublicationDate(getPropertyFromEntity(entity, CadipOdataSession.PUBLICATION_DATE_ATTRIBUTE, DateUtils::parse));
				session.setSatellite(getPropertyFromEntity(entity, CadipOdataSession.SATELLITE_ATTRIBUTE, String::toString));
				session.setStationUnitId(getPropertyFromEntity(entity, CadipOdataSession.STATION_UNIT_ID_ATTRIBUTE, String::toString));
				session.setDownlinkOrbit(getPropertyFromEntity(entity, CadipOdataSession.DOWNLINK_ORBIT_ATTRIBUTE, Long::parseLong));
				session.setAcquisitionId(getPropertyFromEntity(entity, CadipOdataSession.ACQUISITION_ID_ATTRIBUTE, String::toString));
				session.setAntennaId(getPropertyFromEntity(entity, CadipOdataSession.ANTENNA_ID_ATTRIBUTE, String::toString));
				session.setFrontEndId(getPropertyFromEntity(entity, CadipOdataSession.FRONT_END_ID_ATTRIBUTE, String::toString));
				session.setRetransfer(getPropertyFromEntity(entity, CadipOdataSession.RETRANSFER_ATTRIBUTE, Boolean::parseBoolean));
				session.setAntennaStatusOK(getPropertyFromEntity(entity, CadipOdataSession.ANTENNA_STATUS_OK_ATTRIBUTE, Boolean::parseBoolean));
				session.setFrontEndStatusOK(getPropertyFromEntity(entity, CadipOdataSession.FRONT_END_STATUS_OK_ATTRIBUTE, Boolean::parseBoolean));
				session.setPlannedDataStart(getPropertyFromEntity(entity, CadipOdataSession.PLANNED_DATA_START_ATTRIBUTE, DateUtils::parse));
				session.setPlannedDataStop(getPropertyFromEntity(entity, CadipOdataSession.PLANNED_DATA_STOP_ATTRIBUTE, DateUtils::parse));
				session.setDownlinkStart(getPropertyFromEntity(entity, CadipOdataSession.DOWNLINK_START_ATTRIBUTE, DateUtils::parse));
				session.setDownlinkStop(getPropertyFromEntity(entity, CadipOdataSession.DOWNLINK_STOP_ATTRIBUTE, DateUtils::parse));
				session.setDownlinkStatusOK(getPropertyFromEntity(entity, CadipOdataSession.DOWNLINK_STATUS_OK_ATTRIBUTE, Boolean::parseBoolean));
				session.setDeliveryPushOK(getPropertyFromEntity(entity, CadipOdataSession.DELIVERY_PUSH_OK_ATTRIBUTE, Boolean::parseBoolean));
				// @formatter:on

				result.add(session);
			}
			response.close();
		}
		return result;
	}
	
	/**
	 * Map an Odata response of an endpoint providing a list of files to the
	 * library own model objects
	 * 
	 * @param response odata response
	 * @return list of file objects
	 */
	public static List<CadipFile> mapResponseToListOfFiles(
			final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response) {
		final List<CadipFile> result = new ArrayList<>();

		if (null != response) {
			while (response.hasNext()) {
				final ClientEntity entity = response.next();
				final CadipOdataFile file = new CadipOdataFile();

				// @formatter:off
				file.setId(getPropertyFromEntity(entity, CadipOdataFile.ID_ATTRIBUTE, UUID::fromString));
				file.setName(getPropertyFromEntity(entity, CadipOdataFile.NAME_ATTRIBUTE, String::toString));
				file.setSessionId(getPropertyFromEntity(entity, CadipOdataFile.SESSION_ID_ATTRIBUTE, String::toString));
				file.setChannel(getPropertyFromEntity(entity, CadipOdataFile.CHANNEL_ATTRIBUTE, Long::parseLong));
				file.setBlockNumber(getPropertyFromEntity(entity, CadipOdataFile.BLOCK_NUMBER_ATTRIBUTE, Long::parseLong));
				file.setFinalBlock(getPropertyFromEntity(entity, CadipOdataFile.FINAL_BLOCK_ATTRIBUTE, Boolean::parseBoolean));
				file.setPublicationDate(getPropertyFromEntity(entity, CadipOdataFile.PUBLICATION_DATE_ATTRIBUTE, DateUtils::parse));
				file.setEvictionDate(getPropertyFromEntity(entity, CadipOdataFile.EVICTION_DATE_ATTRIBUTE, DateUtils::parse));
				file.setSize(getPropertyFromEntity(entity, CadipOdataFile.SIZE_ATTRIBUTE, Long::parseLong));
				file.setRetransfer(getPropertyFromEntity(entity, CadipOdataFile.RETRANSFER_ATTRIBUTE, Boolean::parseBoolean));
				// @formatter:on

				result.add(file);
			}
			response.close();
		}
		return result;
	}
	
	/**
	 * Map an Odata response of an endpoint providing a session expanded with its list of files to the
	 * library own model objects
	 * 
	 * @param cliententity object from CADIP Server
	 * @return list of file objects related to the given session
	 */
	public static List<CadipFile> mapSessionResponseToListOfFiles(final ClientEntity response) {
		final List<CadipFile> result = new ArrayList<>();

		if (null != response) {
			ClientCollectionValue<ClientValue> filesProperty = response.getProperty("Files").getCollectionValue();
			filesProperty.forEach((fileProperty) -> {
				final CadipOdataFile file = new CadipOdataFile();
				final ClientComplexValue complexValue = fileProperty.asComplex();
				
				// @formatter:off
				file.setId(getPropertyFromComplexValue(complexValue, CadipOdataFile.ID_ATTRIBUTE, UUID::fromString));
				file.setName(getPropertyFromComplexValue(complexValue, CadipOdataFile.NAME_ATTRIBUTE, String::toString));
				file.setSessionId(getPropertyFromComplexValue(complexValue, CadipOdataFile.SESSION_ID_ATTRIBUTE, String::toString));
				file.setChannel(getPropertyFromComplexValue(complexValue, CadipOdataFile.CHANNEL_ATTRIBUTE, Long::parseLong));
				file.setBlockNumber(getPropertyFromComplexValue(complexValue, CadipOdataFile.BLOCK_NUMBER_ATTRIBUTE, Long::parseLong));
				file.setFinalBlock(getPropertyFromComplexValue(complexValue, CadipOdataFile.FINAL_BLOCK_ATTRIBUTE, Boolean::parseBoolean));
				file.setPublicationDate(getPropertyFromComplexValue(complexValue, CadipOdataFile.PUBLICATION_DATE_ATTRIBUTE, DateUtils::parse));
				file.setEvictionDate(getPropertyFromComplexValue(complexValue, CadipOdataFile.EVICTION_DATE_ATTRIBUTE, DateUtils::parse));
				file.setSize(getPropertyFromComplexValue(complexValue, CadipOdataFile.SIZE_ATTRIBUTE, Long::parseLong));
				file.setRetransfer(getPropertyFromComplexValue(complexValue, CadipOdataFile.RETRANSFER_ATTRIBUTE, Boolean::parseBoolean));
				// @formatter:on

				result.add(file);
			});
		}
		return result;
	}
	
	/**
	 * Map an Odata response of an endpoint providing a list of quality infos to the
	 * library own model objects
	 * 
	 * @param response odata response
	 * @return list of file objects
	 */
	public static List<CadipQualityInfo> mapResponseToListOfQualityInfos(
			final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response) {
		final List<CadipQualityInfo> result = new ArrayList<>();

		if (null != response) {
			while (response.hasNext()) {
				final ClientEntity entity = response.next();
				final CadipOdataQualityInfo qualityInfo = new CadipOdataQualityInfo();

				// @formatter:off
				qualityInfo.setChannel(getPropertyFromEntity(entity, CadipOdataQualityInfo.CHANNEL_ATTRIBUTE, Long::parseLong));
				qualityInfo.setSessionId(getPropertyFromEntity(entity, CadipOdataQualityInfo.SESSION_ID_ATTRIBUTE, String::toString));
				qualityInfo.setAcquiredTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.ACQUIRED_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setErrorTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.ERROR_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setCorrectedTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.CORRECTED_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setUncorrectableTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.UNCORRECTABLE_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setDataTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.DATA_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setErrorDataTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.ERROR_DATA_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setCorrectedDataTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.CORRECTED_DATA_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setUncorrectableDataTFs(getPropertyFromEntity(entity, CadipOdataQualityInfo.UNCORRECTABLE_DATA_TFS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setDeliveryStart(getPropertyFromEntity(entity, CadipOdataQualityInfo.DELIVERY_START_ATTRIBUTE, DateUtils::parse));
				qualityInfo.setDeliveryStop(getPropertyFromEntity(entity, CadipOdataQualityInfo.DELIVERY_STOP_ATTRIBUTE, DateUtils::parse));
				qualityInfo.setTotalChunks(getPropertyFromEntity(entity, CadipOdataQualityInfo.TOTAL_CHUNKS_ATTRIBUTE, Long::parseLong));
				qualityInfo.setTotalVolume(getPropertyFromEntity(entity, CadipOdataQualityInfo.TOTAL_VOLUME_ATTRIBUTE, Long::parseLong));
				// @formatter:on

				result.add(qualityInfo);
			}
			response.close();
		}
		return result;
	}

	private static <E> E getPropertyFromEntity(final ClientEntity entity, final String property,
			final Function<String, E> converterFunction) {
		final ClientProperty entityProperty = entity.getProperty(property);
		if (null != entityProperty) {
			ClientPrimitiveValue value = entityProperty.getPrimitiveValue();
			if (null != value && null != value.toValue()) {
				return converterFunction.apply(value.toString());
			}
		}

		return null;
	}
	
	private static <E> E getPropertyFromComplexValue(final ClientComplexValue complexValue, final String property,
			final Function<String, E> converterFunction) {
		final ClientProperty entityProperty = complexValue.get(property);
		if (null != entityProperty) {
			ClientPrimitiveValue value = entityProperty.getPrimitiveValue();
			if (null != value && null != value.toValue()) {
				return converterFunction.apply(value.toString());
			}
		}

		return null;
	}
}
