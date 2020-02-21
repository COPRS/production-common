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
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class SftpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {				
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater eval) {		
			return new SftpOutboxClient(obsClient, config, eval);
		}			
	}
	
	private static final Logger LOG = LogManager.getLogger(SftpOutboxClient.class);
	
	private static final int DEFAULT_PORT = 22;
	
	SftpOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater eval) {
		super(obsClient, config, eval);
	}

	@Override
	public final String transfer(final ObsObject obsObject, final ReportingFactory reportingFactory) throws Exception {	
		final JSch client = new JSch();
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
				final Path path = evaluatePathFor(obsObject);	
				final String retVal = config.getProtocol().toString().toLowerCase() + "://" + config.getHostname() + 
						path.toString();
				
				for (final Map.Entry<String, InputStream> entry : entries(obsObject)) {					
					final Path dest = path.resolve(entry.getKey());
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
						} catch (final SftpException e) {
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
				return retVal;
	    	}
	    	finally {
			    LOG.debug("Disconnecting ChannelSftp on {}", config.getHostname());
	    		channel.disconnect();
	    	}
	    	
	    }
	    finally {
	    	LOG.debug("Disconnecting Session on {}", config.getHostname());
	    	session.disconnect();
	    }	
	}
}