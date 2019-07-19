package esa.s1pdgs.cpoc.validation.service.obs;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

@Service
public class ObsService {

    /**
     * OBS client
     */
    private final ObsClient client;

    /**
     * Constructor
     * 
     * @param client
     */
    @Autowired
    public ObsService(final ObsClient client) {
        this.client = client;
    }

    /**
     * Check if given file exist in OBS
     * 
     * @param family
     * @param key
     * @return
     * @throws ObsException
     */
    public boolean exist(final ProductFamily family, final String key)
            throws ObsException {
        ObsObject object = new ObsObject(key, getObsFamily(family));
        try {
            return client.doesObjectExist(object);
        } catch (SdkClientException exc) {
            throw new ObsException(family, key, exc);
        }
    }
    
    public List<ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException {
    	ObsFamily obsFamily = getObsFamily(family);
    	List<ObsObject> results = client.getListOfObjectsOfTimeFrameOfFamily(intervalStart, intervalEnd, obsFamily);    	
    	return results;
    }


    /**
     * Get ObsFamily from ProductFamily
     * 
     * @param family
     * @return
     */
    protected ObsFamily getObsFamily(final ProductFamily family) {
        ObsFamily ret;
        switch (family) {
            case AUXILIARY_FILE:
                ret = ObsFamily.AUXILIARY_FILE;
                break;
            case EDRS_SESSION:
                ret = ObsFamily.EDRS_SESSION;
                break;
            case L0_SLICE:
                ret = ObsFamily.L0_SLICE;
                break;
            case L0_SEGMENT:
                ret = ObsFamily.L0_SEGMENT;
                break;
            case L0_BLANK:
                ret = ObsFamily.L0_BLANK;
                break;
            case L0_ACN:
                ret = ObsFamily.L0_ACN;
                break;
            case L1_SLICE:
                ret = ObsFamily.L1_SLICE;
                break;
            case L1_ACN:
                ret = ObsFamily.L1_ACN;
                break;
            case L2_SLICE:
            	ret = ObsFamily.L2_SLICE;
            	break;
            case L2_ACN:
            	ret = ObsFamily.L2_ACN;
            	break;
            	
            // COMPRESSED PRODUCTS
            case AUXILIARY_FILE_ZIP:
                ret = ObsFamily.AUXILIARY_FILE_ZIP;
                break;
            case L0_SLICE_ZIP:
                ret = ObsFamily.L0_SLICE_ZIP;
                break;
            case L0_SEGMENT_ZIP:
                ret = ObsFamily.L0_SEGMENT_ZIP;
                break;
            case L0_BLANK_ZIP:
                ret = ObsFamily.L0_BLANK_ZIP;
                break;
            case L0_ACN_ZIP:
                ret = ObsFamily.L0_ACN_ZIP;
                break;
            case L1_SLICE_ZIP:
                ret = ObsFamily.L1_SLICE_ZIP;
                break;
            case L1_ACN_ZIP:
                ret = ObsFamily.L1_ACN_ZIP;
                break;
            case L2_SLICE_ZIP:
            	ret = ObsFamily.L2_SLICE_ZIP;
            	break;
            case L2_ACN_ZIP:
            	ret = ObsFamily.L2_ACN_ZIP;
            	break;
            default:
                ret = ObsFamily.UNKNOWN;
                break;
        }
        return ret;
    }
}
