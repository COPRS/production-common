package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.report.MissingOutput;

public class OutputEstimationTest {

	@Before
	public void init() {

	}

	@Test
	public void instrumentShortNameOf() {
		Assert.assertEquals("MW", OutputEstimation.instrumentShortNameOf("MW_0_MWR___"));
	}

	@Test
	public void productClassOf() {
		Assert.assertEquals("C", OutputEstimation.productClassOf("IW_RAW__0C"));
	}

	@Test
	public void typeToRegexp() {
		Assert.assertEquals("^.*IW_SLC__1S.*$", OutputEstimation.typeToRegexp("/data/localWD/31333/^.*IW_SLC__1S.*$"));
		Assert.assertEquals("^.*IW_SLC__1S.*$", OutputEstimation.typeToRegexp("^.*IW_SLC__1S.*$"));
		Assert.assertEquals("^.*IW_SLC__1S.*$", OutputEstimation.typeToRegexp("IW_SLC__1S"));
	}

	@Test
	public void regexpToType() {
		Assert.assertEquals("IW_SLC__1S", OutputEstimation.regexpToType("^.*IW_SLC__1S.*$"));
		Assert.assertEquals("IW_SLC__1S", OutputEstimation.regexpToType("IW_SLC__1S"));
	}

	@Test
	public void findMissingType_0() {

		ApplicationProperties properties = new ApplicationProperties();
		IpfExecutionJob job = null;
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		String productType = "IW_SLC__1S";
		ProductFamily family = ProductFamily.L1_SLICE;
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingType(job, productType, family, productsInWorkDir, 1);
		Assert.assertEquals(0, missingOutputs.size());
	}

	@Test
	public void findMissingType_1() {

		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		String productType = "IW_SLC__1S";
		ProductFamily family = ProductFamily.L1_SLICE;
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingType(job, productType, family, productsInWorkDir, 1);
		Assert.assertEquals(1, missingOutputs.size());
	}
	
	@Test
	public void findMissingTypesFromJob_0() {
		
		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		LevelJobOutputDto o1 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_SLC__1S.*$");
		job.addOutput(o1);
		LevelJobOutputDto o2 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_SLC__1A.*$");
		job.addOutput(o2);
		
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1ADV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingTypesFromJob(job, productsInWorkDir);
		Assert.assertEquals(0, missingOutputs.size());
	}
	
	@Test
	public void findMissingTypesFromJob_2() {
		
		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		LevelJobOutputDto o1 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_SLC__1S.*$");
		job.addOutput(o1);
		LevelJobOutputDto o2 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_SLC__1A.*$");
		job.addOutput(o2);
		LevelJobOutputDto o3 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_GRDH_1S.*$");
		job.addOutput(o3);
		LevelJobOutputDto o4 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_GRDH_1A.*$");
		job.addOutput(o4);
		
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1ADV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingTypesFromJob(job, productsInWorkDir);
		Assert.assertEquals(2, missingOutputs.size());
		Assert.assertEquals("IW_GRDH_1S", missingOutputs.get(0).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_GRDH_1A", missingOutputs.get(1).getProductMetadataCustomObject().get("product_type_string"));
	}
	
	@Test
	public void findMissingTypesFromJob_3() {
		
		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		LevelJobOutputDto o1 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_SLC__1S.*$");
		job.addOutput(o1);
		LevelJobOutputDto o2 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_SLC__1A.*$");
		job.addOutput(o2);
		LevelJobOutputDto o3 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_GRDH_1S.*$");
		job.addOutput(o3);
		LevelJobOutputDto o4 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_GRDH_1A.*$");
		job.addOutput(o4);
		
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_IW_SLC__1ADV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingTypesFromJob(job, productsInWorkDir);
		Assert.assertEquals(3, missingOutputs.size());
		Assert.assertEquals("IW_SLC__1S", missingOutputs.get(0).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_GRDH_1S", missingOutputs.get(1).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_GRDH_1A", missingOutputs.get(2).getProductMetadataCustomObject().get("product_type_string"));
	}
	
	@Test
	public void findMissingTypesFromJob_4() {
		
		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		LevelJobOutputDto o1 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_SLC__1S.*$");
		job.addOutput(o1);
		LevelJobOutputDto o2 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_SLC__1A.*$");
		job.addOutput(o2);
		LevelJobOutputDto o3 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_GRDH_1S.*$");
		job.addOutput(o3);
		LevelJobOutputDto o4 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_GRDH_1A.*$");
		job.addOutput(o4);
		
		List<String> productsInWorkDir = new ArrayList<String>();
		productsInWorkDir.add("/workDir/S1B_WV_SLC__1SDV_20181001T134431_20181001T135927_012959_017EF8_00EB.SAFE");

		uut.findMissingTypesFromJob(job, productsInWorkDir);
		Assert.assertEquals(4, missingOutputs.size());
		Assert.assertEquals("IW_SLC__1S", missingOutputs.get(0).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_SLC__1A", missingOutputs.get(1).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_GRDH_1S", missingOutputs.get(2).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_GRDH_1A", missingOutputs.get(3).getProductMetadataCustomObject().get("product_type_string"));
	}
	
	@Test
	public void addMissingOutputFromJob() {
		
		ApplicationProperties properties = new ApplicationProperties();
		CatalogEvent catEvent = new CatalogEvent();
		catEvent.setMissionId("S1");
		catEvent.setSatelliteId("B");
		IpfPreparationJob prepJob = new IpfPreparationJob();
		prepJob.setCatalogEvent(catEvent);
		IpfExecutionJob job = new IpfExecutionJob();
		job.setPreparationJob(prepJob);
		String prefixMonitorLogs = "JUNIT_TEST";
		String listFile = "listFile";
		List<MissingOutput> missingOutputs = new ArrayList<>();

		OutputEstimation uut = new OutputEstimation(properties, prefixMonitorLogs, listFile, missingOutputs);

		LevelJobOutputDto o1 = new LevelJobOutputDto("L1_SLICE", "/data/localWD/31333/^.*IW_SLC__1S.*$");
		job.addOutput(o1);
		LevelJobOutputDto o2 = new LevelJobOutputDto("L1_ACN", "/data/localWD/31333/^.*IW_SLC__1A.*$");
		job.addOutput(o2);

		uut.addMissingOutputFromJob(job);
		Assert.assertEquals(2, missingOutputs.size());
		Assert.assertEquals("IW_SLC__1S", missingOutputs.get(0).getProductMetadataCustomObject().get("product_type_string"));
		Assert.assertEquals("IW_SLC__1A", missingOutputs.get(1).getProductMetadataCustomObject().get("product_type_string"));
	}

}
