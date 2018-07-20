package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

/**
 * Extension of the GenericMessageDto for the category LevelReports
 * 
 * @author Viveris Technologies
 */
public class LevelReportsMessageDto extends GenericMessageDto<LevelReportDto> {

    /**
     * Default constructor
     */
    public LevelReportsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public LevelReportsMessageDto(final long identifier, final String inputKey,
            final LevelReportDto body) {
        super(identifier, inputKey, body);
    }

}
