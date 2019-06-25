package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Message object used by the REST applicative catalog for the category
 * AUXILIARY_FILES
 * 
 * @author Viveris Technologies
 */
public class MqiAuxiliaryFileMessageDto
        extends MqiGenericMessageDto<ProductDto> {

    /**
     * Default constructor
     */
    public MqiAuxiliaryFileMessageDto() {
        super(ProductCategory.AUXILIARY_FILES);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiAuxiliaryFileMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.AUXILIARY_FILES, identifier, topic, partition,
                offset);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     * @param dto
     */
    public MqiAuxiliaryFileMessageDto(final long identifier, final String topic,
            final int partition, final long offset,
            final ProductDto dto) {
        super(ProductCategory.AUXILIARY_FILES, identifier, topic, partition,
                offset, dto);
    }

}
