package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exception concerning the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObsUnknownObject extends ObsException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * 
	 * @param key
	 * @param bucket
	 * @param message
	 */
	public ObsUnknownObject(final ProductFamily family, final String key) {
		super(ErrorCode.OBS_UNKOWN_OBJ, family, key, "Object not found");
	}

}
