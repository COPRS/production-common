package fr.viveris.s1pdgs.common.errors.obs;

import fr.viveris.s1pdgs.common.ProductFamily;

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
