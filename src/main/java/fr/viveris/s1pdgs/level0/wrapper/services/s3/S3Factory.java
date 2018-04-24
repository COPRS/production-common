package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.model.exception.CodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;

@Service
public class S3Factory {

	/**
	 * Amazon S3 service for configuration files
	 */
	private final ConfigFilesS3Services configFilesS3Services;

	/**
	 * Amazon S3 servive for session and raw files
	 */
	private final SessionFilesS3Services sessionFilesS3Services;

	private final L0SlicesS3Services l0SlicesS3Services;

	private final L0AcnsS3Services l0AcnsS3Services;

	private final L1SlicesS3Services l1SlicesS3Services;

	private final L1AcnsS3Services l1AcnsS3Services;

	@Autowired
	public S3Factory(final SessionFilesS3Services sessionFilesS3Services,
			final ConfigFilesS3Services configFilesS3Services, final L0SlicesS3Services l0SlicesS3Services,
			final L0AcnsS3Services l0AcnsS3Services, final L1SlicesS3Services l1SlicesS3Services,
			final L1AcnsS3Services l1AcnsS3Services) {
		this.sessionFilesS3Services = sessionFilesS3Services;
		this.configFilesS3Services = configFilesS3Services;
		this.l0SlicesS3Services = l0SlicesS3Services;
		this.l0AcnsS3Services = l0AcnsS3Services;
		this.l1SlicesS3Services = l1SlicesS3Services;
		this.l1AcnsS3Services = l1AcnsS3Services;
	}

	public void downloadFilesPerBatch(List<S3DownloadFile> filesToDownload) throws CodedException {
		// First check all families
		Map<String, DownloadFileCallable> callables = new HashMap<>();
		for (S3DownloadFile file : filesToDownload) {
			switch (file.getFamily()) {
			case RAW:
				callables.put(file.getKey(),
						new DownloadFileCallable(sessionFilesS3Services, file.getKey(), file.getLocalPath()));
				break;
			case CONFIG:
				callables.put(file.getKey(),
						new DownloadFileCallable(configFilesS3Services, file.getKey(), file.getLocalPath()));
				break;
			case L0_PRODUCT:
				callables.put(file.getKey(),
						new DownloadFileCallable(l0SlicesS3Services, file.getKey(), file.getLocalPath()));
				break;
			case L0_ACN:
				callables.put(file.getKey(),
						new DownloadFileCallable(l0AcnsS3Services, file.getKey(), file.getLocalPath()));
				break;
			default:
				throw new UnknownFamilyException("Family not managed for download in the object storage",
						file.getFamily().name());
			}
		}

		// Submit callables and wait for completion
		ExecutorService workerThread = Executors.newFixedThreadPool(filesToDownload.size());
		CompletionService<Boolean> service = new ExecutorCompletionService<>(workerThread);
		try {
			for (S3DownloadFile file : filesToDownload) {
				service.submit(callables.get(file.getKey()));
			}

			for (int i = 0; i < filesToDownload.size(); i++) {
				try {
					Future<Boolean> future;
					future = service.take();
					future.get(20, TimeUnit.MINUTES);
				} catch (InterruptedException | TimeoutException e) {
					throw new InternalErrorException(e.getMessage(), e);
				} catch (ExecutionException e) {
					if (e.getCause().getClass().isAssignableFrom(CodedException.class)) {
						throw (CodedException) e.getCause();
					} else {
						throw new InternalErrorException(e.getMessage(), e);
					}
				}
			}
		} finally {
			workerThread.shutdownNow();
			try {
				workerThread.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new InternalErrorException(e.getMessage(), e);
			}
		}
	}

	public void uploadFilesPerBatch(List<S3UploadFile> filesToUpload) throws CodedException {
		// First check all families
		Map<String, UploadFileCallable> callables = new HashMap<>();
		for (S3UploadFile file : filesToUpload) {
			switch (file.getFamily()) {
			case L0_PRODUCT:
				callables.put(file.getKey(), new UploadFileCallable(l0SlicesS3Services, file.getKey(), file.getFile()));
				break;
			case L0_ACN:
				callables.put(file.getKey(), new UploadFileCallable(l0AcnsS3Services, file.getKey(), file.getFile()));
				break;
			case L1_PRODUCT:
				callables.put(file.getKey(), new UploadFileCallable(l1SlicesS3Services, file.getKey(), file.getFile()));
				break;
			case L1_ACN:
				callables.put(file.getKey(), new UploadFileCallable(l1AcnsS3Services, file.getKey(), file.getFile()));
				break;
			default:
				throw new UnknownFamilyException("Family not managed for upload in the object storage",
						file.getFamily().name());
			}
		}

		// Submit callables and wait for completion
		ExecutorService workerThread = Executors.newFixedThreadPool(filesToUpload.size());
		CompletionService<Boolean> service = new ExecutorCompletionService<>(workerThread);
		try {
			for (S3UploadFile file : filesToUpload) {
				service.submit(callables.get(file.getKey()));
			}

			for (int i = 0; i < filesToUpload.size(); i++) {
				try {
					Future<Boolean> future;
					future = service.take();
					future.get(20, TimeUnit.MINUTES);
				} catch (InterruptedException | TimeoutException e) {
					throw new InternalErrorException(e.getMessage(), e);
				} catch (ExecutionException e) {
					if (e.getCause().getClass().isAssignableFrom(CodedException.class)) {
						throw (CodedException) e.getCause();
					} else {
						throw new InternalErrorException(e.getMessage(), e);
					}
				}
			}
		} finally {
			workerThread.shutdownNow();
			try {
				workerThread.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new InternalErrorException(e.getMessage(), e);
			}
		}
	}

}

class DownloadFileCallable implements Callable<Boolean> {

	private S3Services service;

	private String key;

	private String localPath;

	public DownloadFileCallable(S3Services service, String key, String localPath) {
		this.service = service;
		this.key = key;
		this.localPath = localPath;
	}

	@Override
	public Boolean call() throws CodedException {
		// Extract last final path
		String basePath = localPath;
		String filename = localPath;
		int lastIndex = localPath.lastIndexOf('/');
		if (lastIndex != -1) {
			filename = localPath.substring(lastIndex + 1);
			basePath = localPath.substring(0, lastIndex);
		}

		// Download objects in base path according object keys
		int nbFiles = service.downloadFiles(key, basePath);
		if (nbFiles <= 0) {
			throw new ObsUnknownObjectException(key, service.getBucketName(), "Object does not exist");
		}

		// If needed rename the file/folder
		if (!key.equals(filename)) {
			File fFrom = new File(basePath + "/" + key);
			File fTo = new File(localPath);
			fFrom.renameTo(fTo);
		}

		return true;
	}

}

class UploadFileCallable implements Callable<Boolean> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(UploadFileCallable.class);

	private S3Services service;

	private String key;

	private File file;

	public UploadFileCallable(S3Services service, String key, File file) {
		this.service = service;
		this.key = key;
		this.file = file;
	}

	@Override
	public Boolean call() throws CodedException {
		if (file.isDirectory()) {
			int nbObj = service.getNbObjects(key);
			if (nbObj > 0) {
				LOGGER.warn("{} objects prefixed by {} already exists in bucket {}: ignore it", nbObj, key,
						service.getBucketName());
			} else {
				service.uploadDirectory(key, file);
			}
		} else {
			if (service.exist(key)) {
				LOGGER.warn("Object {} already exists in bucket {}: ignore it", key, service.getBucketName());
			} else {
				service.uploadFile(key, file);
			}
		}
		return true;
	}

}