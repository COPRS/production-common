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
		final String nameWithoutTrailingSlash = ingestion.getKeyObjectStorage().endsWith("/") ?
				ingestion.getKeyObjectStorage().substring(0,ingestion.getKeyObjectStorage().length()-1) :
			    ingestion.getKeyObjectStorage();
		
		final int lastIndexOfSlash = nameWithoutTrailingSlash.lastIndexOf('/');
		
		// contains slash?
		if (lastIndexOfSlash != -1) {
			return nameWithoutTrailingSlash.substring(lastIndexOfSlash+1);
		}		
		return nameWithoutTrailingSlash;	
	}
}
