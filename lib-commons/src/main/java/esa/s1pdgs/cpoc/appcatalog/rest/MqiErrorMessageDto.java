package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

/**
 * Message object used by the REST applicative catalog for Errors
 * 
 */
public class MqiErrorMessageDto extends MqiGenericMessageDto<ErrorDto> {

    /**
     * Default constructor
     */
    public MqiErrorMessageDto() {
        super(ProductCategory.ERRORS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiErrorMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.ERRORS, identifier, topic, partition, offset);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     * @param dto
     */
    public MqiErrorMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final ErrorDto dto) {
        super(ProductCategory.ERRORS, identifier, topic, partition,
                offset, dto);
    }

}
