package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class Product<E extends AbstractMessage> {	
	private ProductFamily family;
	private URI uri;
	private E dto;
		
	public Product() {
	}

	public Product(final ProductFamily family, final URI uri, final E dto) {
		this.family = family;
		this.uri = uri;
		this.dto = dto;
	}

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

	
	@Override
	public int hashCode() {
		return Objects.hash(dto, family, uri);
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
		return Objects.equals(dto, other.dto) && 
				family == other.family && 
				Objects.equals(uri, other.uri);
	}

	@Override
	public String toString() {
		return "Product [family=" + family + ", uri=" + uri + ", dto=" + dto + "]";
	}
}
