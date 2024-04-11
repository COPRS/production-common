/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class ITReporting {
	
	@Test
	public final void testReportingVsLogging() {
		final Reporting uut = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("test");
		
		uut.begin(new ReportingMessage("test message"));	
		uut.end(new IpfFilenameReportingOutput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(ProductFamily.L0_SEGMENT, "S1A_RF_RAW__0SHV_20200120T123137_20200120T123138_030884_038B5A_FCFB.SAFE")), true,
				""), 
				new ReportingMessage(230000000L,"test message")				
		);

	}
}
