package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.FileQueueMessage;
import fr.viveris.s1pdgs.level0.wrapper.model.kafka.ObsQueueMessage;

@Service
public class OutputProcuderFactory {

	/**
	 * Kafka producer for L0 slices
	 */
	private final L0SlicesProducer senderL0Products;

	/**
	 * Kafka producer for L0 ACNs
	 */
	private final L0ACNsProducer senderL0Acns;

	/**
	 * Kafka producer for L1 slices
	 */
	private final L1SlicesProducer senderL1Products;

	/**
	 * Kafka producer for L1 ACNs
	 */
	private final L1ACNsProducer senderL1Acns;

	/**
	 * Kafka producer for report
	 */
	private final L0ReportProducer senderL0Reports;

	/**
	 * Kafka producer for report
	 */
	private final L1ReportProducer senderL1Reports;

	@Autowired
	public OutputProcuderFactory(final L0SlicesProducer senderL0Products, final L0ACNsProducer senderL0Acns,
			final L0ReportProducer senderL0Reports, final L1SlicesProducer senderL1Products,
			final L1ACNsProducer senderL1Acns, final L1ReportProducer senderL1Reports) {
		this.senderL0Products = senderL0Products;
		this.senderL0Acns = senderL0Acns;
		this.senderL0Reports = senderL0Reports;
		this.senderL1Products = senderL1Products;
		this.senderL1Acns = senderL1Acns;
		this.senderL1Reports = senderL1Reports;
	}

	public void sendOutput(FileQueueMessage msg) throws CodedException {
		try {
			switch (msg.getFamily()) {
			case L0_REPORT:
				ReportDto dtoReport = new ReportDto(msg.getProductName(), this.readFile(msg.getFile()), msg.getFamily().toString());
				this.senderL0Reports.send(dtoReport);
				break;
			case L1_REPORT:
				ReportDto dtoL1Report = new ReportDto(msg.getProductName(), this.readFile(msg.getFile()), msg.getFamily().toString());
				this.senderL1Reports.send(dtoL1Report);
				break;
			default:
				throw new UnknownFamilyException("Invalid family for sending FileQueueMessage ",
						msg.getFamily().name());
			}
		} catch (IOException ioe) {
			throw new InternalErrorException("Cannot read file for " + msg.getProductName() + ": " + ioe.getMessage(),
					ioe);
		}
	}

	public void sendOutput(ObsQueueMessage msg) throws CodedException {
		switch (msg.getFamily()) {
		case L0_PRODUCT:
			L0SliceDto dtoSlice = new L0SliceDto(msg.getProductName(), msg.getKeyObs(), msg.getFamily().toString());
			this.senderL0Products.send(dtoSlice);
			break;
		case L0_ACN:
			L0AcnDto dtoAcn = new L0AcnDto(msg.getProductName(), msg.getKeyObs());
			this.senderL0Acns.send(dtoAcn);
			break;
		case L1_PRODUCT:
			L1SliceDto dtoSlice1 = new L1SliceDto(msg.getProductName(), msg.getKeyObs(), msg.getFamily().toString());
			this.senderL1Products.send(dtoSlice1);
			break;
		case L1_ACN:
			L1AcnDto dtoAcn1 = new L1AcnDto(msg.getProductName(), msg.getKeyObs());
			this.senderL1Acns.send(dtoAcn1);
			break;
		default:
			throw new UnknownFamilyException("Invalid family for sending ObsQueueMessage ", msg.getFamily().name());
		}
	}

	private String readFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
	}
}
