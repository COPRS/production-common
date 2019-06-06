package esa.s1pdgs.cpoc.mqi.model.rest;

/**
 * Extension of the GenericMessageDto for the category LevelJobs
 * 
 * @author Viveris Technologies
 */
public class ErrorsMessageDto extends GenericMessageDto<String> {

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
            final String body) {
        super(identifier, TOPIC_ERROR, body);
    }

}
