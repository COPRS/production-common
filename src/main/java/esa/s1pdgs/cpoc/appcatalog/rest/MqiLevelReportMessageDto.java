package esa.s1pdgs.cpoc.appcatalog.rest;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

/**
 * Message object used by the REST applicative catalog for the category
 * LEVEL_REPORTS
 * 
 * @author Viveris Technologies
 */
public class MqiLevelReportMessageDto
        extends MqiGenericMessageDto<LevelReportDto> {

    /**
     * Default constructor
     */
    public MqiLevelReportMessageDto() {
        super(ProductCategory.LEVEL_REPORTS);
    }

    /**
     * @param category
     * @param identifier
     * @param topic
     * @param partition
     * @param offset
     */
    public MqiLevelReportMessageDto(final long identifier, final String topic,
            final int partition, final long offset) {
        super(ProductCategory.LEVEL_REPORTS, identifier, topic, partition,
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
    public MqiLevelReportMessageDto(final long identifier, final String topic,
            final int partition, final long offset, final LevelReportDto dto) {
        super(ProductCategory.LEVEL_REPORTS, identifier, topic, partition,
                offset, dto);
    }

}
