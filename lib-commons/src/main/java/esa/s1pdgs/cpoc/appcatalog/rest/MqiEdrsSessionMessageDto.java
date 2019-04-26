package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * Message object used by the REST applicative catalog for the category
 * EDRS_SESSIONS
 * 
 * @author Viveris Technologies
 */
public class MqiEdrsSessionMessageDto
        extends MqiGenericMessageDto<EdrsSessionDto> {

    /**
     * Default constructor
     */
    public MqiEdrsSessionMessageDto() {
        super(ProductCategory.EDRS_SESSIONS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiEdrsSessionMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.EDRS_SESSIONS, identifier, topic, partition,
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
    public MqiEdrsSessionMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final EdrsSessionDto dto) {
        super(ProductCategory.EDRS_SESSIONS, identifier, topic, partition,
                offset, dto);
    }

}
