package esa.s1pdgs.cpoc.ingestion;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.product.Product;
import esa.s1pdgs.cpoc.ingestion.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.product.ProductService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class TestIngestionService {

	@Test
	public final void testOnMessage() {
		final IngestionDto ingestion = new IngestionDto("fooBar", "file:///tmp/foo/bar");
		
		final GenericMessageDto<IngestionDto> mess = new GenericMessageDto<IngestionDto>();
		mess.setIdentifier(123);
		mess.setInputKey("testKEy");
		mess.setBody(ingestion);
		
		final ProductService fakeProductService = new ProductService() {			
			@Override
			public void markInvalid(IngestionDto ingestion) {}
			
			@Override
			public <E extends AbstractDto> List<Product<E>> ingest(ProductFamily family, IngestionDto ingestion)
					throws ProductException, InternalErrorException {
				return Collections.emptyList();
			}
		};		
		final IngestionService uut = new IngestionService(
				null, 
				ErrorRepoAppender.NULL, 
				new IngestionServiceConfigurationProperties(), 
				fakeProductService
		);		
		uut.onMessage(mess);
	}
}
