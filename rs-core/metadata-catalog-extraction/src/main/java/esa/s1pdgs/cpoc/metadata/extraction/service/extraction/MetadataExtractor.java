/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
