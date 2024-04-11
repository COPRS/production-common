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

package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

/**
 * The JobGenerator for PDUs of the type "TILE" is relatively simple:
 * 
 * - No changes to start and stop time of the job. 
 * - 1 AppDataJob per incoming product/PreparationJob 
 * - The most important feature is contained in the dynamic processing parameters: 
 *   * TileIdentifiers (17 characters followed by space for each identifier) 
 *   * TileCoordinates (list of coordinates enclosed in square brackets and separated 
 *     by spaces. First and last coordinate shall be the same and the coordinates shall 
 *     form a rectangle)
 */
public class PDUTileGenerator extends AbstractPDUGenerator implements PDUGenerator {

	private static final Logger LOGGER = LogManager.getLogger(PDUTileGenerator.class);

	@Override
	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job, final int primaryCheckMaxTimelifeS)
			throws MetadataQueryException {
		LOGGER.info(" ");
		AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		
		return Collections.singletonList(appDataJob);
	}
}
