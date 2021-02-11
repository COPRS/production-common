package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class SftpOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {				
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
			final JSch client = new JSch();

			if (StringUtil.isNotEmpty(config.getKeyData())) {
				try {
					final Path private_key = Files.createTempFile("", "private_key");
					FileUtils.writeFile(private_key.toFile(), new String(Base64.getDecoder().decode(config.getKeyData())));
					client.addIdentity(private_key.toString());
				} catch (IOException | InternalErrorException | JSchException e) {
					throw new RuntimeException(e);
				}
			}
			return new SftpOutboxClient(obsClient, client, config, eval, config.getPermissions());
		}
	}
	
	private static final Logger LOG = LogManager.getLogger(SftpOutboxClient.class);
	
	private static final int DEFAULT_PORT = 22;

	private final JSch client;
	
	private final String permissions;

	SftpOutboxClient(
			final ObsClient obsClient, 
			final JSch sshClient, 
			final OutboxConfiguration config, 
			final PathEvaluator eval,
			final String permissions
	) {
		super(obsClient, config, eval);
		this.client = sshClient;
		this.permissions = permissions;
	}

	@Override
	public final String transfer(final ObsObject obsObject, final ReportingFactory reportingFactory) throws Exception {	

		final int port = config.getPort() > 0 ? config.getPort() : DEFAULT_PORT;

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
				
				for (final String entry : entries(obsObject)) {
					final Path dest = path.resolve(entry);
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
			    			LOG.info("Chmod {} dir {}", permissions, currentPath.toString());
							channel.chmod(Integer.parseInt(permissions, 8), currentPath.toString());
						}
	    			}		    			
	    			try (final InputStream in = stream(obsObject.getFamily(), entry)) {
	    				LOG.info("Uploading {} to {}", entry, dest);
	    				channel.put(in, dest.toString());
	        			LOG.info("Chmod {} file {}", permissions, dest.toString());
	    				channel.chmod(Integer.parseInt(permissions, 8), dest.toString());
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