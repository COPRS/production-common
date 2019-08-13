package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public final class SftpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {				
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config) {		
			return new SftpOutboxClient(obsClient, config);
		}			
	}
	
	private static final Logger LOG = LogManager.getLogger(SftpOutboxClient.class);
	
	private static final int DEFAULT_PORT = 22;
	
	SftpOutboxClient(final ObsClient obsClient, final OutboxConfiguration config) {
		super(obsClient, config);
	}

	@Override
	public final void transfer(ProductFamily family, String keyObjectStorage) throws Exception {	
		final JSch client = new JSch();
		final Map<String, InputStream> elements = obsClient.getAllAsInputStream(family, keyObjectStorage);
		final int port = config.getPort() > 0 ? config.getPort() : DEFAULT_PORT;
		
		final Path remoteDir = Paths.get(config.getPath());
		
		if (config.getKeyFile() != null) {
			client.addIdentity(config.getKeyFile());
		}
	    final Session session = client.getSession(config.getUsername(), config.getHostname(), port);
	    if (config.getPassword() != null) {
	    	session.setPassword(config.getPassword());
	    }
	    session.setConfig("StrictHostKeyChecking", "no");
	    session.connect();
	    LOG.debug("Creating new ChannelSftp on {}", config.getHostname());
	    try {
	    	final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
		    channel.connect();
	    	try {		    		
	    		for (final Map.Entry<String, InputStream> entry : elements.entrySet()) {
	    			final String path = entry.getKey();		    		
	    			Utils.assertValidPath(path);
	    			
	    			final Path dest = remoteDir.resolve(path);	
	    			String currentPath = "";
	    			
	    			final Path parentPath = dest.getParent();    			
	    			if (parentPath == null) {
	    				throw new RuntimeException("Invalid destination " + dest);
	    			}    	
	    				    			
	    			// create parent directories if required
	    			for (final Path pathElement : parentPath) {
	    				currentPath = currentPath + "/" + pathElement;
	    	 			try {	    	 			
	    	 				LOG.debug("current path is {}", currentPath);
	    	 				channel.cd(currentPath);
						} catch (SftpException e) {
							// thrown, if directory does not exist
							LOG.info("Creating directory {}", currentPath);
							channel.mkdir(currentPath);
						}
	    			}		    			
	    			try (final InputStream in = entry.getValue()) {
	    				LOG.info("Uploading {} to {}", path, dest);
	    				channel.put(in, dest.toString());	    				
	    			}
	    		}
	    	}
	    	finally {
			    LOG.debug("Disconneting ChannelSftp on {}", config.getHostname());
	    		channel.disconnect();
	    	}
	    	
	    }
	    finally {
	    	LOG.debug("Disconneting Session on {}", config.getHostname());
	    	session.disconnect();
	    }	
	}
}