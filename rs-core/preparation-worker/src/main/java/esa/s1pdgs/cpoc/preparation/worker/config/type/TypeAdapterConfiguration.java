package esa.s1pdgs.cpoc.preparation.worker.config.type;

import java.util.Arrays;

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
	private PDUProperties pduSettings;
	
	@Autowired
	private TaskTableFactory taskTableFactory;
	
	@Autowired
	private ElementMapper elementMapper;
	
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
				// TODO: Add Timeout mechanic back for V2
				return new LevelSliceTypeAdapter(
						metadataClient, 
						settings.getTypeOverlap(), 
						settings.getTypeSliceLength(),
						settings.getJoborderTimelinessCategoryMapping()
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
