package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * Class to build metadata for configuration and ERDS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
public class MetadataBuilder {
	private static final Logger LOGGER = LogManager.getLogger(MetadataBuilder.class);
	
	/**
	 * Metadata extractor
	 */
	private ExtractMetadata extractor;

	/**
	 * Default constructor
	 */
//	public MetadataBuilder(final MetadataExtractorConfig extractorConfig, final XmlConverter xmlConverter, final String localDirectory) {
//		this(new ExtractMetadata(extractorConfig.getTypeOverlap(), extractorConfig.getTypeSliceLength(),
//				extractorConfig.getXsltDirectory(), xmlConverter, localDirectory));
//	}

	/**
	 * Constructor with an existing metadata extractor
	 * 
	 * @param extractor
	 */
	public MetadataBuilder(final ExtractMetadata extractor) {
		this.extractor = extractor;
	}

	/**
	 * Build metadata from configuration files
	 * 
	 * @param descriptor
	 * @param file
	 * 
	 * @return the JSONObject containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
	 */
	public JSONObject buildConfigFileMetadata(final AuxDescriptor descriptor, final File file)
			throws MetadataExtractionException, MetadataMalformedException {
		switch (descriptor.getExtension()) {
			case EOF:
				if (descriptor.getProductType().equals("AUX_RESORB")) {
					return extractor.processEOFFileWithoutNamespace(descriptor, file);
				} 
				return extractor.processEOFFile(descriptor, file);
			case SAFE:
				return extractor.processSAFEFile(descriptor, file);
			case XML:
				return extractor.processXMLFile(descriptor, file);
			default:
				// fall through
		}
		throw new MetadataExtractionException(new Exception("Invalid extension"));
	}

	/**
	 * Build metadata for ERDS session files
	 * 
	 * @param descriptor
	 * @param file
	 * 
	 * @return the JSONObject containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 */
	public JSONObject buildEdrsSessionFileMetadata(final EdrsSessionFileDescriptor descriptor, final File dsib)
			throws MetadataExtractionException {
		return extractor.processSESSIONFile(descriptor, dsib);
	}
	
	public JSONObject buildEdrsSessionFileRaw(final EdrsSessionFileDescriptor descriptor) 
			throws MetadataExtractionException
	{
		return extractor.processRAWFile(descriptor);
	}


	/**
     * Build the metadata for L0 Segment
     * 
     * 
     * @param descriptor
     * @param file
     * 
     * @return the JSONObject containing the metadata to index
     * 
     * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
     */
    public JSONObject buildL0SegmentOutputFileMetadata(final OutputFileDescriptor descriptor, final File file,
    		final ReportingFactory reportingFactory) throws MetadataExtractionException, MetadataMalformedException {
        final JSONObject metadataToIndex = extractor.processL0Segment(descriptor, file, reportingFactory);        
        LOGGER.debug("JSON OBJECT:{}",metadataToIndex.toString());
        return metadataToIndex;
    }
	
	/**
     * Build the metadata
     * 
     * 
     * @param descriptor
     * @param file
     * @param productFamily
     * 
     * @return the JSONObject containing the metadata to index
     * 
     * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
     */
    public JSONObject buildOutputFileMetadata(final OutputFileDescriptor descriptor, final File file, final CatalogJob job)
            throws MetadataExtractionException, MetadataMalformedException {
        final JSONObject metadataToIndex = extractor.processProduct(descriptor, job.getProductFamily(), file);
        // Adding fields that are directly used from the DTO
        metadataToIndex.put("oqcFlag", job.getOqcFlag());
        LOGGER.debug("JSON OBJECT:{}",metadataToIndex.toString());
        return metadataToIndex;
    }
    
	/**
	 * Build the metadata for an S3 auxiliary product
	 * 
	 * @param descriptor file descriptor of the product
	 * @param file       file to extract metadata from
	 * 
	 * @return the JSONObject containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject buildS3AuxFileMetadata(final S3FileDescriptor descriptor, final File file, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		final JSONObject metadataToIndex = extractor.processAuxXFDUFile(descriptor, file);

		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}
	
	/**
	 * Build the metadata for an S3 level product
	 * 
	 * @param descriptor file descriptor of the product
	 * @param file       file to extract metadata from
	 * 
	 * @return the JSONObject containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public JSONObject buildS3LevelProductFileMetadata(final S3FileDescriptor descriptor, final File file, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject metadataToIndex;
		
		switch (descriptor.getExtension()) {
			case ISIP:
				metadataToIndex = extractor.processIIFFile(descriptor, file);
				break;
			case SEN3:
				metadataToIndex = extractor.processProductXFDUFile(descriptor, file);
				break;
			default:
				throw new MetadataExtractionException(new Exception("Invalid extension for S3 level products"));
		}
		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}


	
}
