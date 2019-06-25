package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

public class CompressionJobsMessageDto extends GenericMessageDto<ProductDto> {
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
            final ProductDto body) {
        super(identifier, inputKey, body);
    }
}
