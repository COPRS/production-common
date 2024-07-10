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

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties.PDUTypeProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUType;

public interface PDUGenerator {

	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job, final int primaryCheckMaxTimelifeS)
			throws MetadataQueryException;

	/**
	 * Create a suitable instance of PDUGenerator for the given settings
	 * 
	 * @param settings PDUTypeSettings used to choose which generator to create
	 * @param mdClient MetadataClient for constructor of PDUGenerator
	 * @return instance of PDUGenerator, or null if none is suitable
	 */
	public static PDUGenerator getPDUGenerator(final ProcessProperties processSettings,
			PDUTypeProperties settings, MetadataClient mdClient) {
		if (settings.getType() == PDUType.FRAME) {
			return new PDUFrameGenerator(processSettings, settings, mdClient);
		} else if (settings.getType() == PDUType.STRIPE) {
			return new PDUStripeGenerator(processSettings, settings, mdClient);
		} else if (settings.getType() == PDUType.TILE) {
			return new PDUTileGenerator();
		}

		return null;
	}
}
