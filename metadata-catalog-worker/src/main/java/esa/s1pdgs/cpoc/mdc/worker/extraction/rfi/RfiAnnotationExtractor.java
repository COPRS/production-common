package esa.s1pdgs.cpoc.mdc.worker.extraction.rfi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.config.RfiConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.RfiAnnotation;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.RfiDetectionFromNoiseReport;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.RfiMitigationPerformed;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class RfiAnnotationExtractor {

	private static final Logger LOG = LogManager.getLogger(RfiAnnotationExtractor.class);

	private final ProcessConfiguration processConfiguration;
	private final RfiConfiguration rfiConfiguration;
	private final ObsClient obsClient;
	private final XmlConverter xmlConverter;

	public RfiAnnotationExtractor(final ProcessConfiguration processConfiguration,
			final RfiConfiguration rfiConfiguration, final ObsClient obsClient, final XmlConverter xmlConverter) {
		this.processConfiguration = processConfiguration;
		this.rfiConfiguration = rfiConfiguration;
		this.obsClient = obsClient;
		this.xmlConverter = xmlConverter;
	}

	public void addRfiMetadata(final ReportingFactory reportingFactory, final String keyObjectStorage,
			final ProductFamily family, final String localDirectory, final JSONObject metadata)
			throws MetadataExtractionException {

		if (family == ProductFamily.L1_SLICE) {
			RfiMitigationPerformed rfiMitigationPerformed = RfiMitigationPerformed.NOT_SUPPORTED;
			int rfiNbPolarisationsDetected = 0;
			int rfiNbPolarisationsMitigated = 0;

			String swath = (String) metadata.get("swathtype");

			if (swath != null && !swath.startsWith("S")) {

				downloadAnnotationDirectory(reportingFactory, family, keyObjectStorage, localDirectory);

				Path annotationDirectory = Paths.get(localDirectory).resolve(keyObjectStorage)
						.resolve(rfiConfiguration.getAnnotationDirectoryName());
				Path rfiDirectory = annotationDirectory.resolve(rfiConfiguration.getRfiDirectoryName());

				if (Files.exists(rfiDirectory)) {

					Stream<Path> annotationFiles;
					Stream<Path> rfiFiles;
					try {
						annotationFiles = Files.list(annotationDirectory);
						rfiFiles = Files.list(rfiDirectory);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					if (rfiFiles.count() > 0) {

						Pattern patternAnnotation = Pattern.compile(rfiConfiguration.getAnnotationFilePattern(),
								Pattern.CASE_INSENSITIVE);

						try {
							rfiNbPolarisationsDetected = calculateRfiNbPolarisationsDetected(
									rfiFiles.map(p -> p.toFile()).collect(Collectors.toList()));

							rfiMitigationPerformed = getRfiMitigationPerformedFromAnnotationFile(annotationFiles
									.filter(p -> !Files.isDirectory(p))
									.filter(p -> patternAnnotation.matcher(p.getFileName().toString()).matches())
									.map(p -> p.toFile()).collect(Collectors.toList()));

						} finally {
							if (rfiFiles != null) {
								rfiFiles.close();
							}
							if (annotationFiles != null) {
								annotationFiles.close();
							}
							FileUtils.delete(annotationDirectory.toFile().getPath());
						}
					}
				}
			}

			if (RfiMitigationPerformed.ALWAYS == rfiMitigationPerformed
					|| RfiMitigationPerformed.BASED_ON_NOISE_MEAS == rfiMitigationPerformed) {
				rfiNbPolarisationsMitigated = rfiNbPolarisationsDetected;
			}

			metadata.put("rfiMitigationPerformed", rfiMitigationPerformed.stringRepresentation());
			metadata.put("rfiNbPolarisationsDetected", rfiNbPolarisationsDetected);
			metadata.put("rfiNbPolarisationsMitigated", rfiNbPolarisationsMitigated);
		}
	}

	void downloadAnnotationDirectory(final ReportingFactory reportingFactory, final ProductFamily family,
			final String keyObjectStorage, final String localDirectory) {

		try {

			if (obsClient.prefixExists(new ObsObject(family,
					Paths.get(keyObjectStorage).resolve(rfiConfiguration.getAnnotationDirectoryName())
							.resolve(rfiConfiguration.getRfiDirectoryName()).toString()))) {
				Retries.performWithRetries(
						() -> obsClient.download(Collections.singletonList(new ObsDownloadObject(family,
								Paths.get(keyObjectStorage).resolve(rfiConfiguration.getAnnotationDirectoryName())
										.toString(),
								localDirectory)), reportingFactory),
						"Download of Annotation directory of " + keyObjectStorage + " to " + localDirectory,
						processConfiguration.getNumObsDownloadRetries(),
						processConfiguration.getSleepBetweenObsRetriesMillis());
			}
		} catch (InterruptedException | SdkClientException e) {
			throw new RuntimeException(e);
		}
	}

	int calculateRfiNbPolarisationsDetected(List<File> rfiFiles) throws MetadataExtractionException {

		int rfiNbPolarisationsDetected = 0;

		int rfiNbPolH = 0;
		int rfiNbPolV = 0;

		Pattern patternFilePolH = Pattern.compile(rfiConfiguration.gethPolarisationRfiFilePattern(),
				Pattern.CASE_INSENSITIVE);
		Pattern patternFilePolV = Pattern.compile(rfiConfiguration.getvPolarisationRfiFilePattern(),
				Pattern.CASE_INSENSITIVE);

		for (File rfi : rfiFiles) {
			try {
				RfiAnnotation rfiAnnotation = (RfiAnnotation) xmlConverter
						.convertFromXMLToObject(rfi.getAbsolutePath());

				boolean hPol = patternFilePolH.matcher(rfi.getName()).matches();
				boolean vPol = patternFilePolV.matcher(rfi.getName()).matches();

				if (!hPol && !vPol) {
					LOG.debug("Ignoring file, is not RFI annotation: {}", rfi.getName());
					continue;
				}

				for (RfiDetectionFromNoiseReport r : rfiAnnotation.getDetectionFromNoiseReports()) {
					if (r.isRfiDetected()) {
						if (hPol) {
							rfiNbPolH++;
						} else if (vPol) {
							rfiNbPolV++;
						}
					}
				}

			} catch (IOException | JAXBException e) {
				LOG.error("Extraction of RFI annotation file metadata failed", e);
				throw new MetadataExtractionException(e);
			}
		}

		if (rfiNbPolH > 0) {
			rfiNbPolarisationsDetected++;
		}
		if (rfiNbPolV > 0) {
			rfiNbPolarisationsDetected++;
		}

		return rfiNbPolarisationsDetected;
	}

	RfiMitigationPerformed getRfiMitigationPerformedFromAnnotationFile(List<File> annotationFiles)
			throws MetadataExtractionException {
		if (annotationFiles.size() == 0) {
			return RfiMitigationPerformed.NOT_SUPPORTED;
		}

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			final Document document = dbf.newDocumentBuilder().parse(annotationFiles.get(0));
			final XPathFactory xpf = XPathFactory.newInstance();
			final XPath xpath = xpf.newXPath();

			String mitigationPerformed = ((Node) xpath
					.compile("//product/imageAnnotation/processingInformation/rfiMitigationPerformed/text()")
					.evaluate(document, XPathConstants.NODE)).getTextContent();

			return RfiMitigationPerformed.fromString(mitigationPerformed);
		} catch (Exception e) {
			LOG.error("Extraction of Annotation file metadata failed", e);
			throw new MetadataExtractionException(e);
		}
	}

}
