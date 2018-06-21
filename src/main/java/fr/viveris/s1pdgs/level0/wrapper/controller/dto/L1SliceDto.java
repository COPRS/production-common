package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

/**
 * DTO class for L0 slices
 * 
 * @author Viveris Technologies
 */
public class L1SliceDto extends ProductDto {

    /**
     * @param productName
     * @param keyObjectStorage
     */
    public L1SliceDto(final String productName, final String keyObjectStorage) {
        super(productName, keyObjectStorage, ProductFamily.L1_PRODUCT);
    }
}
