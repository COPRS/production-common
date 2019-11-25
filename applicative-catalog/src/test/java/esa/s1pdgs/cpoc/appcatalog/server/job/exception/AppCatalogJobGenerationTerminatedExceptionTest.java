package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class AppCatalogJobGenerationTerminatedExceptionTest {
	
	static final class ExampleDto extends AbstractMessage {
		public ExampleDto() {
			//super("", ProductFamily.BLANK);
		}
		
		@Override
		public String toString() {
			return "body";
		}
	}
	
    @Test
    public void testConstructors() {
    	List<GenericMessageDto<? extends AbstractMessage>> list = Arrays.asList(
        		new GenericMessageDto<ExampleDto>(1, "msg1", new ExampleDto()),
        		new GenericMessageDto<ExampleDto>(2, "msg2", new ExampleDto()),
        		new GenericMessageDto<ExampleDto>(3, "msg3", new ExampleDto())
        		);
        AppCatalogJobGenerationTerminatedException obj =
                new AppCatalogJobGenerationTerminatedException("product-name", list);
        assertEquals(ErrorCode.JOB_GENERATION_TERMINATED, obj.getCode());
        assertEquals("product-name", obj.getProductName());
        assertEquals(list, obj.getMqiMessages());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobGenerationTerminatedException obj =
                new AppCatalogJobGenerationTerminatedException("state-error",
                        Arrays.asList(
                        		new GenericMessageDto<ExampleDto>(1, "msg1", new ExampleDto()),
                        		new GenericMessageDto<ExampleDto>(2, "msg2", new ExampleDto()),
                        		new GenericMessageDto<ExampleDto>(3, "msg3", new ExampleDto())                        		
                        		));
        String str = obj.getLogMessage();
        assertTrue(str.contains("[productName state-error]"));
        assertTrue(str.contains("[mqiMessages " + Arrays.asList(
        		new GenericMessageDto<ExampleDto>(1, "msg1", new ExampleDto()),
        		new GenericMessageDto<ExampleDto>(2, "msg2", new ExampleDto()),
        		new GenericMessageDto<ExampleDto>(3, "msg3", new ExampleDto())                        		
        		).toString() + "]"));
    }

}
