package esa.s1pdgs.cpoc.ingestion.product;

import java.io.File;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;

public class Product<E extends AbstractDto> {	
	private ProductFamily family;
	private File file;
	private E dto;
	
	public ProductFamily getFamily() {
		return family;
	}
	
	public void setFamily(ProductFamily family) {
		this.family = family;
	}
	
	public E getDto() {
		return dto;
	}
	
	public void setDto(E dto) {
		this.dto = dto;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "Product [family=" + family + ", file=" + file + ", dto=" + dto + "]";
	}
}
