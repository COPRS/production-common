package esa.s1pdgs.cpoc.obs_sdk.report;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingInput;

@Component
public class ReportingProductFactory {	
	public ReportingInput reportingInputFor(final ObsObject obsObject, final String bucketName) {		
		if (obsObject.getFamily() == ProductFamily.L0_SEGMENT) {
			return new ObsSegmentReportingInput(bucketName, obsObject.getKey());
		}
		return new ObsReportingInput(bucketName, obsObject.getKey());		
	}
}
