package esa.s1pdgs.cpoc.prip.frontend.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class PripReportingInput extends FilenameReportingInput {	
	@JsonProperty("user_name_string")
	private String userName;

	public PripReportingInput(final List<String> filenames, final List<String> segments, final String userName) {
		super(filenames, segments);
		this.userName = userName;
	}
	
	public static final PripReportingInput newInstance(
			final String productName, 
			final String user, 
			final ProductFamily family
	) {
		if (family == ProductFamily.L0_SEGMENT) {
			return new PripReportingInput(
					Collections.emptyList(),
					Collections.singletonList(productName),
					user
			);
		}
		return new PripReportingInput(			
				Collections.singletonList(productName),
				Collections.emptyList(),
				user
		);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}
}
