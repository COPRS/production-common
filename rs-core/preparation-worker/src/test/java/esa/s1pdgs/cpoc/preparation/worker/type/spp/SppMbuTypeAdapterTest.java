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

package esa.s1pdgs.cpoc.preparation.worker.type.spp;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.preparation.worker.model.joborder.JobOrderAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableFactory;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class SppMbuTypeAdapterTest {
	
	@Mock
	private MetadataClient metadataClient;
	
	@Mock
    private ProductTypeAdapter productTypeAdapter;
	
	@Autowired
	private TaskTableFactory taskTableFactory;

	@Autowired
	private ElementMapper elementMapper;
	
	@Autowired
    private ProcessProperties processSettings;
	
	@Autowired
	private PreparationWorkerProperties settings;
	
	@Autowired
    private XmlConverter xmlConverter;

	private SppMbuTypeAdapter uut;
	
	private TaskTableAdapter taskTableAdapter;
	
	private JobOrderAdapter.Factory jobOrderFactory;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		uut = new SppMbuTypeAdapter(metadataClient);
		
		final File xmlFile = new File("./src/test/resources/MBU_TT_01.xml");

		taskTableAdapter = new TaskTableAdapter(xmlFile,
				taskTableFactory.buildTaskTable(xmlFile, ApplicationLevel.SPP_MBU, ""), elementMapper,
				ProductMode.NON_SLICING);
		

        final JobOrder jobOrder = taskTableAdapter.newJobOrder(processSettings, ProductMode.NON_SLICING);

        jobOrderFactory = new JobOrderAdapter.Factory(
                (t) -> jobOrder,
                productTypeAdapter,
                elementMapper,
                xmlConverter,
                settings
        );
	}
	
	@Test
	public void mbuProduction() throws MetadataQueryException, IOException, JAXBException, URISyntaxException {
		
		Instant insertionTime = Instant.now();
		String metadataInsertionTime = toMetadataDateFormat(insertionTime);
		
		String startTime = "2020-01-20T16:47:03.429756Z";
		String stopTime = "2020-01-20T17:12:45.024909Z";
		
		SearchMetadata l2metadata = new SearchMetadata(
				"S1B_WV_OCN__2SSV_20200120T164703_20200120T171244_019903_025A64_140C.SAFE",
				"WV_OCN__2S",
				"S1B_WV_OCN__2SSV_20200120T164703_20200120T171244_019903_025A64_140C.SAFE",
				startTime,
				stopTime, "S1", "B", null);
		l2metadata.setInsertionTime(metadataInsertionTime);
		
		doReturn(l2metadata).when(metadataClient).queryByFamilyAndProductName(ProductFamily.L2_SLICE.name(), l2metadata.getProductName());
		
		AppDataJob appDataJob = new AppDataJob(123L);
		appDataJob.setCreationDate(Date.from(insertionTime.plusSeconds(3)));
		AppDataJobProduct product1 = new AppDataJobProduct();
		product1.getMetadata().put("startTime", startTime);
		product1.getMetadata().put("productName",
				"S1B_WV_OCN__2SSV_20200120T164703_20200120T171244_019903_025A64_140C.SAFE");
		product1.getMetadata().put("productType", "WV_OCN__2S");
		appDataJob.setProduct(product1);
		
		Product product = uut.mainInputSearch(appDataJob, taskTableAdapter);
		appDataJob.setProduct(product.toProduct());
		appDataJob.setAdditionalInputs(product.overridingInputs());
		appDataJob.setStartTime(startTime);
		appDataJob.setStopTime(stopTime);
		
		try {
			uut.validateInputSearch(appDataJob, taskTableAdapter);
		} catch (IpfPrepWorkerInputsMissingException e) {
			fail("All necessary inputs shall be provided, selection logic have a bug!");
		}
		
		JobOrderAdapter jobOrderAdapter = jobOrderFactory.newJobOrderFor(appDataJob, taskTableAdapter);
		
		final String expectedJobOrder = new String(Files.readAllBytes(new File(
				"./src/test/resources/jobOrder_SPP_MBU_expected.xml").toPath()), StandardCharsets.UTF_8);
		
		assertEquals(expectedJobOrder, jobOrderAdapter.toXml());
	}
	
	private String toMetadataDateFormat(Instant date) {
		return DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.ofInstant(date, ZoneId.of("UTC")));
	}

}
