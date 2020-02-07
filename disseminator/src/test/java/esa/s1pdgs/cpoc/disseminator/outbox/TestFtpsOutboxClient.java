package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class TestFtpsOutboxClient {	
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final int PORT = 4321;
	
	private static File rootDir;
	private static FtpServer ftpServer; 
	private static File userDir;
	
	private File testDir;
	private static File keystoreFile;

	
	@BeforeClass
	public static final void setupClass() throws Exception {
		rootDir = Files.createTempDirectory("testSshServer").toFile();
		rootDir.deleteOnExit();		
		
		final FtpServerFactory fact = new FtpServerFactory();

		final ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
		configFactory.setMaxLogins(100);
		fact.setConnectionConfig(configFactory.createConnectionConfig());
		
		final ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setServerAddress("localhost");
		listenerFactory.setPort(PORT);
		
        // create a defensive copy of the keystore file
        keystoreFile = Files.createTempFile("tmp", ".keystore").toFile();
        
        try (final InputStream in = Utils.getInputStream("test.keystore");
        	 final OutputStream out = new BufferedOutputStream(new FileOutputStream(keystoreFile))) {
        	IOUtils.copy(in, out);
        }        

        // chmod to 600
        keystoreFile.setReadable(false, false);
        keystoreFile.setReadable(true, true);
        keystoreFile.setWritable(false, false);
        keystoreFile.setWritable(true, true);
        keystoreFile.setExecutable(false, false);
        keystoreFile.deleteOnExit();

        final SslConfigurationFactory ssl = new SslConfigurationFactory();
        ssl.setKeystoreFile(keystoreFile);
        ssl.setKeystorePassword("changeit");
        ssl.setSslProtocol("TLS");

        final SslConfiguration sslConfig = ssl.createSslConfiguration();

        // set the SSL configuration for the listener
        listenerFactory.setSslConfiguration(sslConfig);		
        listenerFactory.setImplicitSsl(true);

        fact.addListener("default", listenerFactory.createListener());
		
        final PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();
        userFactory.setAdminName("admin");
        userFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        
        final File userPropsFile = Files.createTempFile("user", ".properties").toFile();
        userDir = new File(rootDir, "user");        
        final BaseUser user   = new BaseUser();
        user.setName(USER);
        user.setPassword(PASS);
        user.setHomeDirectory(userDir.getPath());
        user.setEnabled(true);
        userDir.mkdirs();
                
        final String prefix = "ftpserver.user." + user.getName();

        final String template = new StringBuilder(500)
          .append(prefix).append(".homedirectory=%s\n")
          .append(prefix).append(".userpassword=%s\n")
          .append(prefix).append(".enableflag=true\n")
          .append(prefix).append(".writepermission=true\n")
          .append(prefix).append(".idletime=0\n")
          .append(prefix).append(".maxloginnumber=200\n")
          .append(prefix).append(".maxloginperip=200\n")
          .append(prefix).append(".uploadrate=0\n")
          .append(prefix).append(".downloadrate=0\n\n")
          .toString();

        final String configString = String.format(template, user.getHomeDirectory(), user.getPassword());
        
        FileUtils.writeFile(userPropsFile, configString);
        userFactory.setFile(userPropsFile);
        fact.setUserManager(userFactory.createUserManager());
        
        ftpServer = fact.createServer();
        ftpServer.start();
	}
	
	@AfterClass
	public static final void tearDownClass() throws Exception {
		ftpServer.stop();
		keystoreFile.delete();
		FileUtils.delete(rootDir.getPath());		
	}
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("foo").toFile();
	}
	
	@After
	public final void tearDown() throws IOException {
		FileUtils.delete(testDir.getPath());
	}
	
	@Test
	public final void testFoo() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public Map<String, InputStream> getAllAsInputStream(final ProductFamily family, final String keyPrefix, final ReportingFactory reportingFactory) {
				return Collections.singletonMap("my/little/file", new ByteArrayInputStream("expected file content".getBytes()));
			}			
		};		
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setPort(PORT);
		config.setTruststoreFile(keystoreFile.getPath());
		config.setTruststorePass("changeit");
				
		final File dir = new File(userDir, testDir.toPath().toString());
		
		final FtpsOutboxClient uut = new FtpsOutboxClient(fakeObsClient, config, PathEvaluater.NULL);		
		uut.transfer(new ObsObject(ProductFamily.BLANK, "my/little/file"), ReportingFactory.NULL);
		
		final File expectedFile = new File(dir, "my/little/file");
		assertEquals(true, expectedFile.exists());
		
		assertEquals("expected file content", FileUtils.readFile(expectedFile));
	}
}
