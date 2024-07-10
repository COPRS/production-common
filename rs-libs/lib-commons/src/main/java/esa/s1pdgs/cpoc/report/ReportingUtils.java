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

package esa.s1pdgs.cpoc.report;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public final class ReportingUtils {
	private static final Predicate<ReportingFilenameEntry> ALL_FILTER = e -> { return true; };
	
	public static final Reporting.Builder newReportingBuilder(MissionId mission) {
		return new ReportAdapter.Builder(new LoggerReportingAppender(), mission);
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ProductFamily family, final String name) {
		return newFilenameReportingInputFor(new ReportingFilenameEntry(family, name));
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ReportingFilenameEntry ... products) {
		return new FilenameReportingInput(new ReportingFilenameEntries(Arrays.asList(products)));
	}
	
	public static final ReportingOutput newFilenameReportingOutputFor(final ProductFamily family, final String name) {
		return newFilenameReportingOutputFor(new ReportingFilenameEntry(family, name));
	}
	
	public static final ReportingOutput newFilenameReportingOutputFor(final ReportingFilenameEntry ... products) {
		return new FilenameReportingOutput(new ReportingFilenameEntries(Arrays.asList(products)));
	}
	
	static List<String> filenamesOf(final List<ReportingFilenameEntry> products) {
		return uniqueFlatProducts(products, ALL_FILTER);				
	}
	
	private static final List<String> uniqueFlatProducts(
			final List<ReportingFilenameEntry> products,
			final Predicate<ReportingFilenameEntry> filter
	) {
		return products.stream()
				.filter(filter)
				.map(e -> toFlatFilename(e.getProductName()))
				.collect(Collectors.toList());
	}
	

	// S1PRO-1395: makes sure that only the actual filename is dumped	
	private static final String toFlatFilename(final String filename) {
		return new File(filename).getName(); 
	}
}
