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

package esa.s1pdgs.cpoc.compression.worker.process;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;

public class CompressExecutorCallable implements Callable<Void> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(CompressExecutorCallable.class);
	
	private static final Consumer<String> DEFAULT_OUTPUT_CONSUMER = LOGGER::info;

	private final AbstractMessage catalogEvent;
	
	private final MissionId mission; 

	
	/**
	 * Application properties
	 */
	private final CompressionWorkerConfigurationProperties properties;

	/**
	 * Will create one PoolProcessor per pool
	 * 
	 * @param properties
	 * @param job
	 * @param prefixMonitorLogs
	 */
	public CompressExecutorCallable(final MissionId mission, final AbstractMessage catalogEvent, final CompressionWorkerConfigurationProperties properties) {
		this.mission = mission;
		this.catalogEvent = catalogEvent;
		this.properties = properties;
	}

	
	/**
	 * Process execution: <br/>
	 * - Wait for being active (see {@link CompressionWorkerConfigurationProperties} wap fields) <br/>
	 * - For each pool, launch in parallel the tasks executions
	 */
	@Override
	public Void call() throws AbstractCodedException {
		String outputPath;
		String command;		
		
		if (!CompressionEventUtil.isCompressed(catalogEvent.getKeyObjectStorage())) {
			// We trust in the mission id from the event and use it too look up the configured compression command.
			
			command = properties.getCompressionCommand().get(mission.name().toLowerCase());
			
			// If we didn't get a valid command, we fallback to the default compression that had been used previously.
			if (command == null) {
				LOGGER.warn("Unable to determine compression command for mission {}, assuming normal zip compression", mission);
				command = "/app/zip-compression.sh";
			}
			
			// Compression
			outputPath = CompressionEventUtil.composeCompressedKeyObjectStorage(catalogEvent.getKeyObjectStorage(), mission);
		} else {			
			// Uncompression
			command = properties.getUncompressionCommand();
			outputPath = CompressionEventUtil.removeZipFromKeyObjectStorage(catalogEvent.getKeyObjectStorage()); 
		}

		LOGGER.debug("command={}, mission={}, productName={}, workingDirectory={}", command, mission, catalogEvent.getKeyObjectStorage(),
				properties.getWorkingDirectory());

		execute(
				command, 
				catalogEvent.getKeyObjectStorage(), 
				outputPath,
				properties.getWorkingDirectory() + "/" + outputPath
		);
		return null;
	}
	
	public TaskResult execute(final String binaryPath, final String inputPath, final String outputPath,
            final String workDirectory) throws InternalErrorException {
				
		LOGGER.info("Starting compression/uncompression task using '{}' with input {} and output {} in {}", binaryPath, inputPath, outputPath, workDirectory);
        
        final Consumer<String> stdOutConsumer = DEFAULT_OUTPUT_CONSUMER;
        final Consumer<String> stdErrConsumer = DEFAULT_OUTPUT_CONSUMER;
        
        int r = -1;

        Process process = null;
        try {
            final ProcessBuilder builder = new ProcessBuilder();
            builder.command(binaryPath, inputPath, outputPath);
            builder.directory(new File(workDirectory));
            process = builder.start();

            final Future<?> out = Executors.newSingleThreadExecutor().submit(
            		new StreamGobbler(process.getInputStream(), stdOutConsumer)
            );
            final Future<?> err = Executors.newSingleThreadExecutor().submit(
            		new StreamGobbler(process.getErrorStream(), stdErrConsumer)
            );      
            r = process.waitFor();

            // wait for STDOUT/STDERR to be consumed
            out.get();
			err.get();

		} catch (final InterruptedException ie) {
			LOGGER.warn("[task {}] [workDirectory {}]  InterruptedException", binaryPath, workDirectory);
			Thread.currentThread().interrupt();
		} catch (final IOException ioe) {
			throw new InternalErrorException("Cannot build the command for the task " + binaryPath, ioe);
		} catch (final ExecutionException e) {
			throw new InternalErrorException("Error on consuming stdout/stderr of task " + binaryPath, e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}        
        
        if (r != 0) {
        	LOGGER.warn("[task {}] [workDirectory {}]  Exit code: {}", binaryPath, workDirectory, r);
        	throw new InternalErrorException("Exit code of compression/uncompression script != 0: " + r);
        } else {
        	LOGGER.debug("[task {}] [workDirectory {}]  Exit code: {}", binaryPath, workDirectory, r);
        }
        
        return new TaskResult(binaryPath, r);
    }
	
}
