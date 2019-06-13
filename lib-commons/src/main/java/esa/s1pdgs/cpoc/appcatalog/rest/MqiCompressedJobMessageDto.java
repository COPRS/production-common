package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;

public class MqiCompressedJobMessageDto extends MqiGenericMessageDto<CompressionJobDto> {

    /**
     * Default constructor
     */
    public MqiCompressedJobMessageDto() {
        super(ProductCategory.COMPRESSED_PRODUCTS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiCompressedJobMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.COMPRESSED_PRODUCTS, identifier, topic, partition,
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
    public MqiCompressedJobMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final CompressionJobDto dto) {
        super(ProductCategory.COMPRESSED_PRODUCTS, identifier, topic, partition,
                offset, dto);
    }
}
