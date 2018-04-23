package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;

import fr.viveris.s1pdgs.jobgenerator.model.EdrsSession;

public class EdrsSessionProduct extends AbstractProduct<EdrsSession>{

	public EdrsSessionProduct(String identifier, String satelliteId, String missionId, Date startTime, Date stopTime,
			EdrsSession object) {
		super(identifier, satelliteId, missionId, startTime, stopTime, object, "SESSION");
	}
	
}
