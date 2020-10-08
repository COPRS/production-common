package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

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

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class TestFtpOutboxClient {	
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final int PORT = 9876;
	
	private static File rootDir;
	private static FtpServer ftpServer; 
	private static File userDir;
	
	private File testDir;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		rootDir = Files.createTempDirectory("testSshServer").toFile();
		rootDir.deleteOnExit();		
		
		final FtpServerFactory fact = new FtpServerFactory();

		final ConnectionConfigFactory configFactory = new ConnectionConfigFactory();
		configFactory.setMaxLogins(100);
		fact.setConnectionConfig(configFactory.createConnectionConfig());
		
		final ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setServerAddress("localhost");
		listenerFactory.setPort(PORT);
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

        final String template = prefix + ".homedirectory=%s\n" +
				prefix + ".userpassword=%s\n" +
				prefix + ".enableflag=true\n" +
				prefix + ".writepermission=true\n" +
				prefix + ".idletime=0\n" +
				prefix + ".maxloginnumber=200\n" +
				prefix + ".maxloginperip=200\n" +
				prefix + ".uploadrate=0\n" +
				prefix + ".downloadrate=0\n\n";

        final String configString = String.format(template, user.getHomeDirectory(), user.getPassword());
        
        FileUtils.writeFile(userPropsFile, configString);
        userFactory.setFile(userPropsFile);
        fact.setUserManager(userFactory.createUserManager());
        
        ftpServer = fact.createServer();
        ftpServer.start();
	}
	
	@AfterClass
	public static void tearDownClass() {
		ftpServer.stop();
		FileUtils.delete(rootDir.getPath());		
	}
	
	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("foo").toFile();
	}
	
	@After
	public final void tearDown() {
		FileUtils.delete(testDir.getPath());
	}
	
	@Test
	public final void testFoo() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient() {
			@Override
			public List<String> list(final ProductFamily family, final String keyPrefix) {
				return Collections.singletonList("my/little/file");
			}

			@Override
			public InputStream getAsStream(ProductFamily family, String key) {
				return new ByteArrayInputStream("expected file content".getBytes());
			}
		};
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setPort(PORT);
		config.setTruststorePass("changeit");
				
		final File dir = new File(userDir, testDir.toPath().toString());
		
		final FtpOutboxClient uut = new FtpOutboxClient(fakeObsClient, config, PathEvaluater.NULL);		
		uut.transfer(new ObsObject(ProductFamily.BLANK, "my/little/file"), ReportingFactory.NULL);
		
		final File expectedFile = new File(dir, "my/little/file");
		assertTrue(expectedFile.exists());
		assertEquals("expected file content", FileUtils.readFile(expectedFile));
	}
}
