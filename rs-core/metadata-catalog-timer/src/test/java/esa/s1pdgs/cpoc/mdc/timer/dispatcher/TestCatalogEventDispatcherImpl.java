package esa.s1pdgs.cpoc.mdc.timer.dispatcher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.assertj.core.util.Lists;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.mdc.timer.db.CatalogEventTimerEntry;
import esa.s1pdgs.cpoc.mdc.timer.db.CatalogEventTimerEntryRepository;
import esa.s1pdgs.cpoc.mdc.timer.publish.Publisher;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class TestCatalogEventDispatcherImpl {
	
	@Mock
	private MetadataClient metadataClient;
	
	@Mock
	private CatalogEventTimerEntryRepository repository;
	
	@Mock
	private Publisher publisher;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public final void run_oneResult() throws Exception  {
		CatalogEventDispatcher uut = new CatalogEventDispatcherImpl(metadataClient, repository, publisher, "AUX", ProductFamily.AUXILIARY_FILE, "S1A");
		
		SearchMetadata searchMetadata = new SearchMetadata();
		searchMetadata.setInsertionTime("2021-01-01T00:00:00.000Z");
		List<SearchMetadata> metadataList = new  ArrayList<>();
		metadataList.add(searchMetadata);
		
		doReturn(Collections.emptyList()).when(repository).findByProductTypeAndProductFamily(eq("AUX"),eq(ProductFamily.AUXILIARY_FILE));
		doReturn(metadataList).when(metadataClient).searchInterval(eq(ProductFamily.AUXILIARY_FILE), eq("AUX"), any(), any(), eq("S1A"));
		
		uut.run();
		
		verify(publisher, times(1)).publish(any());
	}
	
	@Test
	public final void run_twoResults() throws Exception  {
		CatalogEventDispatcher uut = new CatalogEventDispatcherImpl(metadataClient, repository, publisher, "AUX", ProductFamily.AUXILIARY_FILE, "S1A");
		
		SearchMetadata searchMetadata1 = new SearchMetadata();
		SearchMetadata searchMetadata2 = new SearchMetadata();
		searchMetadata2.setInsertionTime("2021-01-01T00:00:00.000Z");
		
		List<SearchMetadata> metadataList = new  ArrayList<>();
		metadataList.add(searchMetadata1);
		metadataList.add(searchMetadata2);
		
		CatalogEventTimerEntry timerEntry = new CatalogEventTimerEntry();
		timerEntry.setLastCheckDate(Instant.now().toDate());
		List<CatalogEventTimerEntry> timerEntryList = new ArrayList<>();
		timerEntryList.add(timerEntry);
		
		doReturn(timerEntryList).when(repository).findByProductTypeAndProductFamily(eq("AUX"),eq(ProductFamily.AUXILIARY_FILE));
		doReturn(metadataList).when(metadataClient).searchInterval(eq(ProductFamily.AUXILIARY_FILE), eq("AUX"), any(), any(), eq("S1A"));
		
		uut.run();
		
		verify(publisher, times(2)).publish(any());
	}
	
	
}
