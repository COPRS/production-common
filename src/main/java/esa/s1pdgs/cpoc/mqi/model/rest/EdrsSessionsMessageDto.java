package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * Extension of the GenericMessageDto for the category EdrsSessions
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionsMessageDto extends GenericMessageDto<EdrsSessionDto> {
    
    /**
     * Default constructor
     */
    public EdrsSessionsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public EdrsSessionsMessageDto(final long identifier,
            final String inputKey, final EdrsSessionDto body) {
        super(identifier, inputKey, body);
    }

}
