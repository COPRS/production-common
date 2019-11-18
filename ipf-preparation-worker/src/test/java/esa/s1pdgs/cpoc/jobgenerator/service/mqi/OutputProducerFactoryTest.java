package esa.s1pdgs.cpoc.jobgenerator.service.mqi;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public class OutputProducerFactoryTest {

    @Mock
    private GenericMqiClient sender;

    private OutputProducerFactory factory;

    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(sender).publish(Mockito.any(), Mockito.any());

        factory = new OutputProducerFactory(sender);
    }

    @Test
    public void testSendJob() throws AbstractCodedException {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        GenericMessageDto<ProductDto> message =
                new GenericMessageDto<ProductDto>(123, "key",
                        new ProductDto("level-name", "key-obs",
                                ProductFamily.L0_SLICE, "NRT"));

        GenericPublicationMessageDto<LevelJobDto> expected =
                new GenericPublicationMessageDto<LevelJobDto>(123L,
                        ProductFamily.L1_JOB, dto);
        expected.setInputKey("key");
        expected.setOutputKey("L1_JOB");

        factory.sendJob(message, dto);
        verify(sender, times(1)).publish(Mockito.eq(expected),Mockito.eq(ProductCategory.LEVEL_JOBS));
    }
//
//    @Test
//    public void testSendError() throws AbstractCodedException {
//        factory.sendError("error message");
//        
//        verify(errorService, times(1)).publish(Mockito.eq("error message"));
//        verifyZeroInteractions(sender);
//    }
//
//    @Test
//    public void testSendErrorWhenException() throws AbstractCodedException {
//        doThrow(new InternalErrorException("exception")).when(errorService)
//                .publish(Mockito.anyString());
//        factory.sendError("error message");
//
//        verify(errorService, times(1)).publish(Mockito.eq("error message"));
//        verifyZeroInteractions(sender);
//    }
}
