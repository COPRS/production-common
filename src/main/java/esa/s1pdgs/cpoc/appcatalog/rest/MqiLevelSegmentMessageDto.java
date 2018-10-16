package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

/**
 * Message object used by the REST applicative catalog for the category
 * LEVEL_PRODUCTS
 * 
 * @author Viveris Technologies
 */
public class MqiLevelSegmentMessageDto
        extends MqiGenericMessageDto<LevelSegmentDto> {

    /**
     * Default constructor
     */
    public MqiLevelSegmentMessageDto() {
        super(ProductCategory.LEVEL_SEGMENTS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiLevelSegmentMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.LEVEL_SEGMENTS, identifier, topic, partition,
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
    public MqiLevelSegmentMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final LevelSegmentDto dto) {
        super(ProductCategory.LEVEL_SEGMENTS, identifier, topic, partition,
                offset, dto);
    }

}
