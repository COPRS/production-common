package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Extension of the GenericMessageDto for the category AuxiliaryFiles
 * 
 * @author Viveris Technologies
 */
public class AuxiliaryFilesMessageDto
        extends GenericMessageDto<ProductDto> {

    /**
     * Default constructor
     */
    public AuxiliaryFilesMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public AuxiliaryFilesMessageDto(final long identifier,
            final String inputKey, final ProductDto body) {
        super(identifier, inputKey, body);
    }

}
