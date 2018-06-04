package fr.viveris.s1pdgs.ingestor.files.services;

import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.exceptions.FilePathException;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;

/**
 * Service for managing file descriptor for auxiliary files
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class AuxiliaryFileDescriptorService extends AbstractFileDescriptorService {

	/**
	 * Family
	 */
	private final static String FAMILY_STR = "CONFIG";

	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_STR = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";

	/**
	 * Constructor
	 * 
	 * @param directory
	 */
	@Autowired
	public AuxiliaryFileDescriptorService(@Value("${file.auxiliary-files.local-directory}") final String directory) {
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

		// Extract product name
		String productName = relativePath;
		boolean isRoot = true;
		int indexFirstSeparator = relativePath.indexOf("/");
		if (indexFirstSeparator != -1) {
			productName = relativePath.substring(0, indexFirstSeparator);
			isRoot = false;
		}
		// Extract filename
		String filename = relativePath;
		int indexLastSeparator = relativePath.lastIndexOf("/");
		if (indexFirstSeparator != -1) {
			filename = relativePath.substring(indexLastSeparator + 1);
		}
		// Build descriptor
		FileDescriptor configFile = new FileDescriptor();
		configFile.setRelativePath(relativePath);
		configFile.setKeyObjectStorage(relativePath);
		configFile.setProductName(productName);
		configFile.setHasToBePublished(false);
		if (isRoot || filename.equalsIgnoreCase("manifest.safe")) {
			configFile.setHasToBePublished(true);
		}
		configFile.setMissionId(matcher.group(1));
		configFile.setSatelliteId(matcher.group(2));
		return configFile;
	}

}
