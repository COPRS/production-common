package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

/**
 * Extension of the GenericMessageDto for the error messages
 * 
 * @author Viveris Technologies
 */
public class ErrorsMessageDto extends GenericMessageDto<ErrorDto> {

	public final static String TOPIC_ERROR = "t-pdgs-errors";
	
    /**
     * Default constructor
     */
    public ErrorsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public ErrorsMessageDto(final long identifier,
            final ErrorDto body) {
        super(identifier, TOPIC_ERROR, body);
    }

}
