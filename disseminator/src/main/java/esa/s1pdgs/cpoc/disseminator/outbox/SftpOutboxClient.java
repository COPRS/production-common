package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public final class SftpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {				
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config, final PathEvaluater eval) {		
			return new SftpOutboxClient(obsClient, config, eval);
		}			
	}
	
	private static final Logger LOG = LogManager.getLogger(SftpOutboxClient.class);
	
	private static final int DEFAULT_PORT = 22;
	
	SftpOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater eval) {
		super(obsClient, config, eval);
	}

	@Override
	public final void transfer(final ObsObject obsObject) throws Exception {	
		final JSch client = new JSch();
		final Map<String, InputStream> elements = obsClient.getAllAsInputStream(obsObject.getFamily(), obsObject.getKey());
		final int port = config.getPort() > 0 ? config.getPort() : DEFAULT_PORT;
		
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
	       			final Path dest = evaluatePathFor(new ObsObject(entry.getKey(), obsObject.getFamily()));	
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
	    				LOG.info("Uploading {} to {}", entry.getKey(), dest);
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