package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.StreamGobbler;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;

public class OQCTask implements Callable<OQCFlag> {
	private static final Logger LOGGER = LogManager.getLogger(OQCTask.class);

	private static final Consumer<String> DEFAULT_OUTPUT_CONSUMER = LOGGER::info;

	private File originalProduct;

	private Path binaryPath;
	private Path oqcBaseWorkingDirectory;
	private long timeOutInSeconds;

	private final Consumer<String> stdOutConsumer = DEFAULT_OUTPUT_CONSUMER;
	private final Consumer<String> stdErrConsumer = DEFAULT_OUTPUT_CONSUMER;

	public OQCTask(final ApplicationProperties properties, final File originalProduct) {
		this.originalProduct = originalProduct;

		this.binaryPath = Paths.get(properties.getOqcBinaryPath());
		this.oqcBaseWorkingDirectory = Paths.get(properties.getOqcWorkingDir());
		this.timeOutInSeconds = properties.getOqcTimeoutInSeconds();
	}

	@Override
	public OQCFlag call() {
		LOGGER.info("Performing OQC check for product: {}", originalProduct);		

		try {
//			if (!Files.exists(originalProduct)) {
//				LOGGER.error("Unable to find original product at {}", originalProduct);
//				throw new IllegalArgumentException("Unable to find original product at "+originalProduct);
//			}
			
			// Generate working directory
			final Path workDir = generateWorkingDirectory();

			// Generate Job order for OQC
			final Path jobOrder = generateJobOrder(workDir);

			// start OQC executable
			executeOQC(workDir, jobOrder);

			// evaluate results
			final OQCFlag oqcFlag = evaluateOQC(workDir);
			
			// cleanup
			FileUtils.delete(workDir.toString());
			
			LOGGER.debug("OQC validation was successful, resulting in {}", oqcFlag);
			return oqcFlag;
		} catch (final Exception e) {
			// Consider every failure as a non successful validation!
			LOGGER.error("An error occured during the oqc validation, resulting in NOT_CHECKED flag",e);
			return OQCFlag.NOT_CHECKED;
		}
	}

	private Path generateWorkingDirectory() {
		try {					
			final Path workDir = Files.createDirectories(
					Paths.get(oqcBaseWorkingDirectory.toString(), originalProduct.getName())
			); 
			Files.createDirectories(Paths.get(workDir.toString(), "reports"));
			LOGGER.debug("Generated working directory for oqc check: {}", workDir);
			return workDir;
		} catch (final IOException e) {
			LOGGER.error("Failed to generate oqc working directory: {}",LogUtils.toString(e));
			throw new IllegalArgumentException("Failed to generate oqc working directory:"+LogUtils.toString(e));
		}
	}


	Path generateJobOrder(final Path workDir) {
		final Path path = Paths.get(workDir.toString(), "JobOrder.000000000.xml");
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			final InputStream is = new ClassPathResource("/amalfi-template.xml").getInputStream();
			final Document document = dbf.newDocumentBuilder().parse(is);
			final XPathFactory xpf = XPathFactory.newInstance();
			final XPath xpath = xpf.newXPath();

			// replace content that is specific for the task
			((Node) xpath.compile("//Input/List_of_File_Names/File_Name/text()").evaluate(document,
					XPathConstants.NODE)).setTextContent(originalProduct.toString());
			
			final NodeList outputNodes = (NodeList)xpath.compile("//Output/File_Name/text()").evaluate(document, XPathConstants.NODESET);
			for (int c=0; c < outputNodes.getLength(); c++) {
				final Node node = outputNodes.item(c);
				node.setTextContent(workDir.toString() + "/reports");
			}			
//			((Node) xpath.compile("//Output/File_Name/text()").evaluate(document, XPathConstants.NODESET))
//					.setTextContent(workDir.toString() + "/reports");

			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer t = tf.newTransformer();
			t.transform(new DOMSource(document), new StreamResult(Files.newOutputStream(path)));

			LOGGER.info("Generated amalfi job order: {}", path);
		} catch (final Exception e) {
			LOGGER.error("Failed to generate oqc job order: {}",LogUtils.toString(e));
			throw new IllegalArgumentException("Failed to generate oqc job order:"+LogUtils.toString(e));
		}

		return path;
	}

	private void executeOQC(final Path workingDirectory, final Path jobOrder) {
		Process process = null;
		try {
			LOGGER.info("Executing OQC binary at {} for working directory {} using job order {}", binaryPath,
					workingDirectory.toAbsolutePath(), jobOrder.toAbsolutePath());

			final int r = -1;
			final ProcessBuilder builder = new ProcessBuilder();

			builder.command(binaryPath.toString(), jobOrder.toString());
			builder.directory(workingDirectory.toFile());
			process = builder.start();

			final Future<?> out = Executors.newSingleThreadExecutor()
					.submit(new StreamGobbler(process.getInputStream(), stdOutConsumer));
			final Future<?> err = Executors.newSingleThreadExecutor()
					.submit(new StreamGobbler(process.getErrorStream(), stdErrConsumer));
			process.waitFor(timeOutInSeconds,TimeUnit.SECONDS);
			
			if (process.isAlive()) {
				LOGGER.info("Process is still alive, enforcing termination");
				process.destroyForcibly();
				throw new TimeoutException("OQC process timed out");
			}

			// wait for STDOUT/STDERR to be consumed
			out.get();
			err.get();
		} catch (final Exception e) {
			LOGGER.error("Unable to execute oqc binary:{}", LogUtils.toString(e));
			throw new IllegalArgumentException("Unable to execute oqc binary due:"+e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private OQCFlag evaluateOQC(final Path workingDirectory) {
		final Path reportDir = Paths.get(workingDirectory.toString(), "reports");
		LOGGER.debug("Evaluating results from report directory {}", reportDir);

		// Identify the possible reports
		Path pdfReport = null;
		Path xmlReport = null;

		try {

			try (Stream<Path> walk = Files.walk(reportDir)) {
				final List<String> pdfReports = walk.map(x -> x.toString()).filter(f -> f.endsWith(".pdf"))
						.collect(Collectors.toList());

				LOGGER.info("Found {} PDF documents", pdfReports.size());
				if (pdfReports.size() != 1) {
					LOGGER.error("Found {} pdf report, but expected one", pdfReports.size());
					throw new IllegalArgumentException("Found not exactly one pdf report from oqc check");
				}
				pdfReport = Paths.get(pdfReports.get(0));
			}

			try (Stream<Path> walk = Files.walk(reportDir)) {
				final List<String> xmlReports = walk.map(x -> x.toString()).filter(f -> f.endsWith(".xml"))
						.collect(Collectors.toList());
				LOGGER.info("Found {} XML documents", xmlReports.size());
				if (xmlReports.size() != 1) {
					LOGGER.error("Found {} xml report, but expected one", xmlReports.size());
					throw new IllegalArgumentException("Found not exactly one xml report from oqc check");
				}

				xmlReport = Paths.get(xmlReports.get(0));
			}
		} catch (final IOException e) {
			LOGGER.error("Error while trying to identify oqc reports: {}", LogUtils.toString(e));
			throw new IllegalArgumentException("Error while trying to identify oqc reports:"+e.getMessage());
		}

		// Extract OQC from xml report
		OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final Document document = dbf.newDocumentBuilder().parse(Files.newInputStream(xmlReport));
			final XPathFactory xpf = XPathFactory.newInstance();
			final XPath xpath = xpf.newXPath();
			final XPathExpression expression = xpath
					.compile("/*[local-name()='report']/*[local-name()='inspection']/@status");

			final String reportStatus = (String) expression.evaluate(document, XPathConstants.STRING);
			if (reportStatus.equals("Passed")) {
				LOGGER.info("OQC check was succesfully");
				oqcFlag = OQCFlag.CHECKED_OK;
			} else {
				LOGGER.info("OQC check was NOT succesfully");
				oqcFlag = OQCFlag.CHECKED_NOK;
			}
			
			// Copy pdf report into original product
			final Path pdfName = pdfReport.getFileName(); 
			Assert.notNull(pdfName, "Could not determine name of pdf OQC report");
			final Path newPdf = Paths.get(originalProduct.toString(), pdfName.toString());
			LOGGER.info("Copying pdf report {} into original product {}", pdfReport, newPdf);
			Files.copy(pdfReport, newPdf, StandardCopyOption.REPLACE_EXISTING);
		} catch (final Exception e) {
			LOGGER.error("Error due validation of oqc reporting: {}", LogUtils.toString(e));
			throw new IllegalArgumentException("Error due validation of oqc reporting:"+e.getMessage());
		}

		// Return results
		return oqcFlag;
	}

}
