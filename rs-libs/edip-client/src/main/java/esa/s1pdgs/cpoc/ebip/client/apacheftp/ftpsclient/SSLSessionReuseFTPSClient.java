/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ebip.client.apacheftp.ftpsclient;

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

    private final boolean ftpsSslSessionReuse; 
    
	public SSLSessionReuseFTPSClient(final String protocol, final boolean isImplicit,
			final boolean ftpsSslSessionReuse, final boolean useExtendedMasterSecret) {
		super(protocol, isImplicit);
		this.ftpsSslSessionReuse = ftpsSslSessionReuse;
		if (!useExtendedMasterSecret) {
			// In case of compatibility issues, an application may disable extended master secret extension support
			// See: https://www.oracle.com/java/technologies/javase/8u161-relnotes.html
			logger.trace("Disabling useExtendedMasterSecret");
			System.setProperty("jdk.tls.useExtendedMasterSecret", "false");
		}
	}
	
    @Override
    protected void _prepareDataSocket_(final Socket socket) throws IOException {
    	if (!ftpsSslSessionReuse) {
    		logger.trace("FTPS SSL Session Reuse is disabled");
    	} else {
    		// adapted from https://trac.cyberduck.io/browser/trunk/ftp/src/main/java/ch/cyberduck/core/ftp/FTPClient.java
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
	                    final Method method = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
	                    method.setAccessible(true);
	                    method.invoke(cache, String
	                            .format("%s:%s", socket.getInetAddress().getHostName(), String.valueOf(socket.getPort()))
	                            .toLowerCase(Locale.ROOT), session);
	                    method.invoke(cache, String
	                            .format("%s:%s", socket.getInetAddress().getHostAddress(), String.valueOf(socket.getPort()))
	                            .toLowerCase(Locale.ROOT), session);
	                    logger.trace("Configured data socket for SSL session reuse");
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
}