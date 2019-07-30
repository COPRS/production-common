package esa.s1pdgs.cpoc.ingestion.product;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;

public class ProductFactory {

	public Product<? extends AbstractDto> newProduct()
	{
		return new Product<>();
	}
}
