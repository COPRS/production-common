package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public interface Product {
	Logger LOGGER = LogManager.getLogger(Product.class);
	
	AppDataJobProduct toProduct();
}