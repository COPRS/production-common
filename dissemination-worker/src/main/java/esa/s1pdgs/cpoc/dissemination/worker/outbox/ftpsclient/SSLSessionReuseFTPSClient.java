package esa.s1pdgs.cpoc.dissemination.worker.outbox.ftpsclient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Locale;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLSessionReuseFTPSClient extends FTPSClient {

	protected final Logger logger = LoggerFactory.getLogger(SSLSessionReuseFTPSClient.class);

	public SSLSessionReuseFTPSClient(String protocol, boolean isImplicit) {
		super(protocol, isImplicit);
	}
	
    // adapted from:
    // https://trac.cyberduck.io/browser/trunk/ftp/src/main/java/ch/cyberduck/core/ftp/FTPClient.java
    @Override
    protected void _prepareDataSocket_(final Socket socket) throws IOException {
    	logger.trace("Preparing data socket" + (socket instanceof SSLSocket ? " for SSL" : " without SSL"));
    	if (socket instanceof SSLSocket) {
            final SSLSession session = ((SSLSocket) _socket_).getSession();
            if (session.isValid()) {
            	logger.trace("Found SSL session");
                final SSLSessionContext context = session.getSessionContext();
                try {
                    final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
                    sessionHostPortCache.setAccessible(true);
                    final Object cache = sessionHostPortCache.get(context);
                    final Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                    putMethod.setAccessible(true);
                    final Method getHostMethod = socket.getClass().getMethod("getPeerHost");
                    getHostMethod.setAccessible(true);
                    Object peerHost = getHostMethod.invoke(socket);
                    putMethod.invoke(cache, String.format("%s:%s", peerHost, socket.getPort()).toLowerCase(Locale.ROOT), session);
                    logger.info("Configured data socket for SSL session reuse");
                } catch (NoSuchFieldException e) {
                	logger.error("No field sessionHostPortCache in SSLSessionContext", e);
                	throw new IOException(e);
                } catch (Exception e) {
                	throw new IOException(e);
                }
            } else {
            	logger.warn(String.format("SSL session %s for socket %s is not rejoinable", session, socket));
            }
        }
    }
}