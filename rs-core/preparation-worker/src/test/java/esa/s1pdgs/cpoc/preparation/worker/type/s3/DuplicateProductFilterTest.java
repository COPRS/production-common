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

package esa.s1pdgs.cpoc.preparation.worker.type.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;

public class DuplicateProductFilterTest {

	@Test
	public void filterJobOrderInputShouldChooseMostRecentCreationTime() {
		List<JobOrderTimeInterval> timeIntervals = new ArrayList<>();
		timeIntervals.add(new JobOrderTimeInterval("20040703_005217706000", "20040703_005417706000",
				"/data/localWD/3/S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T072225_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3"));
		timeIntervals.add(new JobOrderTimeInterval("20040703_005217706000", "20040703_005417706000",
				"/data/localWD/3/S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T074028_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3"));

		List<JobOrderInputFile> fileNames = new ArrayList<>();
		fileNames.add(new JobOrderInputFile(
				"/data/localWD/3/S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T072225_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3",
				"S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T072225_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3"));
		fileNames.add(new JobOrderInputFile(
				"/data/localWD/3/S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T074028_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3",
				"S3A_OL_0_EFR____20040703T005217_20040703T005417_20200827T074028_DDDD_001_002_FFFF_WER_D_NR_NNN.SEN3"));

		JobOrderInput input = new JobOrderInput("OL_0_EFR___", JobOrderFileNameType.PHYSICAL, fileNames, timeIntervals,
				ProductFamily.S3_L0);
		;

		input = DuplicateProductFilter.filterJobOrderInput(input);
		assertEquals(input.getNbFilenames(), 1);
	}

}
