package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.pdu.PDUType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3.S3TypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public class PDUTypeAdapter extends AbstractProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(S3TypeAdapter.class);

	private MetadataClient metadataClient;
	private TaskTableFactory ttFactory;
	private ElementMapper elementMapper;
	private ProcessSettings processSettings;
	private IpfPreparationWorkerSettings workerSettings;
	private PDUSettings settings;

	public PDUTypeAdapter(final MetadataClient metadataClient, final TaskTableFactory ttFactory,
			final ElementMapper elementMapper, final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings workerSettings, final PDUSettings settings) {
		this.metadataClient = metadataClient;
		this.ttFactory = ttFactory;
		this.elementMapper = elementMapper;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public List<AppDataJob> createAppDataJobs(IpfPreparationJob job) throws Exception {
		PDUTypeSettings typeSettings = settings.getConfig().get(job.getEventMessage().getBody().getProductType());

		if (typeSettings != null) {
			if (typeSettings.getType() == PDUType.FRAME) {
				PDUFrameGeneration jobGenerator = new PDUFrameGeneration(typeSettings, metadataClient);
				return jobGenerator.generateAppDataJobs(job);
			}
		}

		return null;
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// TODO Auto-generated method stub

	}

}
