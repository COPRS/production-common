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

package esa.s1pdgs.cpoc.preparation.worker.config.type;

import java.util.Arrays;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableFactory;
import esa.s1pdgs.cpoc.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.edrs.AiopPropertiesAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.edrs.EdrsSessionProductValidator;
import esa.s1pdgs.cpoc.preparation.worker.type.edrs.EdrsSessionTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.pdu.PDUTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.s3.S3TypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.segment.AspPropertiesAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.segment.L0SegmentTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.slice.LevelSliceTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.spp.SppMbuTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.spp.SppObsPropertiesAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.spp.SppObsTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.synergy.S3SynergyTypeAdapter;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;

@Configuration
public class TypeAdapterConfiguration {

	static final Logger LOGGER = LogManager.getLogger(TypeAdapterConfiguration.class);
	
	@Autowired
	private MetadataClient metadataClient;
	
	@Autowired
	private ProcessProperties processSettings;
	
	@Autowired
	private PreparationWorkerProperties settings;
	
	@Autowired
    private AiopProperties aiopProperties;
    
	@Autowired
	private AspProperties aspProperties;
    
	@Autowired
	private SppObsProperties sppObsProperties;
	
	@Autowired
	private S3TypeAdapterProperties s3TypeAdapterSettings;
	
	@Autowired
	private S3SynergyProperties s3SynSettings;
    
	@Autowired
	private PDUProperties pduSettings;
	
	@Autowired
	private TaskTableFactory taskTableFactory;
	
	@Autowired
	private ElementMapper elementMapper;
	
	@Autowired
	private Function<TaskTable, InputTimeoutChecker> timeoutCheckerF;
	
	@Bean
	@Autowired
	public ProductTypeAdapter typeAdapter() {
		
		LOGGER.info("Create ProductTypeAdapter for level {}", processSettings.getLevel());
		
		switch(processSettings.getLevel()) {
			case L0:
				return new EdrsSessionTypeAdapter(
						metadataClient, 
						AiopPropertiesAdapter.of(aiopProperties),
						new EdrsSessionProductValidator()
				);
			case L0_SEGMENT:
				return new L0SegmentTypeAdapter(
						metadataClient, 
						AspPropertiesAdapter.of(aspProperties)
				);
			case L1: case L2:
				return new LevelSliceTypeAdapter(
						metadataClient, 
						settings.getTypeOverlap(), 
						settings.getTypeSliceLength(),
						settings.getJoborderTimelinessCategoryMapping(),
						timeoutCheckerF
				);			
			case S3_L0: case S3_L1: case S3_L2:
				return new S3TypeAdapter(
						metadataClient,
						taskTableFactory,
						elementMapper,
						processSettings,
						settings,
						s3TypeAdapterSettings
				);
			case S3_PDU:
				return new PDUTypeAdapter(
						metadataClient,
						elementMapper,
						settings,
						processSettings,
						pduSettings
				);
			case S3_SYN:
				return new S3SynergyTypeAdapter(
						metadataClient,
						elementMapper,
						taskTableFactory,
						processSettings, 
						settings,
						s3SynSettings);
			case SPP_MBU:
				return new SppMbuTypeAdapter(
						metadataClient
				);
			case SPP_OBS:
				return new SppObsTypeAdapter(metadataClient, SppObsPropertiesAdapter.of(sppObsProperties));
			default:
				throw new IllegalArgumentException(
						String.format(
								"Unsupported Application Level '%s'. Available are: %s", 
								processSettings.getLevel(),
								Arrays.asList(
										ApplicationLevel.L0, 
										ApplicationLevel.L0_SEGMENT, 
										ApplicationLevel.L1, 
										ApplicationLevel.L2,
										ApplicationLevel.S3_L0,
										ApplicationLevel.S3_L1,
										ApplicationLevel.S3_L2,
										ApplicationLevel.S3_PDU,
										ApplicationLevel.SPP_MBU,
										ApplicationLevel.SPP_OBS
										)
								)
						);
		}
	}}
