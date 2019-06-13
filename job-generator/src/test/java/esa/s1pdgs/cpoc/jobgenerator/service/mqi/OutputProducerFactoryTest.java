package esa.s1pdgs.cpoc.jobgenerator.service.mqi;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.client.ErrorService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public class OutputProducerFactoryTest {

    @Mock
    private GenericMqiService<LevelJobDto> sender;

    @Mock
    private ErrorService errorService;

    private OutputProducerFactory factory;

    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(sender).publish(Mockito.any());
        doNothing().when(errorService).publish(Mockito.anyString());

        factory = new OutputProducerFactory(sender, errorService);
    }

    @Test
    public void testSendJob() throws AbstractCodedException {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L1_JOB, "product-name", "NRT",
                "work-dir", "job-order");
        GenericMessageDto<LevelProductDto> message =
                new GenericMessageDto<LevelProductDto>(123, "key",
                        new LevelProductDto("level-name", "key-obs",
                                ProductFamily.L0_SLICE, "NRT"));

        GenericPublicationMessageDto<LevelJobDto> expected =
                new GenericPublicationMessageDto<LevelJobDto>(123L,
                        ProductFamily.L1_JOB, dto);
        expected.setInputKey("key");
        expected.setOutputKey("L1_JOB");

        factory.sendJob(message, dto);
        verify(sender, times(1)).publish(Mockito.eq(expected));
        verifyZeroInteractions(errorService);
    }

    @Test
    public void testSendError() throws AbstractCodedException {
        factory.sendError("error message");
        
        verify(errorService, times(1)).publish(Mockito.eq("error message"));
        verifyZeroInteractions(sender);
    }

    @Test
    public void testSendErrorWhenException() throws AbstractCodedException {
        doThrow(new InternalErrorException("exception")).when(errorService)
                .publish(Mockito.anyString());
        factory.sendError("error message");

        verify(errorService, times(1)).publish(Mockito.eq("error message"));
        verifyZeroInteractions(sender);
    }
}
