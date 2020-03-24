package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ingestion.worker.obs.ObsAdapter;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

@Service
public class ProductServiceImpl implements ProductService {
	static final Logger LOG = LogManager.getLogger(ProductServiceImpl.class);

	private final ObsClient obsClient;

	@Autowired
	public ProductServiceImpl(final ObsClient obsClient) {
		this.obsClient = obsClient;
	}

	@Override
	public IngestionResult ingest(final ProductFamily family, final IngestionJob ingestion,
			final ReportingFactory reportingFactory)
			throws ProductException, InternalErrorException, ObsEmptyFileException {
		final File file = toFile(ingestion);
		assertPermissions(ingestion, file);

		final ObsAdapter obsAdapter = newObsAdapterFor(reportingFactory);

		final IngestionEvent dto = new IngestionEvent();
		dto.setProductName(ingestion.getKeyObjectStorage());
		dto.setProductFamily(family);
		dto.setKeyObjectStorage(ingestion.getKeyObjectStorage());
		dto.setRelativePath(ingestion.getRelativePath());

		final Product<IngestionEvent> prod = new Product<>();
		prod.setFamily(family);
		prod.setFile(file);
		prod.setDto(dto);

		long transferAmount = 0L;

		if (ingestion.getProductFamily() == ProductFamily.INVALID) {
			if (family == ProductFamily.INVALID) {
				LOG.debug("Product ingestion of file {} has been already restarted before", file);
			} else {
				LOG.debug("Moving file {} with obs key {} from family {} to family {}", file,
						ingestion.getKeyObjectStorage(), ProductFamily.INVALID, family);
				obsAdapter.move(ProductFamily.INVALID, family, file, ingestion.getKeyObjectStorage());
			}
		} else {
			obsAdapter.upload(family, file, ingestion.getKeyObjectStorage());
			transferAmount = FileUtils.sizeOf(file);
		}
		return new IngestionResult(Collections.singletonList(prod), transferAmount);
	}

	@Override
	public void markInvalid(final IngestionJob ingestion, final ReportingFactory reportingFactory)
			throws ObsEmptyFileException, InternalErrorException {
		final File file = toFile(ingestion);
		final ObsAdapter obsAdapter = newObsAdapterFor(reportingFactory);
		obsAdapter.upload(ProductFamily.INVALID, file, ingestion.getKeyObjectStorage());
	}

	@Override
	public void assertFileIsNotEmpty(final IngestionJob ingestion) throws ObsEmptyFileException, InternalErrorException {
		final File file = toFile(ingestion);
		if (FileUtils.sizeOf(file) == 0) {
			throw new ObsEmptyFileException("Empty file detected: " + file.getName());
		}
	}

	final String toObsKey(final Path relPath) {
		return relPath.toString();
	}

	private final ObsAdapter newObsAdapterFor(final ReportingFactory reportingFactory) {
		return new ObsAdapter(obsClient, reportingFactory);
	}

	static void assertPermissions(final IngestionJob ingestion, final File file) {
		if (!file.exists()) {
			throw new RuntimeException(String.format("File %s of %s does not exist", file, ingestion));
		}
		if (!file.canRead()) {
			throw new RuntimeException(String.format("File %s of %s is not readable", file, ingestion));
		}
		if (!file.canWrite()) {
			throw new RuntimeException(String.format("File %s of %s is not writeable", file, ingestion));
		}
	}

	static final File toFile(final IngestionJob ingestion) throws InternalErrorException {

		URI ingestionBaseURL;
		try {
			ingestionBaseURL = new URI(ingestion.getPickupBaseURL());
		} catch (URISyntaxException e) {
			throw new InternalErrorException(String.format("URL syntax not correct for %s", ingestion.getProductName()), e);
		}
		if (!ingestionBaseURL.getScheme().equals("file")) {
			throw new InternalErrorException(
					String.format("URL scheme is not file for %s", ingestion.getProductName()));
		}
		return Paths.get(ingestionBaseURL).resolve(ingestion.getRelativePath()).toFile();
	}
}
