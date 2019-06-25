package esa.s1pdgs.cpoc.mqi.model.rest;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Extension of the GenericMessageDto for the category AuxiliaryFiles
 * 
 * @author Viveris Technologies
 */
public class ProductMessageDto extends GenericMessageDto<ProductDto> {

    /**
     * Default constructor
     */
    public ProductMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public ProductMessageDto(final long identifier,
            final String inputKey, final ProductDto body) {
        super(identifier, inputKey, body);
    }

}
