package fr.viveris.s1pdgs.jobgenerator.service.metadata;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.EdrsSessionMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0AcnMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;

public class MetadataServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private RestTemplate restTemplate;

	private final static String METADATA_HOST = "localhost:8082";

	private MetadataService service;
	
	private int nbRetry = 3; 
	private int tempoRetry = 3; 

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void init() throws Exception {
		// Mcokito
		MockitoAnnotations.initMocks(this);

		service = new MetadataService(restTemplate, METADATA_HOST, nbRetry, tempoRetry);
	}

	// --------------------------------------------------
	// Test around getEdrsSession
	// --------------------------------------------------

	@Test
	public void testHostnameQueryGetEdrsSession() throws JobGenMetadataException {
		EdrsSessionMetadata expectedFile = new EdrsSessionMetadata("DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
				"RAW", "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
				"2017-12-01T22:15:30", "2017-12-02T22:15:30");
		ResponseEntity<EdrsSessionMetadata> r = new ResponseEntity<EdrsSessionMetadata>(expectedFile, HttpStatus.OK);
		String uri = "http://" + METADATA_HOST + "/edrsSession/RAW/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw";
		when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), eq(null), eq(EdrsSessionMetadata.class))).thenReturn(r);

		this.service.getEdrsSession("RAW", "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw");

		verify(this.restTemplate, times(1)).exchange(eq(uri), eq(HttpMethod.GET), eq(null),
				eq(EdrsSessionMetadata.class));
	}

	@Test
	public void testGetEdrsSessionOk() throws JobGenMetadataException {
		EdrsSessionMetadata expectedFile = new EdrsSessionMetadata("DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
				"RAW", "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
				"2017-12-01T22:15:30", "2017-12-02T22:15:30");
		ResponseEntity<EdrsSessionMetadata> r = new ResponseEntity<EdrsSessionMetadata>(expectedFile, HttpStatus.OK);
		when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.GET), eq(null), eq(EdrsSessionMetadata.class)))
				.thenReturn(r);

		EdrsSessionMetadata file = this.service.getEdrsSession("RAW",
				"DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw");
		assertEquals("RAW", file.getProductType());
		assertEquals("DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw", file.getProductName());
		assertEquals("S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
				file.getKeyObjectStorage());
		assertEquals("2017-12-01T22:15:30", file.getValidityStart());
		assertEquals("2017-12-02T22:15:30", file.getValidityStop());
	}

	@Test
	public void testGetEdrsSessionKo() throws JobGenMetadataException {
		ResponseEntity<EdrsSessionMetadata> r = new ResponseEntity<EdrsSessionMetadata>(
				HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.GET), eq(null), eq(EdrsSessionMetadata.class)))
				.thenReturn(r);

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("nvalid HTTP statu");
		this.service.getEdrsSession("RAW", "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw");

	}

	@Test
	public void testGetEdrsSessionRestKO() throws JobGenMetadataException {
		doThrow(new RestClientException("rest exception")).when(restTemplate).exchange(Mockito.anyString(),
				eq(HttpMethod.GET), eq(null), eq(EdrsSessionMetadata.class));

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("rest exception");
		thrown.expectCause(isA(RestClientException.class));
		this.service.getEdrsSession("RAW", "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw");

	}

	// --------------------------------------------------
	// Test around getSlice
	// --------------------------------------------------

	@Test
	public void testHostnameQueryGetSlice() throws JobGenMetadataException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0SliceMetadata expectedResult = new L0SliceMetadata(
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "IW_RAW__0S",
				"S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE", "20171213T121623",
				"20171213T121656", 6, 2, "021735");
		ResponseEntity<L0SliceMetadata> r = new ResponseEntity<L0SliceMetadata>(expectedResult, HttpStatus.OK);
		String uri = "http://" + METADATA_HOST + "/l0Slice/IW_RAW__0S/" + file;
		when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), eq(null), eq(L0SliceMetadata.class))).thenReturn(r);

		this.service.getSlice("IW_RAW__0S", file);

		verify(this.restTemplate, times(1)).exchange(eq(uri), eq(HttpMethod.GET), eq(null), eq(L0SliceMetadata.class));
	}

	@Test
	public void testGetSliceOk() throws JobGenMetadataException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0SliceMetadata expectedResult = new L0SliceMetadata(file, "IW_RAW__0S", file, "2017-12-13T12:16:23",
				"2017-12-13T12:16:56", 6, 2, "021735");
		ResponseEntity<L0SliceMetadata> r = new ResponseEntity<L0SliceMetadata>(expectedResult, HttpStatus.OK);
		when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.GET), eq(null), eq(L0SliceMetadata.class)))
				.thenReturn(r);

		L0SliceMetadata f = this.service.getSlice("IW_RAW__0S", file);

		assertEquals("IW_RAW__0S", f.getProductType());
		assertEquals(file, f.getProductName());
		assertEquals(file, f.getKeyObjectStorage());
		assertEquals("2017-12-13T12:16:23", f.getValidityStart());
		assertEquals("2017-12-13T12:16:56", f.getValidityStop());
		assertEquals(6, f.getInstrumentConfigurationId());
		assertEquals(2, f.getNumberSlice());
		assertEquals("021735", f.getDatatakeId());
	}

	@Test
	public void testGetSliceKo() throws JobGenMetadataException {
		ResponseEntity<L0SliceMetadata> r = new ResponseEntity<L0SliceMetadata>(HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(Mockito.anyString(), eq(HttpMethod.GET), eq(null), eq(L0SliceMetadata.class)))
				.thenReturn(r);

		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("nvalid HTTP statu");
		this.service.getSlice("IW_RAW__0S", file);
	}

	@Test
	public void testGetSliceRestKo() throws JobGenMetadataException {
		doThrow(new RestClientException("rest exception")).when(restTemplate).exchange(Mockito.anyString(),
				eq(HttpMethod.GET), eq(null), eq(L0SliceMetadata.class));

		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("rest exception");
		thrown.expectCause(isA(RestClientException.class));
		this.service.getSlice("IW_RAW__0S", file);
	}

	// --------------------------------------------------
	// Test around acn
	// --------------------------------------------------

	@Test
	public void testHostnameQueryGetFirstAcn() throws JobGenMetadataException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0AcnMetadata[] expectedResult = {
				new L0AcnMetadata("S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"IW_RAW__0S", "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE",
						"20171213T121623", "20171213T121656", 6, 2, "021735") };
		ResponseEntity<L0AcnMetadata[]> r = new ResponseEntity<L0AcnMetadata[]>(expectedResult, HttpStatus.OK);
		String uri = "http://" + METADATA_HOST + "/l0Slice/IW_RAW__0S/" + file + "/acns";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE");
		when(restTemplate.exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(L0AcnMetadata[].class))).thenReturn(r);

		this.service.getFirstACN("IW_RAW__0S", file);

		verify(this.restTemplate, times(1)).exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(L0AcnMetadata[].class));
	}

	@Test
	public void testGetFirstAcnOk() throws JobGenMetadataException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		String fileA = "S1A_IW_RAW__0ADV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		String fileN = "S1A_IW_RAW__0CDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		L0AcnMetadata[] expectedResult = {
				new L0AcnMetadata(fileA, "IW_RAW__0A", fileA, "2017-12-13T12:16:23", "2017-12-13T12:16:56", 6, 2,
						"021735"),
				new L0AcnMetadata(fileN, "IW_RAW__0C", fileN, "2017-12-13T12:16:23", "2017-12-13T12:16:56", 6, 2,
						"021735") };
		String uri = "http://" + METADATA_HOST + "/l0Slice/IW_RAW__0S/" + file + "/acns";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE");
		ResponseEntity<L0AcnMetadata[]> r = new ResponseEntity<L0AcnMetadata[]>(expectedResult, HttpStatus.OK);
		when(restTemplate.exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(L0AcnMetadata[].class))).thenReturn(r);

		L0AcnMetadata f = this.service.getFirstACN("IW_RAW__0S", file);

		assertEquals("IW_RAW__0A", f.getProductType());
		assertEquals(fileA, f.getProductName());
		assertEquals(fileA, f.getKeyObjectStorage());
		assertEquals("2017-12-13T12:16:23", f.getValidityStart());
		assertEquals("2017-12-13T12:16:56", f.getValidityStop());
		assertEquals(6, f.getInstrumentConfigurationId());
		assertEquals(2, f.getNumberOfSlices());
		assertEquals("021735", f.getDatatakeId());

	}

	@Test
	public void testGetFirstAcnKo() throws JobGenMetadataException {
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";
		ResponseEntity<L0AcnMetadata[]> r = new ResponseEntity<L0AcnMetadata[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		String uri = "http://" + METADATA_HOST + "/l0Slice/IW_RAW__0S/" + file + "/acns";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE");
		when(restTemplate.exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(L0AcnMetadata[].class))).thenReturn(r);

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("nvalid HTTP statu");
		this.service.getFirstACN("IW_RAW__0S", file);
	}

	@Test
	public void testGetFirstAcnRestKo() throws JobGenMetadataException {
		doThrow(new RestClientException("rest exception")).when(restTemplate).exchange(Mockito.any(),
				eq(HttpMethod.GET), eq(null), eq(L0AcnMetadata[].class));
		String file = "S1A_IW_RAW__0SDV_20171213T121623_20171213T121656_019684_021735_C6DB.SAFE";

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("rest exception");
		thrown.expectCause(isA(RestClientException.class));
		this.service.getFirstACN("IW_RAW__0S", file);
	}

	// --------------------------------------------------
	// Test around search
	// --------------------------------------------------

	@Test
	public void testHostnameSearch() throws RestClientException, JobGenMetadataException, ParseException {
		DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		SearchMetadata expectedFile = new SearchMetadata("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
				"MPL_ORBPRE", "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF", "2017-12-05T20:03:09",
				"2017-12-15T20:03:09");
		ResponseEntity<SearchMetadata> r = new ResponseEntity<SearchMetadata>(expectedFile, HttpStatus.OK);
		when(restTemplate.exchange(Mockito.any(), eq(HttpMethod.GET), eq(null), eq(SearchMetadata.class)))
				.thenReturn(r);

		this.service.search(new SearchMetadataQuery(1, "LatestValCover", 1, 2, "AUX_OBMEMC"),
				format.parse("20171120_221516"), format.parse("20171220_101516"), "A", -1);

		String uri = "http://" + METADATA_HOST + "/metadata/search";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productType", "AUX_OBMEMC")
				.queryParam("mode", "LatestValCover").queryParam("t0", "2017-11-20T22:15:16")
				.queryParam("t1", "2017-12-20T10:15:16").queryParam("dt0", "1.0").queryParam("dt1", "2.0")
				.queryParam("satellite", "A");
		verify(this.restTemplate, times(1)).exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(SearchMetadata.class));
	}

	@Test
	public void testHostnameSearchWithInsConfDir() throws JobGenMetadataException, ParseException {
		DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		SearchMetadata expectedFile = new SearchMetadata("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
				"MPL_ORBPRE", "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF", "2017-12-05T20:03:09",
				"2017-12-15T20:03:09");
		ResponseEntity<SearchMetadata> r = new ResponseEntity<SearchMetadata>(expectedFile, HttpStatus.OK);
		when(restTemplate.exchange(Mockito.any(), eq(HttpMethod.GET), eq(null), eq(SearchMetadata.class)))
				.thenReturn(r);

		this.service.search(new SearchMetadataQuery(1, "LatestValCover", 1, 2, "AUX_OBMEMC"),
				format.parse("20171120_221516"), format.parse("20171220_101516"), "A", 6);

		String uri = "http://" + METADATA_HOST + "/metadata/search";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productType", "AUX_OBMEMC")
				.queryParam("mode", "LatestValCover").queryParam("t0", "2017-11-20T22:15:16")
				.queryParam("t1", "2017-12-20T10:15:16").queryParam("dt0", "1.0").queryParam("dt1", "2.0")
				.queryParam("satellite", "A").queryParam("insConfId", 6);
		verify(this.restTemplate, times(1)).exchange(eq(builder.build().toUri()), eq(HttpMethod.GET), eq(null),
				eq(SearchMetadata.class));
	}

	@Test
	public void testSearchOk() throws JobGenMetadataException {
		SearchMetadata expectedFile = new SearchMetadata("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
				"MPL_ORBPRE", "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF", "2017-12-05T20:03:09",
				"2017-12-15T20:03:09");
		ResponseEntity<SearchMetadata> r = new ResponseEntity<SearchMetadata>(expectedFile, HttpStatus.OK);
		when(restTemplate.exchange(Mockito.any(), eq(HttpMethod.GET), eq(null), eq(SearchMetadata.class)))
				.thenReturn(r);

		SearchMetadata file = this.service.search(new SearchMetadataQuery(1, "LatestValCover", 1, 2, "AUX_OBMEMC"),
				new Date(), new Date(), "A", -1);
		assertEquals("MPL_ORBPRE", file.getProductType());
		assertEquals("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF", file.getProductName());
		assertEquals("S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF", file.getKeyObjectStorage());
		assertEquals("2017-12-05T20:03:09", file.getValidityStart());
		assertEquals("2017-12-15T20:03:09", file.getValidityStop());

	}

	@Test
	public void testSearchKo() throws JobGenMetadataException {
		ResponseEntity<SearchMetadata> r = new ResponseEntity<SearchMetadata>(HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(Mockito.any(), eq(HttpMethod.GET), eq(null), eq(SearchMetadata.class)))
				.thenReturn(r);

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("nvalid HTTP statu");
		this.service.search(new SearchMetadataQuery(1, "LatestValCover", 1, 2, "AUX_OBMEMC"), new Date(), new Date(),
				"A", -1);
	}

	@Test
	public void testSearchRestKo() throws JobGenMetadataException {
		doThrow(new RestClientException("rest exception")).when(restTemplate).exchange(Mockito.any(),
				eq(HttpMethod.GET), eq(null), eq(SearchMetadata.class));

		thrown.expect(JobGenMetadataException.class);
		thrown.expectMessage("rest exception");
		thrown.expectCause(isA(RestClientException.class));
		this.service.search(new SearchMetadataQuery(1, "LatestValCover", 1, 2, "AUX_OBMEMC"), new Date(), new Date(),
				"A", -1);
	}

}
