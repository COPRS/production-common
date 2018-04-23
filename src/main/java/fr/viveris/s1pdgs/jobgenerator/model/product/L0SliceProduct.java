package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Date;

public class L0SliceProduct extends AbstractProduct<L0Slice> {

	public L0SliceProduct(String identifier, String satelliteId, String missionId, Date startTime, Date stopTime,
			L0Slice object) {
		super(identifier, satelliteId, missionId, startTime, stopTime, object, "");
	}

}
