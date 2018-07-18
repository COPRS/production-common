package fr.viveris.s1pdgs.jobgenerator.service;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;
import fr.viveris.s1pdgs.jobgenerator.exception.InvalidFormatProduct;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.service.s3.ObsService;

/**
 * Class for managing EDRS session files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class EdrsSessionFileService {

	/**
	 * S3 service
	 */
	private final ObsService obsService;

	/**
	 * XML converter
	 */
	private final XmlConverter xmlConverter;

	/**
	 * Local directory to upload EDRS session file
	 */
	private final String pathTempDirectory;

	/**
	 * Constructor
	 * 
	 * @param s3Services
	 * @param xmlConverter
	 * @param pathTempDirectory
	 */
	@Autowired
	public EdrsSessionFileService(final ObsService obsService, final XmlConverter xmlConverter,
			@Value("${level0.dir-extractor-sessions}") final String pathTempDirectory) {
		this.obsService = obsService;
		this.xmlConverter = xmlConverter;
		this.pathTempDirectory = pathTempDirectory;
	}

	/**
	 * Create an object EdrsSessionFile from the key in object storage. We will get
	 * the file from the object storage and convert it into an object
	 * 
	 * @param keyObjectStorage
	 * @param channelId
	 * @return
	 * @throws InvalidFormatProduct
	 * @throws ObjectStorageException
	 */
	public EdrsSessionFile createSessionFile(final String keyObjectStorage) throws AbstractCodedException {

		// Download file
		File tmpFile = obsService.downloadFile(ProductFamily.EDRS_SESSION, keyObjectStorage, this.pathTempDirectory);

		// Convert it
		try {
			return (EdrsSessionFile) xmlConverter.convertFromXMLToObject(tmpFile.getAbsolutePath());
		} catch (IOException | JAXBException e) {
			throw new InternalErrorException("Cannot convert file " + keyObjectStorage, e);
		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}

}
