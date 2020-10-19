package esa.s1pdgs.cpoc.ebip.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

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
import org.springframework.util.StreamUtils;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.Streams;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.ApacheFtpEdipClient;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public class ITEdipClient {
	private static final String USER = "user";
	private static final String PASS = "pass";
	private static final int PORT = 4321;
	
	private static File rootDir;
	private static FtpServer ftpServer; 
	private static File userDir;
	
	private File testDir;
	private static File keystoreFile;

	
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
		
        // create a defensive copy of the keystore file
        keystoreFile = Files.createTempFile("tmp", ".keystore").toFile();
        
        try (final InputStream in = Streams.getInputStream("test.keystore");
        	 final OutputStream out = new BufferedOutputStream(new FileOutputStream(keystoreFile))) {
        	StreamUtils.copy(in, out);
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
		keystoreFile.delete();
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
		final URI uri = new URI("ftps://localhost:4321/" + rootDir.getPath());
		final ApacheFtpEdipClient uut =  new ApacheFtpEdipClient(newConfig(), uri);
		
		System.out.println(uut.list(EdipEntryFilter.ALLOW_ALL));

	}
	
	private final EdipHostConfiguration newConfig() {
		final EdipHostConfiguration result = new EdipHostConfiguration();
		result.setServerName("localhost");
		result.setPass(PASS);
		result.setUser(USER);		
		return result;
	}
}
