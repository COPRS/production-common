package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Extension of the GenericMessageDto for the category LevelProducts
 * 
 * @author Viveris Technologies
 */
public class LevelSegmentsMessageDto
        extends GenericMessageDto<ProductDto> {

    /**
     * Default constructor
     */
    public LevelSegmentsMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public LevelSegmentsMessageDto(final long identifier, final String inputKey,
            final ProductDto body) {
        super(identifier, inputKey, body);
    }

}
