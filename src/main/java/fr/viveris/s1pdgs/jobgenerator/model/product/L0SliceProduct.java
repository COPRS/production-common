package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;

/**
 * Product for level L1
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceProduct extends AbstractProduct<L0Slice> {

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param satelliteId
	 * @param missionId
	 * @param startTime
	 * @param stopTime
	 * @param object
	 */
	public L0SliceProduct(final String identifier, final String satelliteId, final String missionId,
			final Date startTime, final Date stopTime, final L0Slice object) {
		super(identifier, satelliteId, missionId, startTime, stopTime, object, "");
	}

}
