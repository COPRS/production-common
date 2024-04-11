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

package esa.s1pdgs.cpoc.common;

/**
 * Application levels
 * @author Viveris Technologies
 *
 */
public enum ApplicationLevel {
	L0(ProductFamily.L0_JOB), 
	L0_SEGMENT(ProductFamily.L0_SEGMENT_JOB),
	L1(ProductFamily.L1_JOB), 
	L1_ETAD(ProductFamily.L1_ETAD_JOB), 
	L2(ProductFamily.L2_JOB),
	SPP_MBU(ProductFamily.SPP_MBU_JOB),
	SPP_OBS(ProductFamily.SPP_OBS_JOB),
	S3_L0(ProductFamily.S3_JOB),
	S3_L1(ProductFamily.S3_JOB),
	S3_L2(ProductFamily.S3_JOB),
	S3_PDU(ProductFamily.S3_JOB),
	S3_SYN(ProductFamily.S3_JOB);
	
	private final ProductFamily jobFamily;
	
	ApplicationLevel(final ProductFamily jobFamily) {
		this.jobFamily = jobFamily;
	}

	public ProductFamily toFamily() {
		return jobFamily;
	}
}
