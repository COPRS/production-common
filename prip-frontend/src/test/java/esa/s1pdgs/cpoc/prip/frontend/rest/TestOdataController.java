package esa.s1pdgs.cpoc.prip.frontend.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.prip.frontend.service.rest.OdataController;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter.Operator;

@RunWith(SpringRunner.class)
@WebMvcTest(OdataController.class)
public class TestOdataController {

	@MockBean
	private PripMetadataRepository pripMetadataRepository;

	@MockBean
	private RestTemplateBuilder builder;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testListing() throws Exception {

		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		PripMetadata p2 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name2");
		l.add(p1);
		l.add(p2);

		doReturn(l).when(pripMetadataRepository).findAll(Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get("/odata/v1/Products").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
		expectIdAndName(ra, p2);

	}

	@Test
	public void testCreationDateFiltering() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		PripMetadata p2 = createPripMetadata(LocalDateTime.of(2020, 1, 5, 00, 00, 00), "name2");
		l.add(p1);
		l.add(p2);

		List<PripDateTimeFilter> creationDateFilters = new ArrayList<>();
		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setDateTime(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setOperator(Operator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setDateTime(LocalDateTime.of(2020, 01, 06, 02, 00, 00));
		f2.setOperator(Operator.LT);
		
		creationDateFilters.add(f1);
		creationDateFilters.add(f2);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(Collections.emptyList(), creationDateFilters, Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=PublicationDate gt 2019-01-01T00:00:00.000Z and PublicationDate lt 2020-01-06T02:00:00.000Z")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
		expectIdAndName(ra, p2);
	}
	
	@Test
	public void testCreationDateFiltering2() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		l.add(p1);

		List<PripDateTimeFilter> creationDateFilters = new ArrayList<>();
		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setDateTime(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setOperator(Operator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setDateTime(LocalDateTime.of(2020, 01, 04, 00, 00, 00));
		f2.setOperator(Operator.LT);
		
		creationDateFilters.add(f1);
		creationDateFilters.add(f2);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(Collections.emptyList(), creationDateFilters, Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=PublicationDate gt 2019-01-01T00:00:00.000Z and PublicationDate lt 2020-01-04T00:00:00.000Z")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
	}
	
	
	@Test
	public void testContentDateFiltering() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		p1.setContentDateStart(LocalDateTime.of(2020, 1, 1, 00, 00, 00));
		p1.setContentDateEnd(LocalDateTime.of(2020, 1, 5, 00, 00, 00));
		l.add(p1);

		List<PripDateTimeFilter> creationDateFilters = new ArrayList<>();
		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CONTENT_DATE_START);
		f1.setDateTime(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setOperator(Operator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CONTENT_DATE_END);
		f2.setDateTime(LocalDateTime.of(2020, 01, 06, 02, 00, 00));
		f2.setOperator(Operator.LT);
		
		creationDateFilters.add(f1);
		creationDateFilters.add(f2);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(Collections.emptyList(), creationDateFilters, Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=ContentDate/Start gt 2019-01-01T00:00:00.000Z and ContentDate/End lt 2020-01-06T02:00:00.000Z")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
	}
	
	@Test
	public void testNameFiltering_startswith() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1abc");
		l.add(p1);

		List<PripTextFilter> nameFilters = new ArrayList<>();
		
		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.STARTS_WITH);
		n1.setText("name1");
		nameFilters.add(n1);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(nameFilters, Collections.emptyList(), Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=startswith(Name,'name1')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
	}
	
	@Test
	public void testNameFiltering_endswith() throws Exception {
		final List<PripMetadata> metadata = new ArrayList<>();
		final PripMetadata metadata_ZIP = this.createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00),	"name1abc.ZIP");
		metadata.add(metadata_ZIP);

		final List<PripTextFilter> nameFilters = new ArrayList<>();
		final PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME, PripTextFilter.Function.ENDS_WITH, "ZIP");
		nameFilters.add(n1);

		doReturn(metadata).when(this.pripMetadataRepository).findWithFilters(nameFilters, Collections.emptyList(),
				Optional.empty(), Optional.empty());

		final ResultActions ra = this.mockMvc
				.perform(get("/odata/v1/Products?$filter=endswith(Name,'ZIP')")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

//		final String contentAsString = ra.andReturn().getResponse().getContentAsString();
//		System.out.println("CONTENT: " + contentAsString);

		this.expectIdAndName(ra, metadata_ZIP);
	}
	
	@Test
	public void testNameFiltering_contains() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1abc");
		l.add(p1);

		List<PripTextFilter> nameFilters = new ArrayList<>();
		
		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.CONTAINS);
		n1.setText("e1a");
		nameFilters.add(n1);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(nameFilters, Collections.emptyList(), Optional.empty(), Optional.empty());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=contains(Name,'e1a')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
	}
	
	@Test
	public void testNameFiltering_startswith_and_contains() throws Exception {
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1abc");
		PripMetadata p2 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1abcdf");
		l.add(p1);
		l.add(p2);

		List<PripTextFilter> nameFilters = new ArrayList<>();
		
		PripTextFilter n2 = new PripTextFilter(FIELD_NAMES.NAME);
		n2.setFunction(PripTextFilter.Function.STARTS_WITH);
		n2.setText("name");
		nameFilters.add(n2);
		
		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.CONTAINS);
		n1.setText("e1a");
		nameFilters.add(n1);
		
		doReturn(l).when(pripMetadataRepository).findWithFilters(nameFilters, Collections.emptyList(), Optional.empty(), Optional.empty());
		
		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=startswith(Name,'name') and contains(Name,'e1a')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
		expectIdAndName(ra, p2);
	}

	private ResultActions expectIdAndName(ResultActions a, PripMetadata p) throws Exception {
		return a.andExpect(content().string(containsString(String.format("\"Id\":\"%s\"", p.getId().toString()))))
				.andExpect(content().string(containsString(String.format("\"Name\":\"%s\"", p.getName()))));
	}

	private PripMetadata createPripMetadata(LocalDateTime creationDate, String name) {

		Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum1.setValue("000000000000000000000");

		Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA1");
		checksum2.setValue("111111111111111111111");

		List<Checksum> checksums = new ArrayList<>();
		checksums.add(checksum1);
		checksums.add(checksum2);

		PripMetadata pripMetadata = new PripMetadata();
		UUID id = UUID.randomUUID();
		pripMetadata.setId(id);
		pripMetadata.setObsKey("productionEvent/keyObjectStorage");
		pripMetadata.setName(name);
		pripMetadata.setProductFamily(ProductFamily.AUXILIARY_FILE_ZIP);
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(0);
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(checksums);
		return pripMetadata;
	}

}
