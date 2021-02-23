package esa.s1pdgs.cpoc.directorycleaner.client;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.ArrayUtil;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties;
import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties.Protocol;

public class TestMyOceanFtpDirectoryCleaner {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final int PORT = 9876;

	private static File rootDir;
	private static File userDir;
	private static FtpServer ftpServer;

	private File day20File;
	private File day21File;
	private File day22File;

	// --------------------------------------------------------------------------

	@BeforeClass
	public static void setupClass() throws Exception {
		System.out.println("setup (class level) ...");

		rootDir = Files.createTempDirectory("test_ftp_server_").toFile();
		rootDir.deleteOnExit();

		final FtpServerFactory ftpServerFactory = new FtpServerFactory();

		final ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
		configFactory.setMaxLogins(100);
		ftpServerFactory.setConnectionConfig(configFactory.createConnectionConfig());

		final ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setServerAddress("localhost");
		listenerFactory.setPort(PORT);
		ftpServerFactory.addListener("default", listenerFactory.createListener());

		final PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();
		userFactory.setAdminName("admin");
		userFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());

		final File userPropsFile = Files.createTempFile("user", ".properties").toFile();
		userPropsFile.deleteOnExit();
		userDir = new File(rootDir, "user");
		final BaseUser user = new BaseUser();
		user.setName(USER);
		user.setPassword(PASS);
		user.setHomeDirectory(userDir.getPath());
		user.setEnabled(true);
		System.out.println(" -> create user directory: " + userDir.getPath());
		userDir.mkdirs();

		final String prefix = "ftpserver.user." + user.getName();

		final String template = prefix + ".homedirectory=%s\n" + prefix + ".userpassword=%s\n" + prefix
				+ ".enableflag=true\n" + prefix + ".writepermission=true\n" + prefix + ".idletime=0\n" + prefix
				+ ".maxloginnumber=200\n" + prefix + ".maxloginperip=200\n" + prefix + ".uploadrate=0\n" + prefix
				+ ".downloadrate=0\n\n";

		final String configString = String.format(template, user.getHomeDirectory(), user.getPassword());

		System.out.println(" -> write content to file " + userPropsFile.getPath() + ": " + configString);
		FileUtils.writeFile(userPropsFile, configString);
		userFactory.setFile(userPropsFile);
		ftpServerFactory.setUserManager(userFactory.createUserManager());

		ftpServer = ftpServerFactory.createServer();
		System.out.println(" -> start ftp server ...");
		ftpServer.start();
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("tear down (class level) ...");

		System.out.println(" -> stop ftp server ...");
		ftpServer.stop();

		System.out.println(" -> delete root dir " + rootDir.getPath() + " ...");
		FileUtils.delete(rootDir.getPath());
	}

	@Before
	public final void setUp() throws IOException, InternalErrorException {
		System.out.println("setup (test level) ...");

		final File yearDir = new File(userDir, "2021");
		final File monthDir = new File(yearDir, "02");

		final File day20Dir = new File(monthDir, "20");
		day20Dir.mkdirs();
		this.day20File = new File(day20Dir, "file0");
		System.out.println(" -> create " + this.day20File);
		FileUtils.writeFile(this.day20File, "file0");

		final File day21Dir = new File(monthDir, "21");
		day21Dir.mkdirs();
		this.day21File = new File(day21Dir, "file1");
		System.out.println(" -> create " + this.day21File);
		FileUtils.writeFile(this.day21File, "file1");

		final File day22Dir = new File(monthDir, "22");
		day22Dir.mkdirs();
		this.day22File = new File(day22Dir, "file2");
		System.out.println(" -> create " + this.day22File);
		FileUtils.writeFile(this.day22File, "file2");
	}

	@After
	public final void tearDown() {
		System.out.println("tear down (test level) ...");
		for (final String path : ArrayUtil.nullToEmpty(userDir.list())) {
			final File file = new File(userDir, path);

			if (!userDir.equals(file)) {
				System.out.println(" -> delete " + file.getPath());
				FileUtils.delete(file.getPath());
			}
		}
	}

	// --------------------------------------------------------------------------

	@Test
	public final void test_exceedsRetentionTime() throws Exception {
		final DirectoryCleanerProperties config = new DirectoryCleanerProperties();
		config.setRetentionTimeInDays(7);
		System.out.println("using config: " + config);

		MyOceanFtpDirectoryCleaner cleaner = new MyOceanFtpDirectoryCleaner(config);
		final Calendar timestamp = Calendar.getInstance();
		boolean exceedsRetentionTime = cleaner.exceedsRetentionTime(timestamp);
		assertTrue("expected " + DATE_FORMAT.format(timestamp.getTime()) + " to _not_ exceed retention time of "
				+ config.getRetentionTimeInDays() + " days", !exceedsRetentionTime);

		config.setRetentionTimeInDays(0);
		System.out.println("using config: " + config);

		cleaner = new MyOceanFtpDirectoryCleaner(config);
		exceedsRetentionTime = cleaner.exceedsRetentionTime(timestamp);
		assertTrue("expected " + DATE_FORMAT.format(timestamp.getTime()) + " to exceed retention time of "
				+ config.getRetentionTimeInDays() + " days", exceedsRetentionTime);
	}

	@Test
	public final void testOutboxCleaner_nothingToDelete() throws Exception {
		this.checkFilesExist();

		final DirectoryCleanerProperties config = new DirectoryCleanerProperties();
		config.setProtocol(Protocol.FTP);
		config.setHostname("localhost");
		config.setPort(PORT);
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setPath(userDir.getPath());
		config.setImplicitSsl(false);
		config.setFtpPasv("false");
		config.setRetentionTimeInDays(7);
		System.out.println("using config: " + config);

		final MyOceanFtpDirectoryCleaner cleaner = new MyOceanFtpDirectoryCleaner(config);
		cleaner.cleanDirectories();

		this.checkFilesExist();
	}

	@Test
	public final void testOutboxCleaner_deleteAllFiles() throws Exception {
		this.checkFilesExist();

		final DirectoryCleanerProperties config = new DirectoryCleanerProperties();
		config.setProtocol(Protocol.FTP);
		config.setHostname("localhost");
		config.setPort(PORT);
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setImplicitSsl(false);
		config.setFtpPasv("false");
		config.setRetentionTimeInDays(0);
		System.out.println("using config: " + config);

		final MyOceanFtpDirectoryCleaner cleaner = new MyOceanFtpDirectoryCleaner(config);
		cleaner.cleanDirectories();

		this.checkFilesDontExist();
	}

	// --------------------------------------------------------------------------

	private final void checkFilesExist() {
		assertTrue("file " + ((null != this.day20File) ? this.day20File : "") + " must exist",
				null != this.day20File && this.day20File.exists());
		assertTrue("file " + ((null != this.day21File) ? this.day21File : "") + " must exist",
				null != this.day21File && this.day21File.exists());
		assertTrue("file " + ((null != this.day22File) ? this.day22File : "") + " must exist",
				null != this.day22File && this.day22File.exists());
	}

	private final void checkFilesDontExist() {
		assertTrue("file " + ((null != this.day20File) ? this.day20File : "") + " must not exist",
				null != this.day20File && !this.day20File.exists());
		assertTrue("file " + ((null != this.day21File) ? this.day21File : "") + " must not exist",
				null != this.day21File && !this.day21File.exists());
		assertTrue("file " + ((null != this.day22File) ? this.day22File : "") + " must not exist",
				null != this.day22File && !this.day22File.exists());
	}

}