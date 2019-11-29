package esa.s1pdgs.cpoc.mdc.worker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    	
    	// TODO FIXME
//    	job.setMissionId(missionId);
//    	job.setSatelliteId(satelliteId);
//    	job.setStationCode(stationCode);
//    	job.setSessionId(sessionId);
    	
    	return job;
    }
    
    public static final void copyFolder(final Path src, final Path dest) throws Exception {
        Files.walk(src)
            .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    private static final void copy(final Path source, final Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}
