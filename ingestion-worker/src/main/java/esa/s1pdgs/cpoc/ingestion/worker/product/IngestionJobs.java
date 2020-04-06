package esa.s1pdgs.cpoc.ingestion.worker.product;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class IngestionJobs {
	public static final URI toUri(final IngestionJob ingestion) throws InternalErrorException {
		try {
			System.out.println(ingestion.getPickupBaseURL());
			System.out.println(ingestion.getRelativePath());
			
			System.out.println("OLD: " + new URIBuilder(ingestion.getPickupBaseURL() + ingestion.getRelativePath()).build(););
			System.out.println("new: " + new URIBuilder(ingestion.getPickupBaseURL() + ("/" + ingestion.getRelativePath()).replaceAll("//", "/")).build());
			System.out.println("NEW: " + new URIBuilder(ingestion.getPickupBaseURL() + ("/" + ingestion.getRelativePath()).replaceAll("//", "/")).build());
			System.out.println(ingestion.getPickupBaseURL());
			System.out.println(ingestion.getRelativePath());
			
			//return new URIBuilder(ingestion.getPickupBaseURL() + ("/" + ingestion.getRelativePath()).replaceFirst("//", "/")).build();
			return new URIBuilder(ingestion.getPickupBaseURL() + ingestion.getRelativePath()).build();
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
}
