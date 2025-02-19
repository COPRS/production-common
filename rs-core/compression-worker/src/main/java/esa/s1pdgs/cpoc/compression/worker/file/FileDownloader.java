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

package esa.s1pdgs.cpoc.compression.worker.file;

import java.io.File;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class FileDownloader {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(FileDownloader.class);

	/**
	 * Factory for accessing to the object storage
	 */
	private final ObsClient obsClient;

	/**
	 * Path to the local working directory
	 */
	private final String localWorkingDir;

	private final AbstractMessage event;
	
	private MissionId mission;


	public FileDownloader(final MissionId mission, final ObsClient obsClient, final String localWorkingDir, final AbstractMessage event) {
		this.mission = mission;
		this.obsClient = obsClient;
		this.localWorkingDir = localWorkingDir;
		this.event = event;
	}

	/**
	 * Prepare the working directory by downloading all needed inputs
	 * 
	 * @throws AbstractCodedException
	 */
	public void processInputs(final ReportingFactory reportingFactory) throws AbstractCodedException {
		// prepare directory structure
		LOGGER.info("CompressionProcessor 1 - Creating working directory: {}", this.localWorkingDir);
		final File workingDir = new File(localWorkingDir);
		workingDir.mkdirs();

		// organize inputs
		final ObsDownloadObject inputProduct = buildInput();

		// download input from object storage in batch
		LOGGER.info("4 - Starting downloading input product {}", inputProduct);
		obsClient.download(Arrays.asList(new ObsDownloadObject(inputProduct.getFamily(), inputProduct.getKey(), inputProduct.getTargetDir())), reportingFactory);
	}

	/**
	 * Sort inputs: - if JOB => create the file - if RAW / CONFIG / L0_PRODUCT /
	 * L0_ACN => convert into S3DownloadFile - if BLANK => ignore - else => throw
	 * exception
	 * 
	 * @return
	 * @throws InternalErrorException
	 * @throws UnknownFamilyException
	 */
	protected ObsDownloadObject buildInput() throws InternalErrorException, UnknownFamilyException {
		LOGGER.info("CompressionProcessor 3 - Starting organizing inputs");
		
		if (event.getKeyObjectStorage() == null) {
			throw new InternalErrorException("productName to download cannot be null");
		}
		
		String targetFile = "";
		if (!CompressionEventUtil.isCompressed(event.getKeyObjectStorage())) {
			// Compression
			targetFile = this.localWorkingDir + "/" + CompressionEventUtil.composeCompressedKeyObjectStorage(event.getKeyObjectStorage(), mission);	
		} else {
			// Uncompression
			targetFile = this.localWorkingDir + "/" + CompressionEventUtil.removeZipFromKeyObjectStorage(event.getKeyObjectStorage());
		}

		
		LOGGER.info("Input {} will be stored in {}", event.getKeyObjectStorage(), targetFile);
		return new ObsDownloadObject(event.getProductFamily(), event.getKeyObjectStorage(), targetFile);

	}
}
