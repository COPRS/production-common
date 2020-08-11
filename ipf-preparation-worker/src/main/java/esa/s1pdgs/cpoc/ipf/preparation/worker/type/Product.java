package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public interface Product {
	public static Product nullProduct(final AppDataJob job) {
		return new Product() {		
			@Override
			public AppDataJobProduct toProduct() {
				return job.getProduct();
			}
		};
	}
	
	Logger LOGGER = LogManager.getLogger(Product.class);
	
	AppDataJobProduct toProduct();
}