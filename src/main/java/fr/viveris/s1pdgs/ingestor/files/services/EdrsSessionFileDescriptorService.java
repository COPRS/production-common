package fr.viveris.s1pdgs.ingestor.files.services;

import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.exceptions.FilePathException;
import fr.viveris.s1pdgs.ingestor.files.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.model.FileExtension;

/**
 * Service for managing file descriptor for auxiliary files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class EdrsSessionFileDescriptorService extends AbstractFileDescriptorService {

	/**
	 * Family
	 */
	private final static String FAMILY_STR = "SESSION";

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_STR = "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";

	/**
	 * Constructor
	 * 
	 * @param directory
	 */
	@Autowired
	public EdrsSessionFileDescriptorService(@Value("${file.session-files.local-directory}") final String directory) {
		super(directory, PATTERN_STR, FAMILY_STR);
	}

	/**
	 * @throws FilePathException
	 * 
	 */
	@Override
	protected FileDescriptor buildFromMatcher(final Matcher matcher, final String relativePath)
			throws FilePathException {
		if (!matcher.matches()) {
			throw new FilePathException(relativePath, relativePath, family, "File does not match the pattern");
		}

		// Ignore the IIF files
		if (matcher.group(11).toLowerCase().contains("iif_")) {
			throw new FilePathException(relativePath, relativePath, family, "IIF file");
		}

		// "^([a-z0-9][a-z0-9])([a-z0-9])(/|\\\\)(\\w+)(/|\\\\)(ch)(0[1-2])(/|\\\\)((\\w*)\\4(\\w*)\\.(XML|RAW))$";
		FileDescriptor descriptor = new FileDescriptor();
		descriptor.setRelativePath(relativePath);
		descriptor.setProductName(matcher.group(9));
		descriptor.setExtension(FileExtension.valueOfIgnoreCase(matcher.group(12)));
		descriptor.setProductType(EdrsSessionFileType.valueFromExtension(descriptor.getExtension()));
		descriptor.setChannel(Integer.parseInt(matcher.group(7)));
		descriptor.setKeyObjectStorage(relativePath);
		descriptor.setMissionId(matcher.group(1));
		descriptor.setSatelliteId(matcher.group(2));
		descriptor.setHasToBePublished(true);

		return descriptor;
	}

}
