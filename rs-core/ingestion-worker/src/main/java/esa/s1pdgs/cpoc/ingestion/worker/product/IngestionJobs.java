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

package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.utils.URIBuilder;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class IngestionJobs {
	public static URI toUri(final IngestionJob ingestion) {

		try {
			final URI baseUri = new URI(ingestion.getPickupBaseURL());			

			URIBuilder uriBuilder = new URIBuilder(baseUri);

			//TODO move this logic into adapter itself
			if(!"auxip".equals(ingestion.getInboxType())) {
				final Path resultPath = Paths.get(baseUri.getPath())
						.resolve(ingestion.getRelativePath());
				uriBuilder.setPath(resultPath.toAbsolutePath().toString());
			}

			return uriBuilder
					.build();
		} catch (final URISyntaxException e) {
			throw new RuntimeException(
					String.format(
							"URL syntax not correct for %s: %s", 
							ingestion.getProductName(),
							ingestion
					), 
					e
			);
		}
	}
	
	/**
	 * 'substracts' {@code name} from the path given in {@code uri} 
	 */
	public static Path basePath(final URI uri, final String name) {	
		return Paths.get(uri.getPath().substring(0, uri.getPath().indexOf(name)));
	}
	
	public static String filename(final IngestionJob ingestion) {
		final String nameWithoutTrailingSlash;
		if ("auxip".equalsIgnoreCase(ingestion.getInboxType())) {
			nameWithoutTrailingSlash = ingestion.getRelativePath();
		} else if ("cadip".equalsIgnoreCase(ingestion.getInboxType())) {
			String relativePath = ingestion.getRelativePath();
			nameWithoutTrailingSlash = relativePath.substring(relativePath.indexOf("/") + 1);
		} else {
			nameWithoutTrailingSlash = ingestion.getKeyObjectStorage().endsWith("/") ?
					ingestion.getKeyObjectStorage().substring(0,ingestion.getKeyObjectStorage().length()-1) :
					    ingestion.getKeyObjectStorage();
		}
		
		final int lastIndexOfSlash = nameWithoutTrailingSlash.lastIndexOf('/');
		
		// contains slash?
		if (lastIndexOfSlash != -1) {
			return nameWithoutTrailingSlash.substring(lastIndexOfSlash+1);
		}		
		return nameWithoutTrailingSlash;	
	}
}
