package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.disseminator.util.LogPrintWriter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;

public class FtpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater eval) {
			return new FtpOutboxClient(obsClient, config, eval);
		}			
	}
	
	private static final int DEFAULT_PORT = 21;
	
	public FtpOutboxClient(ObsClient obsClient, OutboxConfiguration config, PathEvaluater pathEvaluator) {
		super(obsClient, config, pathEvaluator);
	}
	
	@Override
	public String transfer(final ObsObject obsObject, final Reporting.ChildFactory reportingChildFactory) throws Exception {
		final FTPClient ftpClient = new FTPClient();
		ftpClient.addProtocolCommandListener(
				new PrintCommandListener(new LogPrintWriter(s -> logger.debug(s)), true)
		);
		final int port = (config.getPort() > 0) ? config.getPort(): DEFAULT_PORT;
		ftpClient.connect(config.getHostname(), port);
	    assertPositiveCompletion(ftpClient);
        
        return performTransfer(obsObject, ftpClient, reportingChildFactory);
	}

	protected String performTransfer(final ObsObject obsObject, final FTPClient ftpClient, final Reporting.ChildFactory reportingChildFactory)
			throws IOException, SdkClientException {
		if (!ftpClient.login(config.getUsername(), config.getPassword())) {
        	throw new RuntimeException("Could not authenticate user " + config.getUsername());
        }
        
    	try {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        
	        if (config.isFtpPasv()) {
	        	logger.debug("Using passive");
		        ftpClient.enterLocalPassiveMode();
	        } else {
	        	logger.debug("Using active");
		        ftpClient.enterLocalActiveMode();
	        }
	        assertPositiveCompletion(ftpClient);        
	        
			final Path path = evaluatePathFor(obsObject);	
			final String retVal = config.getProtocol().toString().toLowerCase() + "://" + config.getHostname() + 
					path.toString();
					
			for (final Map.Entry<String, InputStream> entry : entries(obsObject, reportingChildFactory)) {
				
				final Path dest = path.resolve(entry.getKey());
    			
    			String currentPath = "";
    			
    			final Path parentPath = dest.getParent();    			
    			if (parentPath == null) {
    				throw new RuntimeException("Invalid destination " + dest);
    			}    				    			
    			// create parent directories if required
    			for (final Path pathElement : parentPath) {
    				currentPath = currentPath + "/" + pathElement;
    	 	    	 			
	 				logger.debug("current path is {}", currentPath);
	 				
	 				boolean directoryExists = ftpClient.changeWorkingDirectory(currentPath);
	 				if (directoryExists) {
	 					continue;
	 				}
	 				logger.debug("creating directory {}", currentPath);
	 				ftpClient.makeDirectory(currentPath);
	 				assertPositiveCompletion(ftpClient);	    	 
    			}		    
    			
    			try (final InputStream in = entry.getValue()) {
    				logger.info("Uploading {} to {}", entry.getKey(), dest);
    				ftpClient.storeFile(dest.toString(), in);
    				assertPositiveCompletion(ftpClient);	    				
    			}
    		}
			return retVal;
    	}
    	finally { 
    		try {
    			ftpClient.logout();
	            assertPositiveCompletion(ftpClient);
    		}
    		finally {
    			ftpClient.disconnect();
    			assertPositiveCompletion(ftpClient);
    		}
    	}
	}	
	
	static final void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}
}
