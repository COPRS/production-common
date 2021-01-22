package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
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
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.JobOrderAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
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
    private ProcessSettings processSettings;
	
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
				taskTableFactory.buildTaskTable(xmlFile, ApplicationLevel.SPP_MBU), elementMapper,
				ProductMode.NON_SLICING);
		

        final JobOrder jobOrder = taskTableAdapter.newJobOrder(processSettings, ProductMode.NON_SLICING);

        jobOrderFactory = new JobOrderAdapter.Factory(
                () -> jobOrder,
                productTypeAdapter,
                elementMapper,
                xmlConverter
        );
	}
	
	@Test
	public void mbuProduction() throws MetadataQueryException, IOException, JAXBException {
		
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
		
		JobOrderAdapter jobOrderAdapter = jobOrderFactory.newJobOrderFor(appDataJob);
		
		System.out.println(jobOrderAdapter.toXml());
		
		
	}
	
	private String toMetadataDateFormat(Instant date) {
		return DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.ofInstant(date, ZoneId.of("UTC")));
	}

}
