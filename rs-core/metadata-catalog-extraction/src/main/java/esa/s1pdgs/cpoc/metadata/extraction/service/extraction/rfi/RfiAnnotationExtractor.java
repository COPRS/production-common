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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.rfi;

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

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.config.RfiConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.RfiAnnotation;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.RfiDetectionFromNoiseReport;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.RfiMitigationPerformed;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.message.output.RfiOutput;

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
			final ProductFamily family, final String localDirectory, final ProductMetadata metadata)
			throws MetadataExtractionException {

		if (family == ProductFamily.L1_SLICE) {

			final Reporting rfiReporting = reportingFactory.newReporting("RfiMitigation");
			rfiReporting
					.begin(new ReportingMessage("Start extraction of RFI metadata from product %s", keyObjectStorage));

			try {

				RfiMitigationPerformed rfiMitigationPerformed = RfiMitigationPerformed.NOT_SUPPORTED;
				int rfiNbPolarisationsDetected = 0;
				int rfiNbPolarisationsMitigated = 0;

				downloadAnnotationDirectory(reportingFactory, family, keyObjectStorage, localDirectory);

				Path annotationDirectory = Paths.get(localDirectory).resolve(keyObjectStorage)
						.resolve(rfiConfiguration.getAnnotationDirectoryName());
				Path rfiDirectory = annotationDirectory.resolve(rfiConfiguration.getRfiDirectoryName());

				try {
					long count = 0;
					try (Stream<Path> stream = Files.list(rfiDirectory)) {
						count = stream.count();	
					}
					
					if (Files.exists(rfiDirectory) && count > 0) {

						Pattern patternAnnotation = Pattern.compile(rfiConfiguration.getAnnotationFilePattern(),
								Pattern.CASE_INSENSITIVE);

						try (Stream<Path> stream = Files.list(rfiDirectory)) {
							rfiNbPolarisationsDetected = calculateRfiNbPolarisationsDetected(
									stream.map(p -> p.toFile())
									.collect(Collectors.toList()));
						}

						try (Stream<Path> stream = Files.list(annotationDirectory)) {
							rfiMitigationPerformed = getRfiMitigationPerformedFromAnnotationFile(
									stream.filter(p -> !Files.isDirectory(p))
									.filter(p -> patternAnnotation.matcher(p.getFileName().toString()).matches())
									.map(p -> p.toFile()).collect(Collectors.toList()));
						}
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					try {
						if (Files.exists(annotationDirectory)) {
							FileUtils.delete(annotationDirectory.toFile().getPath());
						}
					} catch (Exception e) {
						LOG.warn("Disk cleanup failed for directory ", annotationDirectory.toFile().getPath());
					}
				}

				if (RfiMitigationPerformed.ALWAYS == rfiMitigationPerformed
						|| RfiMitigationPerformed.BASED_ON_NOISE_MEAS == rfiMitigationPerformed) {
					rfiNbPolarisationsMitigated = rfiNbPolarisationsDetected;
				}

				String swath = (String) metadata.get("swathtype");
				if (swath != null && swath.startsWith("S")) {
					// special SM handling
					rfiNbPolarisationsDetected = 0;
				}

				metadata.put("rfiMitigationPerformed", rfiMitigationPerformed.stringRepresentation());
				metadata.put("rfiNbPolarisationsDetected", rfiNbPolarisationsDetected);
				metadata.put("rfiNbPolarisationsMitigated", rfiNbPolarisationsMitigated);

				RfiOutput rfiOutput = new RfiOutput();
				rfiOutput.setL1ProductName(keyObjectStorage);
				rfiOutput.setRfiMitigationPerformed(rfiMitigationPerformed.stringRepresentation());
				rfiOutput.setRfiNbPolarisationsDetected(rfiNbPolarisationsDetected);
				rfiOutput.setRfiNbPolarisationsMitigated(rfiNbPolarisationsMitigated);

				rfiReporting.end(rfiOutput,
						new ReportingMessage("End extraction of RFI metadata from product %s", keyObjectStorage));

			} catch (Exception e) {

				rfiReporting.error(new ReportingMessage("Error extraction of RFI metadata from product %s: %s",
						keyObjectStorage, LogUtils.toString(e)));

				throw new MetadataExtractionException(e);
			}
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
				
				if (rfiAnnotation == null || rfiAnnotation.getDetectionFromNoiseReports() == null) {
					LOG.debug("Ignoring file, can not extract RFI informaton: {}", rfi.getName());
					continue;
				}

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
		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		
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
