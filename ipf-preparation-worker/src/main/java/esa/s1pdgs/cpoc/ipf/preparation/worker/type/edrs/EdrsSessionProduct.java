package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import java.util.Collections;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProduct;

final class EdrsSessionProduct extends AbstractProduct {
	private static final String RAWS1_ID = "raws1";
	private static final String RAWS2_ID = "raws2";

	public EdrsSessionProduct(final AppDataJobProductAdapter product) {
		super(product);
	}

	public static final EdrsSessionProduct of(final AppDataJob job) {
		return of(job.getProduct());
	}
	
	public static final EdrsSessionProduct of(final AppDataJobProduct product) {
		return new EdrsSessionProduct(
				new AppDataJobProductAdapter(product)
		);
	}
	
	public final void setRawsForChannel(final int channel, final List<AppDataJobFile> raws) {
		Collections.sort(raws);
		final String channelKey = getIdForChannels(channel);	
		product.setProductsFor(channelKey, raws);
	}	
	
	public final List<AppDataJobFile> getRawsForChannel(final int channel) {
		final String channelKey = getIdForChannels(channel);	
		return product.getProductsFor(channelKey);
	}
	
	public final void setStationCode(final String stationCode) {
		product.setStringValue("stationCode", stationCode);
	}

	public final String getStationCode() {
		return product.getStringValue("stationCode");
	}
	
	public final void setSessionId(final String sessionId) {
		product.setStringValue("sessionId", sessionId);
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