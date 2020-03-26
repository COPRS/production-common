package esa.s1pdgs.cpoc.obs_sdk.report;

import java.util.Collections;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingInput;

@Component
public class ReportingProductFactory {	
	public ReportingInput reportingInputFor(final ObsObject obsObject, final String bucketName) {		
		if (obsObject.getFamily() == ProductFamily.L0_SEGMENT) {			
			return new ObsReportingInput(
					Collections.emptyList(), 
					Collections.singletonList(obsObject.getKey()), 
					bucketName
			);
		}
		return new ObsReportingInput(
				Collections.singletonList(obsObject.getKey()), 
				Collections.emptyList(), 				
				bucketName
		);		
	}
}
