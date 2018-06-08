package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;

import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;

/**
 * Product for level L0
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionProduct extends AbstractProduct<EdrsSession> {

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
	public EdrsSessionProduct(final String identifier, final String satelliteId, final String missionId,
			final Date startTime, final Date stopTime, final EdrsSession object) {
		super(identifier, satelliteId, missionId, startTime, stopTime, object, "SESSION");
	}
}
