package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L1OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L2OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.extraction.obs.ObsService;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * KAFKA consumer. Consume on a topic defined in L1 slices
 * 
 * @author Olivier Bex-Chauvet
 */
@Service
public class LevelProductsExtractor extends GenericExtractor<ProductDto> {

    /**
     * Pattern for configuration files to extract data
     */
    public final static String PATTERN_CONFIG =
            "^(S1|AS)(A|B)_(S[1-6]|IW|EW|WV|N[1-6]|EN|IM)_(SLC|GRD|OCN|RAW)(F|H|M|_)_(0|1|2)(A|C|N|S|_)(SH|SV|HH|HV|VV|VH|DH|DV)_([0-9a-z]{15})_([0-9a-z]{15})_([0-9]{6})_([0-9a-z_]{6})\\w{1,}\\.(SAFE)(/.*)?$";

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsService obsService;

    /**
     * Manifest filename
     */
    private final String manifestFilename;

    /**
     * 
     */
    private final String fileManifestExt;

    @Autowired
    public LevelProductsExtractor(final EsServices esServices,
            final ObsService obsService,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<ProductDto> mqiService,
            final AppStatus appStatus,
            final MetadataExtractorConfig extractorConfig,
            @Value("${file.product-categories.level-products.local-directory}") final String localDirectory,
            @Value("${file.manifest-filename}") final String manifestFilename,
            final ErrorRepoAppender errorAppender,
            final ProcessConfiguration processConfiguration,
            @Value("${file.file-with-manifest-ext}") final String fileManifestExt) {
        super(esServices, mqiService, appStatus, localDirectory,
                extractorConfig, PATTERN_CONFIG, errorAppender, ProductCategory.LEVEL_PRODUCTS, processConfiguration, ProductDto.class);
        this.obsService = obsService;
        this.manifestFilename = manifestFilename;
        this.fileManifestExt = fileManifestExt;
    }

    /**
     * Consume a message from the AUXILIARY_FILES product category and extract
     * metadata
     * 
     * @see GenericExtractor#genericExtract()
     */
    @Scheduled(fixedDelayString = "${file.product-categories.level-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-products.init-delay-poll-ms}")
    public void extract() {
        super.genericExtract();
    }

    /**
     * @see GenericExtractor#extractMetadata(GenericMessageDto)
     */
    @Override
    protected JSONObject extractMetadata(
    		final Reporting.Factory reportingFactory, 
            final GenericMessageDto<ProductDto> message)
            throws AbstractCodedException {
        ProductDto dto = message.getBody();
        // Upload file
        String keyObs = getKeyObs(message);
        
        final String productName = extractProductNameFromDto(dto);
        final ProductFamily family = message.getBody().getFamily();
        
        reportingFactory.product(family.toString(), productName);
        
        final File metadataFile = download(reportingFactory, obsService, family, productName, keyObs);        
        return extract(reportingFactory, dto, metadataFile, productName, family);
    }
    
    private final JSONObject extract(	
    		final Reporting.Factory reportingFactory, 
    		final ProductDto dto,
    		final File metadataFile,
            final String productName,
            final ProductFamily family
    ) 
    	throws AbstractCodedException
    {
        switch (dto.getFamily()) {
	        case L0_ACN:        	
	        	final L0OutputFileDescriptor l0AcnDesc = extractFromFilename(
	        			reportingFactory, 
	        			() -> fileDescriptorBuilder.buildL0OutputFileDescriptor(metadataFile, dto)
	        	);
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL0AcnOutputFileMetadata(l0AcnDesc,metadataFile)
	        	); 
	        case L0_SLICE:
	        	final L0OutputFileDescriptor l0Desc = extractFromFilename(
	        			reportingFactory, 
	        			() -> fileDescriptorBuilder.buildL0OutputFileDescriptor(metadataFile, dto)
	        	);
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL0SliceOutputFileMetadata(l0Desc,metadataFile)
	        	);
	        case L1_ACN:        	
	        	final L1OutputFileDescriptor l1AcnDesc = extractFromFilename(
	        			reportingFactory, 
	        			() -> fileDescriptorBuilder.buildL1OutputFileDescriptor(metadataFile, dto)
	        	);
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL1AcnOutputFileMetadata(l1AcnDesc,metadataFile)
	        	);
	        case L1_SLICE:
	        	final L1OutputFileDescriptor l1SliceDesc = extractFromFilename(
	        			reportingFactory, 
	        			() -> fileDescriptorBuilder.buildL1OutputFileDescriptor(metadataFile, dto)
	        	);
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL1SliceOutputFileMetadata(l1SliceDesc,metadataFile)
	        	);
	        case L2_ACN:
	        	final L2OutputFileDescriptor l2AcnDesc = extractFromFilename(
	        			reportingFactory,
	        			() -> fileDescriptorBuilder.buildL2OutputFileDescriptor(metadataFile, dto)
	        	);	        	
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL2AcnOutputFileMetadata(l2AcnDesc, metadataFile)
	        	);	  
	        case L2_SLICE:
	        	final L2OutputFileDescriptor l2SliceDesc = extractFromFilename(
	        			reportingFactory,
	        			() -> fileDescriptorBuilder.buildL2OutputFileDescriptor(metadataFile, dto)
	        	);	  
	        	return extractFromFile(
	        			reportingFactory, 
	        			() -> mdBuilder.buildL2SliceOutputFileMetadata(l2SliceDesc,metadataFile)
	        	);	  
	        default:
	            throw new UnknownFamilyException(dto.getFamily().name(),
	                    "Family not managed by the catalog for the category LEVEL_PRODUCTS");
	    }
    }
    

    /**
     * Get the OBS key of the file used for extracting metadata for this product
     * 
     * @param message
     * @return
     */
    protected String getKeyObs(final GenericMessageDto<ProductDto> message) {
        String keyObs = message.getBody().getKeyObjectStorage();
        if (keyObs.toLowerCase().endsWith(fileManifestExt.toLowerCase())) {
            keyObs += "/" + manifestFilename;
        }
        return keyObs;
    }

    /**
     * @see GenericExtractor#extractProductNameFromDto(Object)
     */
    @Override
    protected String extractProductNameFromDto(final ProductDto dto) {
        return dto.getProductName();
    }

    /**
     * @see GenericExtractor#cleanProcessing(GenericMessageDto)
     */
    @Override
    protected void cleanProcessing(
            final GenericMessageDto<ProductDto> message) {
        // TODO Auto-generated method stub
        File metadataFile = new File(localDirectory + getKeyObs(message));
        if (metadataFile.exists()) {
            File parent = metadataFile.getParentFile();
            metadataFile.delete();
            // Remove upper directory if needed
            if (!localDirectory.endsWith(parent.getName() + "/")) {
                parent.delete();
            }
        }
    }
}
