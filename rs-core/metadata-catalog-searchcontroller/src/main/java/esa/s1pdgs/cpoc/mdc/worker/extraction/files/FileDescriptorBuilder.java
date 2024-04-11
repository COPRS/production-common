/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mdc.worker.extraction.files;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.FileExtension;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;

/**
 * Service to build file descriptor
 * 
 * @author Cyrielle Gailliard
 *
 */
public class FileDescriptorBuilder {

	static final List<String> AUX_ECE_TYPES = Arrays.asList("AMV_ERRMAT", "AMH_ERRMAT");

	/**
	 * Pattern for files to extract data
	 */
	private final Pattern pattern;

	/**
	 * Local directory for files
	 */
	private final File localDirectory;

	/**
	 * Constructor
	 * 
	 * @param localDirectory
	 * @param pattern
	 */
	public FileDescriptorBuilder(final File localDirectory, final Pattern pattern) {
		this.localDirectory = localDirectory;
		this.pattern = pattern;
	}
	
	public Pattern getPattern() {
		return pattern;
	}

	@Override
	public String toString() {
		return String.format("localDirectory : %s, pattern : %s", localDirectory, pattern.toString());
	}

	/**
	 * Build descriptor for configuration files from path file
	 * 
	 * @param file
	 * 
	 * @return the config file descriptor
	 * 
	 * @throws FilePathException if we have
	 */
	public AuxDescriptor buildAuxDescriptor(final File file)
			throws MetadataFilePathException, MetadataIgnoredFileException {
		// Extract object storage key
		final String absolutePath = file.getAbsolutePath();
		if (absolutePath.length() <= localDirectory.getAbsolutePath().length()) {
			throw new MetadataFilePathException(absolutePath, "CONFIG", "File is not in root directory");
		}
		String relativePath = absolutePath.substring(localDirectory.getAbsolutePath().length() + 1);
		relativePath = relativePath.replace("\\", "/");

		// Ignored if directory
		if (file.isDirectory()) {
			throw new MetadataIgnoredFileException(file.getName());
		}

		// final Matcher m = pattern.matcher(relativePath);
		
		final AuxFilenameMetadataExtractor auxExtract = new AuxFilenameMetadataExtractor(pattern.matcher(relativePath)); 
		if (!auxExtract.matches()) {
			throw new MetadataFilePathException(relativePath, "CONFIG",
					String.format("File %s does not match the configuration file pattern %s", relativePath, pattern));
		}				
		
		// Extract product name
		String productName = relativePath;
		final int indexFirstSeparator = relativePath.indexOf("/");
		if (indexFirstSeparator != -1) {
			productName = relativePath.substring(0, indexFirstSeparator);
		}
		// Extract filename
		String filename = relativePath;
		final int indexLastSeparator = relativePath.lastIndexOf("/");
		if (indexLastSeparator != -1) {
			filename = relativePath.substring(indexLastSeparator + 1);
		}
		// Build descriptor
		final AuxDescriptor configFile = new AuxDescriptor();
		configFile.setFilename(filename);
		configFile.setRelativePath(relativePath);
		configFile.setKeyObjectStorage(productName);
		configFile.setProductName(productName);
		configFile.setMissionId(auxExtract.getMissionId());
		configFile.setSatelliteId(auxExtract.getSatelliteId());
		configFile.setProductClass(auxExtract.getProductClass());
		configFile.setProductType(auxExtract.getFileType());
		configFile.setProductFamily(ProductFamily.AUXILIARY_FILE);
		configFile.setExtension(FileExtension.valueOfIgnoreCase(auxExtract.getExtension()));
		return configFile;
	}

}
