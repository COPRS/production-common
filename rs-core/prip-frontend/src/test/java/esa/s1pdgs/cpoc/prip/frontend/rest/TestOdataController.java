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

package esa.s1pdgs.cpoc.prip.frontend.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.matchAll;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
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
import esa.s1pdgs.cpoc.prip.model.filter.PripInFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.LogicalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;

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

		doReturn(l).when(pripMetadataRepository).findAll(Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setValue(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setRelationalOperator(RelationalOperator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setValue(LocalDateTime.of(2020, 01, 06, 02, 00, 00));
		f2.setRelationalOperator(RelationalOperator.LT);
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(matchAll(f1, f2), Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setValue(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setRelationalOperator(RelationalOperator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setValue(LocalDateTime.of(2020, 01, 04, 00, 00, 00));
		f2.setRelationalOperator(RelationalOperator.LT);
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(matchAll(f1, f2), Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CONTENT_DATE_START);
		f1.setValue(LocalDateTime.of(2019, 01, 01, 00, 00, 00));
		f1.setRelationalOperator(RelationalOperator.GT);
		
		PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CONTENT_DATE_END);
		f2.setValue(LocalDateTime.of(2020, 01, 06, 02, 00, 00));
		f2.setRelationalOperator(RelationalOperator.LT);
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(matchAll(f1, f2), Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.STARTS_WITH);
		n1.setText("name1");
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(n1, Optional.empty(), Optional.empty(), Collections.emptyList());

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

		final PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME.fieldName(), PripTextFilter.Function.ENDS_WITH, "ZIP");

		doReturn(metadata).when(this.pripMetadataRepository).findWithFilter(n1, Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.CONTAINS);
		n1.setText("e1a");
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(n1, Optional.empty(), Optional.empty(), Collections.emptyList());

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

		PripTextFilter n2 = new PripTextFilter(FIELD_NAMES.NAME);
		n2.setFunction(PripTextFilter.Function.STARTS_WITH);
		n2.setText("name");
		
		PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.CONTAINS);
		n1.setText("e1a");
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(matchAll(n2, n1), Optional.empty(), Optional.empty(), Collections.emptyList());
		
		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=startswith(Name,'name') and contains(Name,'e1a')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);
		expectIdAndName(ra, p2);
	}
	
	@Test
	public void testAttributeFiltering_eq() throws Exception {
		
		List<PripMetadata> l = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		l.add(p1);
		
		PripTextFilter n1 = new PripTextFilter("attr_productType_string");
		n1.setFunction(PripTextFilter.Function.EQ);
		n1.setText("HK_RAW__0_");
		
		doReturn(l).when(pripMetadataRepository).findWithFilter(n1, Optional.empty(), Optional.empty(), Collections.emptyList());
		
		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=Attributes/OData.CSC.StringAttribute/any(att:att/Name eq 'productType' and att/OData.CSC.StringAttribute/Value eq 'HK_RAW__0_')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);	
	}
	
	@Test
	public void testNameFiltering_not_contains() throws Exception {
		List<PripMetadata> l1 = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1abc");
		l1.add(p1);
		
		List<PripMetadata> l2 = new ArrayList<>();
		PripMetadata p2 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name");
		l2.add(p2);
		
		PripTextFilter textFilter = new PripTextFilter(FIELD_NAMES.NAME);
		textFilter.setFunction(PripTextFilter.Function.CONTAINS);
		textFilter.setText("e1a");
		
		List<PripQueryFilter> filters = new ArrayList<>();
		filters.add(textFilter);
		PripQueryFilter notFilter = new PripQueryFilterList(LogicalOperator.NOT, filters); 
		
		doReturn(l1).when(pripMetadataRepository).findWithFilter(textFilter,Optional.empty(), Optional.empty(), Collections.emptyList());
		doReturn(l2).when(pripMetadataRepository).findWithFilter(notFilter,Optional.empty(), Optional.empty(), Collections.emptyList());

		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=not contains(Name,'e1a')")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
		notExpectIdAndName(ra, p1);
		expectIdAndName(ra, p2);
	}
	
	@Test
	public void testAttributeFiltering_in() throws Exception {
		
		List<PripMetadata> result = new ArrayList<>();
		PripMetadata p1 = createPripMetadata(LocalDateTime.of(2020, 1, 1, 00, 00, 00), "name1");
		result.add(p1);
		
		List<Object> literals = new ArrayList<>();
		literals.add("HK_RAW__0_");
		
		PripInFilter n1 = new PripInFilter("attr_productType_string");
		n1.setFunction(PripInFilter.Function.IN);
		n1.setTerms(literals);
		
		doReturn(result).when(pripMetadataRepository).findWithFilter(n1, Optional.empty(), Optional.empty(), Collections.emptyList());
		
		ResultActions ra = this.mockMvc.perform(get(
				"/odata/v1/Products?$filter=Attributes/OData.CSC.StringAttribute/any(att:att/Name eq 'productType' and att/OData.CSC.StringAttribute/Value in ('HK_RAW__0_'))")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		expectIdAndName(ra, p1);	
	}

	private ResultActions expectIdAndName(ResultActions a, PripMetadata p) throws Exception {
		return a.andExpect(content().string(containsString(String.format("\"Id\":\"%s\"", p.getId().toString()))))
				.andExpect(content().string(containsString(String.format("\"Name\":\"%s\"", p.getName()))));
	}
	
	private ResultActions notExpectIdAndName(ResultActions a, PripMetadata p) throws Exception {
		return a.andExpect(content().string(not(containsString(String.format("\"Id\":\"%s\"", p.getId().toString())))))
				.andExpect(content().string(not(containsString(String.format("\"Name\":\"%s\"", p.getName())))));
	}

	private PripMetadata createPripMetadata(LocalDateTime creationDate, String name) {

		Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum1.setValue("000000000000000000000");
		checksum1.setDate(creationDate);

		Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA1");
		checksum2.setValue("111111111111111111111");
		checksum2.setDate(creationDate);

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
