package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Message object used by the REST applicative catalog for the category
 * LEVEL_PRODUCTS
 * 
 * @author Viveris Technologies
 */
public class MqiLevelProductMessageDto
        extends MqiGenericMessageDto<ProductDto> {

    /**
     * Default constructor
     */
    public MqiLevelProductMessageDto() {
        super(ProductCategory.LEVEL_PRODUCTS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiLevelProductMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.LEVEL_PRODUCTS, identifier, topic, partition,
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
    public MqiLevelProductMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final ProductDto dto) {
        super(ProductCategory.LEVEL_PRODUCTS, identifier, topic, partition,
                offset, dto);
    }

}
