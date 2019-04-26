package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;

/**
 * Extension of the GenericMessageDto for the category LevelJobs
 * 
 * @author Viveris Technologies
 */
public class LevelJobsMessageDto extends GenericMessageDto<LevelJobDto> {

    /**
     * Default constructor
     */
    public LevelJobsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public LevelJobsMessageDto(final long identifier, final String inputKey,
            final LevelJobDto body) {
        super(identifier, inputKey, body);
    }

}
