package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.utils.URIBuilder;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class IngestionJobs {
	public static final URI toUri(final IngestionJob ingestion) throws InternalErrorException {
		try {
			final URI baseUri = new URI(ingestion.getPickupBaseURL());			
			final Path resultPath = Paths.get(baseUri.getPath())
					.resolve(ingestion.getRelativePath());			
			
			return new URIBuilder(baseUri)
					.setPath(resultPath.toAbsolutePath().toString())
					.build();
		} catch (final URISyntaxException e) {
			throw new InternalErrorException(
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
		return Paths.get(uri.getPath().toString().substring(0, uri.getPath().toString().indexOf(name)));
	}
	
	public static final String filename(final IngestionJob ingestion) {		
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
