package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
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
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
	 */
	public ProductMetadata buildConfigFileMetadata(final AuxDescriptor descriptor, final File file)
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
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 */
	public ProductMetadata buildEdrsSessionFileMetadata(final EdrsSessionFileDescriptor descriptor, final File dsib)
			throws MetadataExtractionException {
		return extractor.processSESSIONFile(descriptor, dsib);
	}
	
	public ProductMetadata buildEdrsSessionFileRaw(final EdrsSessionFileDescriptor descriptor) 
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
     * @return the ProductMetadata containing the metadata to index
     * 
     * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
     */
    public ProductMetadata buildL0SegmentOutputFileMetadata(final OutputFileDescriptor descriptor, final File file,
    		final ReportingFactory reportingFactory) throws MetadataExtractionException, MetadataMalformedException {
        final ProductMetadata metadataToIndex = extractor.processL0Segment(descriptor, file, reportingFactory);        
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
     * @return the ProductMetadata containing the metadata to index
     * 
     * @throws MetadataExtractionException
	 * @throws MetadataMalformedException 
     */
    public ProductMetadata buildOutputFileMetadata(final OutputFileDescriptor descriptor, final File file, final CatalogJob job)
            throws MetadataExtractionException, MetadataMalformedException {
        final ProductMetadata metadataToIndex = extractor.processProduct(descriptor, job.getProductFamily(), file);
        // Adding fields that are directly used from the DTO
        metadataToIndex.put("oqcFlag", job.getOqcFlag());
        LOGGER.debug("JSON OBJECT:{}",metadataToIndex.toString());
        return metadataToIndex;
    }
    
    /**
	 * Build the metadata for an S2 product
	 * 
	 * @param descriptor file descriptor of the product
	 * @param file       file to extract metadata from
	 * 
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public ProductMetadata buildS2ProductFileMetadata(final S2FileDescriptor descriptor, final List<File> metadataFiles, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadataToIndex = extractor.processS2Metadata(descriptor, metadataFiles, job.getProductFamily(), job.getProductName());
		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}
	
	/**
	 * Build the metadata for an S2 HKTM
	 * 
	 * @param descriptor file descriptor of the product
	 * @param metadataFile       file to extract metadata from
	 * 
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public ProductMetadata buildS2HKTMFileMetadata(final S2FileDescriptor descriptor, final File metadataFile, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadataToIndex = extractor.processS2HKTMMetadata(descriptor, metadataFile, job.getProductFamily(), job.getProductName());
		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}
	
	/**
	 * Build the metadata for an S2 SAD
	 * 
	 * @param descriptor file descriptor of the product
	 * @param metadataFile       file to extract metadata from
	 * 
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public ProductMetadata buildS2SADFileMetadata(final S2FileDescriptor descriptor, final File metadataFile, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadataToIndex = extractor.processS2SADMetadata(descriptor, metadataFile, job.getProductFamily(), job.getProductName());
		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}
    
	/**
	 * Build the metadata for an S3 auxiliary product
	 * 
	 * @param descriptor file descriptor of the product
	 * @param file       file to extract metadata from
	 * 
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public ProductMetadata buildS3AuxFileMetadata(final S3FileDescriptor descriptor, final File file, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		final ProductMetadata metadataToIndex = extractor.processAuxXFDUFile(descriptor, file);

		LOGGER.debug("JSON OBJECT:{}", metadataToIndex.toString());
		return metadataToIndex;
	}
	
	/**
	 * Build the metadata for an S3 level product
	 * 
	 * @param descriptor file descriptor of the product
	 * @param file       file to extract metadata from
	 * 
	 * @return the ProductMetadata containing the metadata to index
	 * 
	 * @throws MetadataExtractionException
	 * @throws MetadataMalformedException
	 */
	public ProductMetadata buildS3LevelProductFileMetadata(final S3FileDescriptor descriptor, final File file, final CatalogJob job)
			throws MetadataExtractionException, MetadataMalformedException {
		ProductMetadata metadataToIndex;
		
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
