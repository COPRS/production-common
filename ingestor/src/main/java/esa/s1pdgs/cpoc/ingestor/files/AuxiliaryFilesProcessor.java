package esa.s1pdgs.cpoc.ingestor.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AuxiliaryFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.KafkaConfigFileProducer;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * 
 */
@Component
public class AuxiliaryFilesProcessor
        extends AbstractFileProcessor<ProductDto> {

    /**
     * @param obsService
     * @param publisher
     * @param extractor
     */
    @Autowired
    public AuxiliaryFilesProcessor(final ObsService obsService,
            final KafkaConfigFileProducer publisher,
            final AuxiliaryFileDescriptorService extractor,
            final AppStatus appStatus,
            @Value("${file.backup-directory}") final String backupDirectory) {
        super(obsService, publisher, extractor, ProductFamily.AUXILIARY_FILE, appStatus, backupDirectory);
    }

    /**
     * 
     */
    @Override
    protected ProductDto buildDto(final FileDescriptor descriptor) {
        return new ProductDto(descriptor.getProductName(), descriptor.getProductName(), ProductFamily.AUXILIARY_FILE);
    }

}
