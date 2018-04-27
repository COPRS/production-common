package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.KafkaSendException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.FileQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.ObsQueueMessage;

public class OutputProducerFactoryTest {

	/**
	 * Kafka producer for L0 slices
	 */
	@Mock
	private L0SlicesProducer senderProducts;

	/**
	 * Kafka producer for L0 ACNs
	 */
	@Mock
	private L0ACNsProducer senderAcns;

	/**
	 * Kafka producer for report
	 */
	@Mock
	private L0ReportProducer senderL0Reports;

	/**
	 * Kafka producer for L0 slices
	 */
	@Mock
	private L1SlicesProducer senderL1Products;

	/**
	 * Kafka producer for L0 ACNs
	 */
	@Mock
	private L1ACNsProducer senderL1Acns;

	/**
	 * Kafka producer for report
	 */
	@Mock
	private L1ReportProducer senderL1Reports;

	private OutputProcuderFactory outputProcuderFactory;

	@Before
	public void init() throws KafkaSendException {
		MockitoAnnotations.initMocks(this);
		doNothing().when(senderProducts).send(Mockito.any());
		doNothing().when(senderAcns).send(Mockito.any());
		doNothing().when(senderL0Reports).send(Mockito.any());
		doNothing().when(senderL1Products).send(Mockito.any());
		doNothing().when(senderL1Acns).send(Mockito.any());
		doNothing().when(senderL1Reports).send(Mockito.any());
		this.outputProcuderFactory = new OutputProcuderFactory(senderProducts, senderAcns, senderL0Reports,
				senderL1Products, senderL1Acns, senderL1Reports);
	}

	@Test
	public void testSendReport() throws CodedException {
		this.outputProcuderFactory.sendOutput(
				new FileQueueMessage(ProductFamily.L0_REPORT, "test.txt", new File("./test/data/report.txt")));
		verify(this.senderProducts, never()).send(Mockito.any());
		verify(this.senderAcns, never()).send(Mockito.any());
		verify(this.senderL0Reports, times(1)).send(Mockito.eq(new ReportDto("test.txt", "Test report file")));
		verify(this.senderL1Products, never()).send(Mockito.any());
		verify(this.senderL1Acns, never()).send(Mockito.any());
		verify(this.senderL1Reports, never()).send(Mockito.any());
	}

	@Test
	public void testSendProduct() throws CodedException {
		this.outputProcuderFactory.sendOutput(new ObsQueueMessage(ProductFamily.L0_PRODUCT, "test.txt", "test.txt"));
		verify(this.senderProducts, times(1)).send(Mockito.eq(new L0SliceDto("test.txt", "test.txt")));
		verify(this.senderAcns, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
		verify(this.senderL1Products, never()).send(Mockito.any());
		verify(this.senderL1Acns, never()).send(Mockito.any());
		verify(this.senderL1Reports, never()).send(Mockito.any());
	}

	@Test
	public void testSendAcn() throws CodedException {
		this.outputProcuderFactory.sendOutput(new ObsQueueMessage(ProductFamily.L0_ACN, "test.txt", "test.txt"));
		verify(this.senderAcns, times(1)).send(Mockito.eq(new L0AcnDto("test.txt", "test.txt")));
		verify(this.senderProducts, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
		verify(this.senderL1Products, never()).send(Mockito.any());
		verify(this.senderL1Acns, never()).send(Mockito.any());
		verify(this.senderL1Reports, never()).send(Mockito.any());
	}

	@Test
	public void testSendL1Report() throws CodedException {
		this.outputProcuderFactory.sendOutput(
				new FileQueueMessage(ProductFamily.L1_REPORT, "test.txt", new File("./test/data/report.txt")));
		verify(this.senderProducts, never()).send(Mockito.any());
		verify(this.senderAcns, never()).send(Mockito.any());
		verify(this.senderL1Reports, times(1)).send(Mockito.eq(new ReportDto("test.txt", "Test report file")));
		verify(this.senderL1Products, never()).send(Mockito.any());
		verify(this.senderL1Acns, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
	}

	@Test
	public void testSendL1Product() throws CodedException {
		this.outputProcuderFactory.sendOutput(new ObsQueueMessage(ProductFamily.L1_PRODUCT, "test.txt", "test.txt"));
		verify(this.senderL1Products, times(1)).send(Mockito.eq(new L1SliceDto("test.txt", "test.txt")));
		verify(this.senderAcns, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
		verify(this.senderProducts, never()).send(Mockito.any());
		verify(this.senderL1Acns, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
	}

	@Test
	public void testSendL1Acn() throws CodedException {
		this.outputProcuderFactory.sendOutput(new ObsQueueMessage(ProductFamily.L1_ACN, "test.txt", "test.txt"));
		verify(this.senderL1Acns, times(1)).send(Mockito.eq(new L1AcnDto("test.txt", "test.txt")));
		verify(this.senderProducts, never()).send(Mockito.any());
		verify(this.senderL0Reports, never()).send(Mockito.any());
		verify(this.senderL1Products, never()).send(Mockito.any());
		verify(this.senderAcns, never()).send(Mockito.any());
		verify(this.senderL1Reports, never()).send(Mockito.any());
	}

	@Test(expected = UnknownFamilyException.class)
	public void testInvalidFamilyForFile() throws CodedException {
		this.outputProcuderFactory
				.sendOutput(new FileQueueMessage(ProductFamily.JOB, "test.txt", new File("./test/data/report.txt")));
	}
}
