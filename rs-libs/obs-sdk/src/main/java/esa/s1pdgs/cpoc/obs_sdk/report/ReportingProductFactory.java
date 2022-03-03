package esa.s1pdgs.cpoc.obs_sdk.report;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;

@Component
public class ReportingProductFactory {	
	public ReportingInput reportingInputFor(final ObsObject obsObject, final String bucketName) {			
		return new ObsReportingInput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(obsObject.getFamily(), obsObject.getKey())
				), 
				bucketName
		);	
	}
}
