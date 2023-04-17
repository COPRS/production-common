package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;

/**
 * Some utility methods taken over from {@link OutputProcessor} for handling of output product files
 *
 */
public class OutputUtils {
	
	private static final Logger LOGGER = LogManager.getLogger(OutputUtils.class);
	
	protected static final String EXT_ISIP = "ISIP";

	protected static final String EXT_SAFE = "SAFE";
	
	private final String prefixMonitorLogs;
	
	private final ApplicationProperties properties;
	
	public OutputUtils(final ApplicationProperties properties, final String prefixMonitorLogs) {
		this.properties = properties;
		this.prefixMonitorLogs = prefixMonitorLogs;
	}
	
	public boolean listFileExists(final String listFile, final String workDirectory) {
		
		if (listFile.contains("*")) {
			File dir = new File(workDirectory);
			FileFilter fileFilter = new WildcardFileFilter(listFile);
			List<File> files = Arrays.asList(dir.listFiles(fileFilter));

			if (files.size() != 1) {
				return false;
			} else {
				return true;
			}
			
		} else {
			return Paths.get(listFile).toFile().exists();
		}
	}
	
	public List<String> extractFiles(final String listFile, final String workDirectory) throws InternalErrorException {
		LOGGER.info("{} 1 - Extracting list of outputs", prefixMonitorLogs);
		try {
			// Allow wildcard * for List-File, searching for *.LIST
			if (listFile.contains("*")) {
				File dir = new File(workDirectory);
				FileFilter fileFilter = new WildcardFileFilter(listFile);
				List<File> files = Arrays.asList(dir.listFiles(fileFilter));

				if (files.size() != 1) {
					LOGGER.error("Found an unexpected number of LIST-files. Expected 1 found {}.", files.size());
					throw new InternalErrorException(
							"Found an unexpected number of LIST-files. Expected 1 found " + files.size() + ".");
				}
				
				try (Stream<String> input = Files.lines(files.get(0).toPath())) {
					return input.collect(Collectors.toList());
				}
			} else {
				try (Stream<String> input = Files.lines(Paths.get(listFile))) {
					return input.collect(Collectors.toList());
				}				
			}
		} catch (final IOException | NullPointerException ioe) {
			LOGGER.error("Cannot parse result list file {}: {}", listFile, ioe.getMessage());
			throw new InternalErrorException("Cannot parse result list file " + listFile + ": " + ioe.getMessage(),
					ioe);
		}
	}
	
	public ProductFamily familyOf(final LevelJobOutputDto output, ApplicationLevel appLevel) {
		final ProductFamily family = ProductFamily.fromValue(output.getFamily());
		if (family == ProductFamily.L0_SLICE && appLevel == ApplicationLevel.L0){			
			return ProductFamily.L0_SEGMENT;
		}
		return family;
	}
	
	public LevelJobOutputDto levelJobOutputDtoOfProductType(String productType,  List<LevelJobOutputDto> authorizedOutputs) {
		
		for (LevelJobOutputDto l: authorizedOutputs) {
			if(l.getRegexp().contains(productType)) {
				return l;
			}
		}
		return null;
		
	}
	
	/**
	 * Extract the product name from the line of the result file
	 * 
	 */
	public String getProductName(final String line) {
		// Extract the product name and the complete filepath
		// First, remove the first directory (NRT or REPORT)
		String productName = line;
		final int index = line.indexOf('/');
		if (index != -1) {
			productName = line.substring(index + 1);
		}
		// Second: if file ISIP, retrieve only .SAFE
		if (properties.isChangeIsipToSafe() && productName.toUpperCase().endsWith(EXT_ISIP)) {
			productName = productName.substring(0, productName.length() - EXT_ISIP.length()) + EXT_SAFE;
		}
		return productName;
	}

}
