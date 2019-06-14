package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;

public class CompressionJobsMessageDto extends GenericMessageDto<CompressionJobDto> {
	/**
     * Default constructor
     */
    public CompressionJobsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public CompressionJobsMessageDto(final long identifier, final String inputKey,
            final CompressionJobDto body) {
        super(identifier, inputKey, body);
    }
}
