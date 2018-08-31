package esa.s1pdgs.cpoc.ingestor.files;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import esa.s1pdgs.cpoc.ingestor.files.model.filter.ExclusionRegexpPatternFileListFilter;

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
	private final int sessionCacheMaxCapacity;
	private final int sessionExecutorPoolSize;
	private final int sessionPeriod;
	private final int sessionMaxMessagesPerPoll;

	/**
	 * Local directory for reading the configuration files
	 */
	private final String configDir;
	private final int configCacheMaxCapacity;
	private final int configExecutorPoolSize;
	private final int configPeriod;
	private final int configMaxMessagesPerPoll;

	/**
	 * Pattern to exclusion temporary file (.writing here)
	 */
	private final static String PATTERN_EXCLUSION = "^\\..*"; // "^.*\\.writing$";

	/**
	 * Constructor
	 * 
	 * @param sessionDir
	 * @param configDir
	 */
	@Autowired
	public FileReaderConfig(@Value("${file.session-files.local-directory}") final String sessionDir,
			@Value("${file.session-files.cache-max-capacity}") final int sessionCacheMaxCapacity,
			@Value("${file.session-files.executor-pool-size}") final int sessionExecutorPoolSize,
			@Value("${file.session-files.poll-fixed-delay}") final int sessionPeriod,
			@Value("${file.session-files.max-msg-per-poll}") final int sessionMaxMessagesPerPoll,
			@Value("${file.auxiliary-files.local-directory}") final String configDir,
			@Value("${file.auxiliary-files.cache-max-capacity}") final int configCacheMaxCapacity,
			@Value("${file.auxiliary-files.executor-pool-size}") final int configExecutorPoolSize,
			@Value("${file.auxiliary-files.poll-fixed-delay}") final int configPeriod,
			@Value("${file.auxiliary-files.max-msg-per-poll}") final int configMaxMessagesPerPoll) {
		this.sessionDir = sessionDir;
		this.sessionCacheMaxCapacity = sessionCacheMaxCapacity;
		this.sessionExecutorPoolSize = sessionExecutorPoolSize;
		this.configDir = configDir;
		this.configCacheMaxCapacity = configCacheMaxCapacity;
		this.configExecutorPoolSize = configExecutorPoolSize;
		this.sessionPeriod = sessionPeriod;
		this.sessionMaxMessagesPerPoll = sessionMaxMessagesPerPoll;
		this.configPeriod = configPeriod;
		this.configMaxMessagesPerPoll = configMaxMessagesPerPoll;
	}

	/**
	 * Channel for ERDS session files
	 * 
	 * @return the sessionFileChannel
	 */
	@Bean(name = "sessionFileChannel")
	public MessageChannel sessionFileChannel() {
		return new DirectChannel();
	}

	/**
	 * Bean which recursively and periodically scan the local directory of ERDS
	 * session file and send the flow in the sessionFileChannel
	 * 
	 * @return a message per file/directory
	 */
	@Bean(name = "sessionFileReadingMessageSource")
	// @InboundChannelAdapter(value = "sessionFileChannel", poller =
	// @Poller(fixedRate = "${file.session-files.read-fixed-rate}"))
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
	public IntegrationFlow processSessionFlow(TaskExecutor sessionTaskExecutor,
			MessageSource<File> sessionFileReadingMessageSource) {
		return IntegrationFlows
				.from(sessionFileReadingMessageSource,
						c -> c.poller(Pollers.fixedDelay(this.sessionPeriod).taskExecutor(sessionTaskExecutor)
								.maxMessagesPerPoll(this.sessionMaxMessagesPerPoll)))
				.handle("sessionFilesProcessor", "processFile").get();
	}

	@Bean(name = "sessionTaskExecutor")
	TaskExecutor sessionTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(this.sessionExecutorPoolSize);
		return taskExecutor;
	}

	/**
	 * Channel for configuration files
	 * 
	 * @return the configFileChannel
	 */
	@Bean(name = "configFileChannel")
	public MessageChannel configFileChannel() {
		return new DirectChannel();
	}

	/**
	 * Bean which recursively and periodically scan the local directory of
	 * configuration file and send the flow in the configFileChannel
	 * 
	 * @return a message per file/directory
	 */
	@Bean(name = "configFileReadingMessageSource")
	// @InboundChannelAdapter(value = "configFileChannel", poller =
	// @Poller(fixedRate = "${file.auxiliary-files.read-fixed-rate}"))
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
	public IntegrationFlow processConfigFlow(TaskExecutor configTaskExecutor,
			MessageSource<File> configFileReadingMessageSource) {
		return IntegrationFlows
				.from(configFileReadingMessageSource,
						c -> c.poller(Pollers.fixedDelay(this.configPeriod).taskExecutor(configTaskExecutor)
								.maxMessagesPerPoll(this.configMaxMessagesPerPoll)))
				.handle("auxiliaryFilesProcessor", "processFile").get();
	}

	@Bean(name = "configTaskExecutor")
	TaskExecutor configTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(this.configExecutorPoolSize);
		return taskExecutor;
	}

}
