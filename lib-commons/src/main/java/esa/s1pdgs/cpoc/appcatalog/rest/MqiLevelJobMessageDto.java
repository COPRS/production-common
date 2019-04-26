package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;

/**
 * Message object used by the REST applicative catalog for the category
 * LEVEL_JOBS
 * 
 * @author Viveris Technologies
 */
public class MqiLevelJobMessageDto extends MqiGenericMessageDto<LevelJobDto> {

    /**
     * Default constructor
     */
    public MqiLevelJobMessageDto() {
        super(ProductCategory.LEVEL_JOBS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiLevelJobMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.LEVEL_JOBS, identifier, topic, partition, offset);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     * @param dto
     */
    public MqiLevelJobMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final LevelJobDto dto) {
        super(ProductCategory.LEVEL_JOBS, identifier, topic, partition, offset,
                dto);
    }

}
