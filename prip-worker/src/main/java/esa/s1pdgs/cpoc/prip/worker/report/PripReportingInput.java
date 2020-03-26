package esa.s1pdgs.cpoc.prip.worker.report;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class PripReportingInput extends FilenameReportingInput {	
	@JsonProperty("prip_storage_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date storeDate;

	public PripReportingInput(final List<String> filenames, final List<String> segments, final Date storeDate) {
		super(filenames, segments);
		this.storeDate = storeDate;
	}

	public static final PripReportingInput newInstance(
			final String productName, 
			final Date storeDate,
			final ProductFamily family
	) {
		if (family == ProductFamily.L0_SEGMENT) {
			return new PripReportingInput(
					Collections.emptyList(),
					Collections.singletonList(productName),
					storeDate
			);
		}
		return new PripReportingInput(			
				Collections.singletonList(productName),
				Collections.emptyList(),
				storeDate
		);
	}

	public Date getStoreDate() {
		return storeDate;
	}

	public void setStoreDate(final Date storeDate) {
		this.storeDate = storeDate;
	}
}
