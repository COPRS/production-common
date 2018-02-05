package fr.viveris.s1pdgs.ingestor.services.file;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.ConfigFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileDescriptor;
import fr.viveris.s1pdgs.ingestor.model.ErdsSessionFileType;
import fr.viveris.s1pdgs.ingestor.model.FileExtension;

/**
 * Service to build file descriptor
 * @author Cyrielle Gailliard
 *
 */
@Service
public class FileDescriptorBuilder {
	
	/**
	 * Pattern for configuration files to extract data
	 */
	private final static String PATTERN_CONFIG = "^([0-9a-z][0-9a-z]){1}([0-9a-z]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_CAL|AUX_INS|AUX_RESORB|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$";
	
	/**
	 * Local directory for configuration files
	 */
	@Value("${file.config-files.local-directory}")
	public String configLocalDirectory;

	/**
	 * Local directory for ERDS session files
	 */
	@Value("${file.session-files.local-directory}")
	public String sessionLocalDirectory;

	public ConfigFileDescriptor buildConfigFileDescriptor(File file) throws IllegalArgumentException {
		
		// Extract object storage key
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= configLocalDirectory.length()) {
			//TODO custom exception
			throw new IllegalArgumentException("Filename " + absolutePath + " is too short");
		}
		String relativePath = absolutePath.substring(configLocalDirectory.length());
		relativePath = relativePath.replace("\\", "/");
		
		// Check if key matches the pattern
		ConfigFileDescriptor configFile = null;
		Pattern p = Pattern.compile(PATTERN_CONFIG, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(relativePath);
		if (m.matches()) {
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
			configFile = new ConfigFileDescriptor();
			configFile.setFilename(filename);
			configFile.setRelativePath(relativePath);
			configFile.setKeyObjectStorage(relativePath);
			configFile.setProductName(productName);
			configFile.setMissionId(m.group(1));
			configFile.setSatelliteId(m.group(2));
			configFile.setProductClass(m.group(4));
			configFile.setProductType(m.group(5));
			configFile.setExtension(FileExtension.valueOfIgnoreCase(m.group(6).toUpperCase()));
			configFile.setHasToExtractMetadata(false);
			if (isRoot && configFile.getExtension() != FileExtension.SAFE) {
				configFile.setHasToExtractMetadata(true);
			}
			if (!isRoot && filename.equalsIgnoreCase("manifest.safe")) {
				configFile.setHasToExtractMetadata(true);
			}
			if (file.isDirectory()) {
				configFile.setDirectory(true);
				configFile.setHasToBeStored(false);
			} else {
				configFile.setDirectory(false);
				configFile.setHasToBeStored(true);
			}
			
			
		} else {
			//TODO custom exception
			throw new IllegalArgumentException("File " + relativePath + " does not match the configuration file pattern");
		}
		
		return configFile;
	}

	public ErdsSessionFileDescriptor buildErdsSessionFileDescriptor(File file) throws IllegalArgumentException {
		// Extract relative path
		String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= sessionLocalDirectory.length()) {
			//TODO custom exception
			throw new IllegalArgumentException("Filename " + absolutePath + " is too short");
		}
		String relativePath = absolutePath.substring(sessionLocalDirectory.length());
		relativePath = relativePath.replace("\\", "/");
		
		// Ignored if directory
		if (file.isDirectory()) {
			//TODO custom exception
			throw new IllegalArgumentException("Ignored directory " + relativePath);
		}
		
		// Check minimal format AAA/{session_id}/chx/{raw or XML file}
		String[] paths = relativePath.split("/");
		if (paths.length != 4) {
			//TODO custom exception
			throw new IllegalArgumentException("Invalid relative path " + relativePath);
		}
		if (paths[0].length() != 3) {
			//TODO custom exception
			throw new IllegalArgumentException("Invalid mission and satellite id for " + relativePath);
		}
		if (paths[2].length() != 4 || !paths[2].substring(0, 2).toLowerCase().equals("ch")) {
			//TODO custom exception
			throw new IllegalArgumentException("Invalid channel for " + relativePath);
		}
		if (!paths[3].toLowerCase().endsWith(".xml") && !paths[3].toLowerCase().endsWith(".raw")) {
			//TODO custom exception
			throw new IllegalArgumentException("Unknown extension file for " + relativePath);
		}
		if (!paths[3].contains(paths[1])) {
			//TODO custom exception
			throw new IllegalArgumentException("Filename does not contain the specified session identifier " + relativePath);
		}
		
		// 
		ErdsSessionFileDescriptor descriptor = new ErdsSessionFileDescriptor();
		descriptor.setFilename(paths[3]);
		descriptor.setRelativePath(relativePath);
		descriptor.setProductName(paths[3]);
		descriptor.setExtension(FileExtension.valueOfIgnoreCase(paths[3].substring(paths[3].length()-3)));
		descriptor.setProductType(ErdsSessionFileType.valueFromExtension(descriptor.getExtension()));
		descriptor.setMissionId(paths[0].substring(0, 2));
		descriptor.setSatelliteId(paths[0].substring(2));
		descriptor.setChannel(Integer.parseInt(paths[2].substring(3)));
		descriptor.setSessionIdentifier(paths[1]);
		descriptor.setKeyObjectStorage(paths[1] + "/" + paths[2] + "/" + paths[3]);
		
		return descriptor;
	}
}
