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

package de.werum.coprs.cadip.client.odata;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.commons.api.http.HttpHeader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

public final class CadipAuthenticationUtil {

	private static final Logger LOG = LogManager.getLogger(CadipAuthenticationUtil.class);

	public static final Header basicAuthHeaderFor(final CadipHostConfiguration hostConfig) {
		final String auth = hostConfig.getUser() + ":" + hostConfig.getPass();
		final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));

		return new BasicHeader(HttpHeader.AUTHORIZATION, "Basic " + new String(encodedAuth));
	}

	public static final Header oauthHeaderFor(final CadipHostConfiguration hostConfig) {
		final String accessToken = retrieveOauthAccessToken(hostConfig);

		switch (hostConfig.getBearerTokenType()) {
		case AUTHORIZATION:
			return new BasicHeader("Authorization", "Bearer " + accessToken);
		case OAUTH2_ACCESS_TOKEN:
			return new BasicHeader("OAUTH2-ACCESS-TOKEN", accessToken);
		}

		throw new IllegalArgumentException("Unknown bearer token type: " + hostConfig.getBearerTokenType());

	}

	public static final String retrieveOauthAccessToken(final CadipHostConfiguration hostConfig) {
		return retrieveOauthAccessToken(URI.create(hostConfig.getOauthAuthUrl()), hostConfig.getOauthClientId(),
				hostConfig.getOauthClientSecret(), hostConfig.getUser(), hostConfig.getPass(), hostConfig.getScope(),
				hostConfig.getAdditionalHeadersAuth());
	}

	public static final String retrieveOauthAccessToken(final URI oauthAuthUrl, final String oauthClientId,
			final String oauthClientSecret, final String oauthAuthServerUser, final String oauthAuthServerPass,
			final String scope, final Map<String, String> additionalHeadersAuth) {
		final CloseableHttpClient httpClient = newOauthAuthorizationClient();

		final List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("grant_type", "password"));
		data.add(new BasicNameValuePair("client_id", oauthClientId));
		data.add(new BasicNameValuePair("client_secret", oauthClientSecret));
		data.add(new BasicNameValuePair("username", oauthAuthServerUser));
		data.add(new BasicNameValuePair("password", oauthAuthServerPass));
		if (scope != null) {
			data.add(new BasicNameValuePair("scope", scope));
		}

		ObjectNode token = null;
		CloseableHttpResponse response = null;
		InputStream responseContent = null;
		try {
			final HttpPost post = new HttpPost(oauthAuthUrl);
			post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			
			// Additional Headers for authentication request
			for (Entry<String, String> entry : additionalHeadersAuth.entrySet()) {
				LOG.debug("Add header to authorization request: key: \"{}\", value: \"{}\"", entry.getKey(), entry.getValue());
				post.addHeader(entry.getKey(), entry.getValue());
			}
			
			response = httpClient.execute(post);

			if (null == response) {
				throw new CadipClientOauthException(
						"error retrieving oauth access token from " + oauthAuthUrl + ": no response");
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				String responseBodyString = "empty";
				final HttpEntity entity = response.getEntity();
				if (null != entity) {
					final InputStream responseBodyStream = entity.getContent();
					if (null != responseBodyStream) {
						responseBodyString = IOUtils.toString(responseBodyStream, StandardCharsets.UTF_8);
					}
				}

				throw new CadipClientOauthException(
						"error retrieving oauth access token from " + oauthAuthUrl + ": status code "
								+ response.getStatusLine().getStatusCode() + "\nbody: " + responseBodyString);
			}

			final HttpEntity responseEntity = response.getEntity();

			if (null == responseEntity) {
				throw new CadipClientOauthException(
						"error retrieving oauth access token from " + oauthAuthUrl + ": empty response / no entity");
			}

			responseContent = responseEntity.getContent();

			token = (ObjectNode) new ObjectMapper().readTree(responseContent);

		} catch (final CadipClientOauthException e) {
			throw e;
		} catch (final Exception e) {
			throw new CadipClientOauthException(
					"error retrieving oauth access token from " + oauthAuthUrl + ": " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(responseContent);
			IOUtils.closeQuietly(response);
		}

		if (null == token) {
			throw new CadipClientOauthException("error retrieving oauth access token from " + oauthAuthUrl
					+ ": no result from parsing response to json");
		}

		final JsonNode tokenNode = token.get("access_token");
		if (null == tokenNode) {
			throw new CadipClientOauthException(
					"error retrieving oauth access token from " + oauthAuthUrl + ": response contains no access_token");
		}

		final String accessToken = tokenNode.asText();
		if (StringUtil.isEmpty(accessToken)) {
			throw new CadipClientOauthException("error retrieving oauth access token from " + oauthAuthUrl
					+ ": response contains no value for access_token");
		}

		return accessToken;
	}

	// --------------------------------------------------------------------------

	private static final CloseableHttpClient newOauthAuthorizationClient() {
		try {
			final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			final SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(new TrustSelfSignedStrategy());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(),
					NoopHostnameVerifier.INSTANCE);
			clientBuilder.setSSLSocketFactory(sslsf);

			return clientBuilder.build();
		} catch (final Exception e) {
			// FIXME error handling
			throw new RuntimeException(e);
		}
	}

	// --------------------------------------------------------------------------

	private CadipAuthenticationUtil() {
	}

}
