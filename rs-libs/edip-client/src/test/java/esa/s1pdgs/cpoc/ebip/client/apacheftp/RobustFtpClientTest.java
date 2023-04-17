package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.util.LogPrintWriter;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

@Ignore
public class RobustFtpClientTest {
	
	static final Logger LOG = LogManager.getLogger(RobustFtpClientTest.class);
	
    private FakeFtpServer fakeFtpServer;
    private RobustFtpClient robustClient;
    private FTPClient ftpClient;
    
    public RobustFtpClientTest() throws SocketException, IOException, URISyntaxException {
    	
    	int defaultTimeoutSec = 60;
		int threadTimeoutSec = 1;
		
		ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(LOG::debug), true));
		ftpClient.setDefaultTimeout(defaultTimeoutSec * 1000);
		ftpClient.setConnectTimeout(defaultTimeoutSec * 1000);
		
		
		EdipHostConfiguration config = new EdipHostConfiguration();
		config.setListingTimeoutSec(threadTimeoutSec);
		config.setServerName("localhost");
		config.setUser("user");
		config.setPass("pw");
		
		ftpClient.setDataTimeout(defaultTimeoutSec * 1000);
		
		robustClient = new RobustFtpClient(config, new URI("localhos:/"), false);
		
		fakeFtpServer = new FakeFtpServer();
		
		UserAccount userAccount = new UserAccount("user", "pw", "/");
		fakeFtpServer.addUserAccount(userAccount);
		
		FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new FileEntry("/test.txt", "contents"));
        fakeFtpServer.setFileSystem(fileSystem);
	}
    

	@After
	public void tearDown() throws Exception {
		fakeFtpServer.stop();
	}
	
	@Test
	public void listWithDelay() throws InterruptedException, ExecutionException, IOException {
		 
		
		MyListCommandHandler lch = new MyListCommandHandler(10);
		fakeFtpServer.setCommandHandler("LIST", lch);
		
		fakeFtpServer.start();

		List<EdipEntry> result = robustClient.list(EdipEntryFilter.ALLOW_ALL);

		Assert.assertEquals(0, result.size());
		
		 
	}

}
