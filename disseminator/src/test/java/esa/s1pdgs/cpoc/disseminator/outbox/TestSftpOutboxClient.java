package esa.s1pdgs.cpoc.disseminator.outbox;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.disseminator.FakeObsClient;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;

public class TestSftpOutboxClient {
	static final class SimplePasswordAuthenticator implements PasswordAuthenticator {
		private final String user;
		private final String pass;

		public SimplePasswordAuthenticator(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}

		@Override
		public final boolean authenticate(final String username, final String password, final ServerSession session) {
			return user.equals(username) && pass.equals(password);
		}
	}
	
	private static final String USER = "user";
	private static final String PASS = "pass";
	
	private static File rootDir;
	private static SshServer sshd;
	
	private File testDir;
	
	@BeforeClass
	public static final void setupClass() throws Exception {
		rootDir = Files.createTempDirectory("testSshServer").toFile();
		rootDir.deleteOnExit();
		
		sshd = SshServer.setUpDefaultServer();
		sshd.setHost("localhost");
		sshd.setPort(1234);
		
		final File keyFile = Files.createTempFile("keys", ".keys").toFile();
		keyFile.delete();

		final SimpleGeneratorHostKeyProvider provider = new SimpleGeneratorHostKeyProvider();
		provider.setAlgorithm("DSA");
		provider.setKeySize(512);
		provider.setPath(keyFile.getPath());
		keyFile.deleteOnExit();		
		sshd.setKeyPairProvider(provider);
		
		sshd.setSubsystemFactories(Arrays.asList(new SftpSubsystem.Factory()));
		sshd.setCommandFactory(new ScpCommandFactory());
		sshd.setPasswordAuthenticator(new SimplePasswordAuthenticator(USER, PASS));
		sshd.setFileSystemFactory(new VirtualFileSystemFactory(rootDir.getPath()));
						
		sshd.start();
	}
	
	@AfterClass
	public static final void tearDownClass() throws Exception {
		sshd.stop();
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
	public final void testUpload_OnNonExistingDirectory_ShallCreateParentDirectoriesLazily() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient(
				Collections.singletonMap("my/little/file", new ByteArrayInputStream("expected file content".getBytes()))
		);
		
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setPort(1234);
		
		final File dir = new File(rootDir, testDir.toPath().toString());
		
		final SftpOutboxClient uut = new SftpOutboxClient(fakeObsClient, config);		
		uut.transfer(ProductFamily.BLANK, "my/little/file");
		
		final File expectedFile = new File(dir, "my/little/file");
		assertEquals(true, expectedFile.exists());
		
		assertEquals("expected file content", FileUtils.readFile(expectedFile));
	}
	
	@Test
	public final void testUpload_OnExistingDirectory_ShallTransferFile() throws Exception {
		final FakeObsClient fakeObsClient = new FakeObsClient(
				Collections.singletonMap("my/little/file", new ByteArrayInputStream("expected file content".getBytes()))
		);
		
		final OutboxConfiguration config = new OutboxConfiguration();
		config.setPath(testDir.getPath());
		config.setUsername(USER);
		config.setPassword(PASS);
		config.setPort(1234);
		
		final File dir = new File(rootDir, testDir.toPath().toString());
		dir.mkdirs();
		
		final SftpOutboxClient uut = new SftpOutboxClient(fakeObsClient, config);		
		uut.transfer(ProductFamily.BLANK, "my/little/file");
		
		final File expectedFile = new File(dir, "my/little/file");
		assertEquals(true, expectedFile.exists());
		
		assertEquals("expected file content", FileUtils.readFile(expectedFile));
	}

}
