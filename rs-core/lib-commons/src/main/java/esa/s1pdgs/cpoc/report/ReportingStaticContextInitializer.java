package esa.s1pdgs.cpoc.report;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportingStaticContextInitializer {	
	private final String segmentBlacklistPattern;

	@Autowired
	public ReportingStaticContextInitializer(
			@Value("${report.segment-blacklist-pattern:}") final String segmentBlacklistPattern
	) {
		this.segmentBlacklistPattern = segmentBlacklistPattern;
	}
	
	@PostConstruct
    public void init() {
		if (!segmentBlacklistPattern.isEmpty()) {
			ReportingUtils.setSegmentBlacklistPattern(segmentBlacklistPattern);
		}
    }
}
