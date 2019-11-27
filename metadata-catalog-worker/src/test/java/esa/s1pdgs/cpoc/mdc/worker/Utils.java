package esa.s1pdgs.cpoc.mdc.worker;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

public class Utils {

    public static final CatalogJob newCatalogJob(final String name, final String keyObs, final ProductFamily family, final String mode)
    {
    	return newCatalogJob(name, keyObs, family, mode, 0, null, null, null, null, null);
    }
    
    public static final CatalogJob newCatalogJob(final String name, final String keyObs, final ProductFamily family)
    {
    	return newCatalogJob(name, keyObs, family, null);
    }
    
    public static final CatalogJob newCatalogJob(
    		final String name, 
    		final String keyObs, 
    		final ProductFamily family, 
    		final String mode,
    		final int channelNotImplemented,
    		final EdrsSessionFileType dontCare,
    		final String missionId,
    		final String satelliteId,
    		final String stationCode,
    		final String sessionId
    )
    {
    	final CatalogJob job = new CatalogJob();
    	job.setProductName(name);
    	job.setKeyObjectStorage(keyObs);
    	job.setProductFamily(family);
    	job.setMode(mode);
    	job.setMissionId(missionId);
    	job.setSatelliteId(satelliteId);
    	job.setStationCode(stationCode);
    	job.setSessionId(sessionId);
    	
    	return job;
    }


}
