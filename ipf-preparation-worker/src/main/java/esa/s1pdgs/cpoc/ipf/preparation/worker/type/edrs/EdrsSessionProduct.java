package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProductAdapter;

final class EdrsSessionProduct
{
	private static final String RAWS1_ID = "raws1";
	private static final String RAWS2_ID = "raws2";
	
	private final AppDataJobProductAdapter product;

	public EdrsSessionProduct(final AppDataJobProductAdapter product) {
		this.product = product;
	}
	
	public static final EdrsSessionProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final EdrsSessionProduct of(final AppDataJobProduct product) {
		return new EdrsSessionProduct(
				new AppDataJobProductAdapter(product)
		);
	}
	
	public final void setRawsForChannel(final int channel, final List<AppDataJobFile> raws) 
	{
		Collections.sort(raws);
		final String channelKey = getIdForChannels(channel);	
		product.setProductsFor(channelKey, raws);
	}	
	
	public final List<AppDataJobFile> getRawsForChannel(final int channel) {
		final String channelKey = getIdForChannels(channel);	
		return product.getProductsFor(channelKey);
	}

	public final String getStationCode() {
		return product.getStringValue("stationCode");
	}

	public final String getProductName() {
		return product.getProductName();
	}

	public final String getMissionId() {
		return product.getMissionId();
	}

	public final String getSatelliteId() {
		return product.getSatelliteId();
	}

	public final String getStartTime() {
		return product.getStartTime();
	}

	public String getStopTime() {
		return product.getStopTime();
	}	
	
	public String getSessionId() {
		return product.getStringValue("sessionId");
	}
	
	private final String getIdForChannels(final int channel) {
		if (channel == 1) {
			return RAWS1_ID;
		}
		return RAWS2_ID;
	}
}