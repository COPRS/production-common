package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.io.File;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class Product<E extends AbstractMessage> {	
	private ProductFamily family;
	private File file;
	private E dto;
	
	public ProductFamily getFamily() {
		return family;
	}
	
	public void setFamily(final ProductFamily family) {
		this.family = family;
	}
	
	public E getDto() {
		return dto;
	}
	
	public void setDto(final E dto) {
		this.dto = dto;
	}

	public File getFile() {
		return file;
	}

	public void setFile(final File file) {
		this.file = file;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(dto, family, file);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Product other = (Product) obj;
		return Objects.equals(dto, other.dto) && family == other.family && Objects.equals(file, other.file);
	}

	@Override
	public String toString() {
		return "Product [family=" + family + ", file=" + file + ", dto=" + dto + "]";
	}
}
