package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.ConfigFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

/**
 * Class to build metadata for configuration and ERDS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
public class MetadataBuilder {

	private static final Logger LOGGER =
            LogManager.getLogger(MetadataBuilder.class);
	
	/**
	 * Metadata extractor
	 */
	private ExtractMetadata extractor;

	/**
     * Local directory for configurations files
     */
    protected final String localDirectory;

	/**
	 * Default constructor
	 */
	public MetadataBuilder(MetadataExtractorConfig extractorConfig, final XmlConverter xmlConverter, final String localDirectory) {
		this(new ExtractMetadata(extractorConfig.getTypeOverlap(), extractorConfig.getTypeSliceLength(),
				extractorConfig.getXsltDirectory(), xmlConverter, localDirectory), localDirectory);
	}

	/**
	 * Constructor with an existing metadata extractor
	 * 
	 * @param extractor
	 */
	public MetadataBuilder(final ExtractMetadata extractor, final String localDirectory) {
		this.extractor = extractor;
		this.localDirectory = localDirectory;
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
	public JSONObject buildConfigFileMetadata(ConfigFileDescriptor descriptor, File file)
			throws MetadataExtractionException, MetadataMalformedException {
		JSONObject metadataToIndex = new JSONObject();
		switch (descriptor.getExtension()) {
		case EOF:
			if (descriptor.getProductType().equals("AUX_RESORB")) {
				metadataToIndex = extractor.processEOFFileWithoutNamespace(descriptor, file);
			} else {
				metadataToIndex = extractor.processEOFFile(descriptor, file);
			}
			break;
		case SAFE:
			metadataToIndex = extractor.processSAFEFile(descriptor, file);
			break;
		case XML:
			metadataToIndex = extractor.processXMLFile(descriptor, file);
			break;
		default:
			throw new MetadataExtractionException(new Exception("Invalid extension"));
		}
		return metadataToIndex;
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
	public JSONObject buildEdrsSessionFileMetadata(EdrsSessionFileDescriptor descriptor)
			throws MetadataExtractionException {
		JSONObject metadataToIndex = new JSONObject();
		switch (descriptor.getEdrsSessionFileType()) {
		case RAW:
			metadataToIndex = extractor.processRAWFile(descriptor);
			break;
		case SESSION:
			metadataToIndex = extractor.processSESSIONFile(descriptor);
			break;
		default:
			throw new MetadataExtractionException(new Exception("Invalid extension"));
		}
		return metadataToIndex;
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
    public JSONObject buildL0SegmentOutputFileMetadata(OutputFileDescriptor descriptor, File file)
            throws MetadataExtractionException, MetadataMalformedException {
        JSONObject metadataToIndex = new JSONObject();
        metadataToIndex = extractor.processL0Segment(descriptor, file);        
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
    public JSONObject buildOutputFileMetadata(OutputFileDescriptor descriptor, File file, ProductionEvent dto)
            throws MetadataExtractionException, MetadataMalformedException {
        JSONObject metadataToIndex = new JSONObject();
        metadataToIndex = extractor.processProduct(descriptor, dto.getProductFamily(), file);
        // Adding fields that are directly used from the DTO
        metadataToIndex.put("oqcFlag", dto.getOqcFlag());
        LOGGER.debug("JSON OBJECT:{}",metadataToIndex.toString());
        return metadataToIndex;
    }


	
}
