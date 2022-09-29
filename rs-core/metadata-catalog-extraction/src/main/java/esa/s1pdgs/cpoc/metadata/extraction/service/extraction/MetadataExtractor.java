package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public interface MetadataExtractor {	
    interface ThrowingSupplier<E> {
    	E get() throws AbstractCodedException;
    }
    
	public static final MetadataExtractor FAIL = new MetadataExtractor() {
		@Override
		public final ProductMetadata extract(final ReportingFactory reportingFactory, final CatalogJob message) {
			throw new IllegalArgumentException(
				String.format("No Metadata extractor defined for catalog job %s", message)	
			);
		}		
	};	
	ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob message) throws AbstractCodedException;
}
