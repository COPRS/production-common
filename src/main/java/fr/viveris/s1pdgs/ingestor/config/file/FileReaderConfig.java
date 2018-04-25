package fr.viveris.s1pdgs.ingestor.config.file;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.messaging.MessageChannel;

import fr.viveris.s1pdgs.ingestor.model.filter.ExclusionRegexpPatternFileListFilter;

/**
 * Configuration class which defined the flows to recursively read the ERDS
 * session files and configuration files on local system
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableIntegration
public class FileReaderConfig {

	/**
	 * Local directory for reading the ERDS session files
	 */
	private final String sessionDir;
	
	/**
	 * Maximal number of session file name cached
	 */
	private final int sessionCacheMaxCapacity;

	/**
	 * Local directory for reading the configuration files
	 */
	private final String configDir;
	
	/**
	 * Maximal number of configuration file name cached
	 */
	private final int configCacheMaxCapacity;

	/**
	 * Pattern to exclusion temporary file (.writing here)
	 */
	private final static String PATTERN_EXCLUSION = "^\\..*"; //"^.*\\.writing$";

	/**
	 * Constructor
	 * @param sessionDir
	 * @param configDir
	 */
	@Autowired
	public FileReaderConfig(@Value("${file.session-files.local-directory}") final String sessionDir,
			@Value("${file.session-files.cache-max-capacity}") final int sessionCacheMaxCapacity,
			@Value("${file.auxiliary-files.local-directory}") final String configDir,
			@Value("${file.auxiliary-files.cache-max-capacity}") final int configCacheMaxCapacity) {
		this.sessionDir = sessionDir;
		this.sessionCacheMaxCapacity = sessionCacheMaxCapacity;
		this.configDir = configDir;
		this.configCacheMaxCapacity = configCacheMaxCapacity;
	}

	/**
	 * Channel for ERDS session files
	 * 
	 * @return the sessionFileChannel
	 */
	@Bean
	public MessageChannel sessionFileChannel() {
		return new DirectChannel();
	}

	/**
	 * Bean which recursively and periodically scan the local directory of ERDS
	 * session file and send the flow in the sessionFileChannel
	 * 
	 * @return a message per file/directory
	 */
	@Bean
	@InboundChannelAdapter(value = "sessionFileChannel", poller = @Poller(fixedRate = "${file.session-files.read-fixed-rate}"))
	public MessageSource<File> sessionFileReadingMessageSource() {
		ChainFileListFilter<File> filter = new ChainFileListFilter<File>();
		filter.addFilter(
				new ExclusionRegexpPatternFileListFilter(Pattern.compile(PATTERN_EXCLUSION, Pattern.CASE_INSENSITIVE)));
		filter.addFilter(new AcceptOnceFileListFilter<>(this.sessionCacheMaxCapacity));
		RecursiveDirectoryScanner scanner = new RecursiveDirectoryScanner();
		scanner.setFilter(filter);

		FileReadingMessageSource sourceReader = new FileReadingMessageSource();
		sourceReader.setDirectory(new File(sessionDir));
		sourceReader.setAutoCreateDirectory(true);
		sourceReader.setScanner(scanner);
		return sourceReader;
	}

	/**
	 * Flow which intercepts message on the sessionFileChannel and handle them to
	 * our file processor
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow processSessionFlow() {
		return IntegrationFlows.from("sessionFileChannel").handle("fileProcessor", "processSessionFile").get();
	}

	/**
	 * Channel for configuration files
	 * 
	 * @return the configFileChannel
	 */
	@Bean
	public MessageChannel configFileChannel() {
		return new DirectChannel();
	}

	/**
	 * Bean which recursively and periodically scan the local directory of
	 * configuration file and send the flow in the configFileChannel
	 * 
	 * @return a message per file/directory
	 */
	@Bean
	@InboundChannelAdapter(value = "configFileChannel", poller = @Poller(fixedRate = "${file.auxiliary-files.read-fixed-rate}"))
	public MessageSource<File> configFileReadingMessageSource() {
		ChainFileListFilter<File> filter = new ChainFileListFilter<File>();
		filter.addFilter(
				new ExclusionRegexpPatternFileListFilter(Pattern.compile(PATTERN_EXCLUSION, Pattern.CASE_INSENSITIVE)));
		filter.addFilter(new AcceptOnceFileListFilter<>(this.configCacheMaxCapacity));
		
		RecursiveDirectoryScanner scanner = new RecursiveDirectoryScanner();
		scanner.setFilter(filter);

		FileReadingMessageSource sourceReader = new FileReadingMessageSource();
		sourceReader.setDirectory(new File(configDir));
		sourceReader.setAutoCreateDirectory(true);
		sourceReader.setScanner(scanner);
		return sourceReader;
	}

	/**
	 * Flow which intercepts message on the configFileChannel and handle them to our
	 * file processor
	 * 
	 * @return
	 */
	@Bean
	public IntegrationFlow processConfigFlow() {
		return IntegrationFlows.from("configFileChannel").handle("fileProcessor", "processConfigFile").get();
	}

}
