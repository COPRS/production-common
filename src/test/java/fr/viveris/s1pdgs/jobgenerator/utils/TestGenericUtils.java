package fr.viveris.s1pdgs.jobgenerator.utils;

import java.util.ArrayList;

import fr.viveris.s1pdgs.jobgenerator.model.ProcessLevel;
import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.AbstractJobOrderConf;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderBreakpoint;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderOutput;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProc;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.L0JobOrderConf;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderDestination;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTable;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableCfgFile;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableDynProcParam;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableInput;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableInputAlternative;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableOuput;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTablePool;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableTask;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputMode;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputOrigin;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableOutputDestination;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableTestEnum;

public class TestGenericUtils {

	public static TaskTable buildTaskTableAIOP() {

		TaskTable t = new TaskTable();
		t.setProcessorName("AIO_PROCESSOR");
		t.setVersion("01.00");
		t.setTest(TaskTableTestEnum.NO);
		t.setLevel(ProcessLevel.L0);

		t.addDynProcParam(new TaskTableDynProcParam("Processing_Mode", "String", "NRT"));
		t.addDynProcParam(new TaskTableDynProcParam("PT_Assembly", "String", "yes"));
		t.addDynProcParam(new TaskTableDynProcParam("Timeout", "String", "4"));
		t.addDynProcParam(new TaskTableDynProcParam("Mission_Id", "String", "S1A"));
		t.addDynProcParam(new TaskTableDynProcParam("Processing_Station", "String", "WILE"));

		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/Sentinel1AIOProcessor.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/Sentinel1AIODPAssembler.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/Sentinel1CCSDSTelemetry.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/DISSlotConf.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/Sentinel1L0ProductDescriptor.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/Sentinel1ProductFileName.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/SafeProduct.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/ISIPFormat.xml", "01.00"));
		t.addCfgFile(new TaskTableCfgFile("/usr/local/conf/AIOP/ExplorerWrapper.xml", "01.00"));

		TaskTablePool pool1 = new TaskTablePool();
		pool1.setDetached(false);
		pool1.setKillingSignal(15);
		pool1.addTask(buildTaskAIOProcessor());
		pool1.addTask(buildTaskAIOProcessor());
		pool1.addTask(buildTaskAIOAssembler());
		t.addPool(pool1);

		TaskTablePool pool2 = new TaskTablePool();
		pool2.setDetached(false);
		pool2.setKillingSignal(15);
		pool2.addTask(buildTaskAIOList());
		t.addPool(pool2);
		return t;
	}

	private static TaskTableTask buildTaskAIOProcessor() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("AIOP_PROC_APP");
		task11.setVersion("01.00");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/AIOP/bin/S1AIOProcessor");
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(0).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 345600, 0, "MPL_ORBPRE", TaskTableFileNameType.PHYSICAL));
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(1).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "MPL_ORBSCT", TaskTableFileNameType.PHYSICAL));
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(2).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_OBMEMC", TaskTableFileNameType.PHYSICAL));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "SM_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "IW_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "EW_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "WV_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "RF_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "AN_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "EN_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZS_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZE_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZI_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZW_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "GP_RAW__0_",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "HK_RAW__0_",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_ACQNR",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_L0PSA_",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_EFEP_",
				TaskTableFileNameType.REGEXP));

		return task11;
	}

	private static TaskTableTask buildTaskAIOAssembler() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("AIOP_DPASSEMBLER_APP");
		task11.setVersion("01.00");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/AIOP/bin/S1AIODPAssembler");
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(0).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 345600, 0, "MPL_ORBPRE", TaskTableFileNameType.PHYSICAL));
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(1).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "MPL_ORBSCT", TaskTableFileNameType.PHYSICAL));
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(2).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_OBMEMC", TaskTableFileNameType.PHYSICAL));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_L0PSA_",
				TaskTableFileNameType.REGEXP));

		return task11;
	}

	private static TaskTableTask buildTaskAIOList() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("AIOP_LIST_APP");
		task11.setVersion("01.00");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/AIOP/bin/makeListFile.sh");
		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(0).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_OBMEMC", TaskTableFileNameType.PHYSICAL));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.YES,
				"ProcessorConfiguration", TaskTableFileNameType.PHYSICAL));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "SM_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "IW_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "EW_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "WV_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "RF_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "AN_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "EN_RAW__0S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZS_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZE_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZI_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "ZW_RAW__0S",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "GP_RAW__0_",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "HK_RAW__0_",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_ACQNR",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_L0PSA_",
				TaskTableFileNameType.REGEXP));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.NO, "REP_EFEP_",
				TaskTableFileNameType.REGEXP));

		return task11;
	}

	public static JobOrder buildJobOrderTemplateAIOP(boolean xmlOnly) {
		AbstractJobOrderConf conf = new L0JobOrderConf();
		conf.setProcessorName("AIO_PROCESSOR");
		conf.setVersion("01.00");
		conf.setStderrLogLevel("INFO");
		conf.setStdoutLogLevel("INFO");
		conf.setTest(false);
		conf.setBreakPointEnable(false);
		conf.setProcessingStation("WILE");

		JobOrderProcParam procParam1 = new JobOrderProcParam("Processing_Mode", "FAST24");
		JobOrderProcParam procParam2 = new JobOrderProcParam("PT_Assembly", "no");
		JobOrderProcParam procParam3 = new JobOrderProcParam("Timeout", "360");
		JobOrderProcParam procParam4 = new JobOrderProcParam("Mission_Id", "S1");
		JobOrderProcParam procParam5 = new JobOrderProcParam("Processing_Station", "WILE");

		conf.addProcParam(procParam1);
		conf.addProcParam(procParam2);
		conf.addProcParam(procParam3);
		conf.addProcParam(procParam5);
		conf.addProcParam(procParam4);

		conf.addConfigFile("/usr/local/conf/AIOP/Sentinel1AIOProcessor.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/Sentinel1AIODPAssembler.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/Sentinel1CCSDSTelemetry.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/DISSlotConf.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/Sentinel1L0ProductDescriptor.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/Sentinel1ProductFileName.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/SafeProduct.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/ISIPFormat.xml");
		conf.addConfigFile("/usr/local/conf/AIOP/ExplorerWrapper.xml");

		JobOrderProc proc1 = new JobOrderProc();
		proc1.setTaskName("AIOP_PROC_APP");
		proc1.setTaskVersion("01.00");
		proc1.setBreakpoint(new JobOrderBreakpoint("OFF", new ArrayList<>()));
		if (xmlOnly) {
			proc1.addOutput(new JobOrderOutput("SM_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_S[1-6]_RAW__0S.*$"));
			proc1.addOutput(
					new JobOrderOutput("IW_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(
					new JobOrderOutput("EW_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(
					new JobOrderOutput("WV_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(
					new JobOrderOutput("RF_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(new JobOrderOutput("AN_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_N[1-6]_RAW__0S.*$"));
			proc1.addOutput(
					new JobOrderOutput("EN_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(new JobOrderOutput("ZS_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_Z[1-6]_RAW__0S.*$"));
			proc1.addOutput(new JobOrderOutput("ZE_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZE_RAW__0S"));
			proc1.addOutput(new JobOrderOutput("ZI_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZI_RAW__0S"));
			proc1.addOutput(new JobOrderOutput("ZW_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZW_RAW__0S"));
			proc1.addOutput(
					new JobOrderOutput("GP_RAW__0_", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(
					new JobOrderOutput("HK_RAW__0_", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776"));
			proc1.addOutput(
					new JobOrderOutput("REP_ACQNR", JobOrderFileNameType.REGEXP, "/data/localWD/564061776/REP_ACQNR"));
			proc1.addOutput(new JobOrderOutput("REP_L0PSA_", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A|B|_]_OPER_REP_ACQ.*$"));
			proc1.addOutput(new JobOrderOutput("REP_EFEP_", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A|B|_]_OPER_REP_PASS.*.EOF$"));
		} else {

			proc1.addOutput(new JobOrderOutput("SM_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_S[1-6]_RAW__0S.*$", JobOrderDestination.PROC, false,
					ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("IW_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("EW_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("WV_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("RF_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("AN_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_N[1-6]_RAW__0S.*$", JobOrderDestination.PROC, false,
					ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("EN_RAW__0S", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("ZS_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A-B]_Z[1-6]_RAW__0S.*$", JobOrderDestination.PROC, false,
					ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("ZE_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZE_RAW__0S", JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("ZI_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZI_RAW__0S", JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("ZW_RAW__0S", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/ZW_RAW__0S", JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("GP_RAW__0_", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("HK_RAW__0_", JobOrderFileNameType.DIRECTORY, "/data/localWD/564061776",
					JobOrderDestination.PROC, false, ProductFamily.L0_PRODUCT));
			proc1.addOutput(new JobOrderOutput("REP_ACQNR", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/REP_ACQNR", JobOrderDestination.PROC, false, ProductFamily.L0_REPORT));
			proc1.addOutput(new JobOrderOutput("REP_L0PSA_", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A|B|_]_OPER_REP_ACQ.*$", JobOrderDestination.PROC, false,
					ProductFamily.L0_REPORT));
			proc1.addOutput(new JobOrderOutput("REP_EFEP_", JobOrderFileNameType.REGEXP,
					"/data/localWD/564061776/^S1[A|B|_]_OPER_REP_PASS.*.EOF$", JobOrderDestination.PROC, false,
					ProductFamily.L0_REPORT));
		}

		JobOrderProc proc2 = new JobOrderProc();
		proc2.setTaskName("AIOP_PROC_APP");
		proc2.setTaskVersion("01.00");
		proc2.setBreakpoint(new JobOrderBreakpoint("OFF", new ArrayList<>()));

		JobOrderProc proc3 = new JobOrderProc();
		proc3.setTaskName("AIOP_DPASSEMBLER_APP");
		proc3.setTaskVersion("01.00");
		proc3.setBreakpoint(new JobOrderBreakpoint("OFF", new ArrayList<>()));

		JobOrderProc proc4 = new JobOrderProc();
		proc4.setTaskName("AIOP_LIST_APP");
		proc4.setTaskVersion("01.00");
		proc4.setBreakpoint(new JobOrderBreakpoint("OFF", new ArrayList<>()));

		JobOrder job = new JobOrder();
		job.setConf(conf);
		job.addProc(proc1);
		job.addProc(proc2);
		job.addProc(proc3);
		job.addProc(proc4);

		return job;
	}

	public static TaskTable buildTaskTableIW() {

		TaskTable t = new TaskTable();
		t.setProcessorName("IW_RAW__0_GRDH_1");
		t.setVersion("02.84");
		t.setTest(TaskTableTestEnum.NO);
		t.setLevel(ProcessLevel.L1);

		t.addDynProcParam(new TaskTableDynProcParam("Application_LUT", "String", "IW_Default"));
		t.addDynProcParam(new TaskTableDynProcParam("Timeliness_Category", "String", "NRT-3h"));
		t.addDynProcParam(new TaskTableDynProcParam("Mission_Id", "String", "S1A"));
		t.addDynProcParam(new TaskTableDynProcParam("Dem", "String", "coarse"));
		t.addDynProcParam(new TaskTableDynProcParam("Slicing_Flag", "String", "TRUE"));
		t.addDynProcParam(new TaskTableDynProcParam("Slice_Number", "Number", "1"));
		t.addDynProcParam(new TaskTableDynProcParam("Total_Number_Of_Slices", "Number", "1"));
		t.addDynProcParam(new TaskTableDynProcParam("Slice_Length", "Number", "25.0"));
		t.addDynProcParam(new TaskTableDynProcParam("Slice_Overlap", "Number", "7.4"));

		t.addCfgFile(new TaskTableCfgFile("/usr/local/components/S1IPF/etc/processorConfiguration.xml", "02.84"));

		TaskTablePool pool1 = new TaskTablePool();
		pool1.setDetached(false);
		pool1.setKillingSignal(15);
		pool1.addTask(buildTaskIWPCS());
		t.addPool(pool1);

		TaskTablePool pool2 = new TaskTablePool();
		pool2.setDetached(false);
		pool2.setKillingSignal(15);
		pool2.addTask(buildTaskIWMDC());
		t.addPool(pool2);

		TaskTablePool pool3 = new TaskTablePool();
		pool3.setDetached(false);
		pool3.setKillingSignal(15);
		pool3.addTask(buildTaskIWWPC());
		t.addPool(pool3);

		TaskTablePool pool4 = new TaskTablePool();
		pool4.setDetached(false);
		pool4.setKillingSignal(15);
		pool4.addTask(buildTaskIWLPC1());
		t.addPool(pool4);

		TaskTablePool pool5 = new TaskTablePool();
		pool5.setDetached(false);
		pool5.setKillingSignal(15);
		pool5.addTask(buildTaskIWStats());
		t.addPool(pool5);
		return t;
	}

	private static TaskTableTask buildTaskIWPCS() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("PSC");
		task11.setVersion("02.84");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/S1IPF/bin/PSC_PreprocMain");

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(0).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0S", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.SLICING, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(1).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0C", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.NON_SLICING, TaskTableMandatoryEnum.NO));
		task11.getInputs().get(2).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0C", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.SLICING, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(3).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0N", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.NON_SLICING, TaskTableMandatoryEnum.NO));
		task11.getInputs().get(4).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0N", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.SLICING, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(5).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0A", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.NON_SLICING, TaskTableMandatoryEnum.NO));
		task11.getInputs().get(6).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "IW_RAW__0A", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(7).setId("AUX_PP1");
		task11.getInputs().get(7).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_PP1", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(8).setId("AUX_CAL");
		task11.getInputs().get(8).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_CAL", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(9).setId("AUX_INS");
		task11.getInputs().get(9).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_INS", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.NO));
		task11.getInputs().get(10).setId("Orbit");
		task11.getInputs().get(10).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_POE", TaskTableFileNameType.PHYSICAL));
		task11.getInputs().get(10).addAlternative(new TaskTableInputAlternative(2, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_RES", TaskTableFileNameType.PHYSICAL));

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.NO));
		task11.getInputs().get(11).setId("AUX_ATT");
		task11.getInputs().get(11).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.DB,
				"LatestValCover", 0, 0, "AUX_ATT", TaskTableFileNameType.PHYSICAL));

		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.YES, "SignalData",
				TaskTableFileNameType.DIRECTORY));

		return task11;
	}

	private static TaskTableTask buildTaskIWMDC() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("MDC");
		task11.setVersion("02.84");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/S1IPF/bin/MDC_MultiSwathDopMain");

		task11.addInput(new TaskTableInput("AUX_PP1"));
		task11.addInput(new TaskTableInput("AUX_CAL"));
		task11.addInput(new TaskTableInput("AUX_INS"));
		task11.addInput(new TaskTableInput("Orbit"));
		task11.addInput(new TaskTableInput("AUX_ATT"));

		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.YES,
				"DopplerCentroidEstimates", TaskTableFileNameType.DIRECTORY));

		return task11;
	}

	private static TaskTableTask buildTaskIWWPC() {

		TaskTableTask task11 = new TaskTableTask();
		task11.setName("WPC");
		task11.setVersion("02.84");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/S1IPF/bin/WPC_ProcMain");

		task11.addInput(new TaskTableInput("AUX_PP1"));
		task11.addInput(new TaskTableInput("AUX_CAL"));
		task11.addInput(new TaskTableInput("AUX_INS"));
		task11.addInput(new TaskTableInput("Orbit"));
		task11.addInput(new TaskTableInput("AUX_ATT"));

		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.PROC, TaskTableMandatoryEnum.YES, "IW_SL1__1_",
				TaskTableFileNameType.DIRECTORY));

		return task11;
	}

	private static TaskTableTask buildTaskIWLPC1() {
		TaskTableTask task11 = new TaskTableTask();
		task11.setName("LPC1");
		task11.setVersion("02.84");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/S1IPF/bin/LPC1_ProcMain.sh");

		task11.addInput(new TaskTableInput(TaskTableInputMode.ALWAYS, TaskTableMandatoryEnum.YES));
		task11.getInputs().get(0).setId("SL1");
		task11.getInputs().get(0).addAlternative(new TaskTableInputAlternative(1, TaskTableInputOrigin.PROC,
				"LatestValCover", 0, 0, "IW_SL1__1_", TaskTableFileNameType.REGEXP));

		task11.addInput(new TaskTableInput("AUX_PP1"));
		task11.addInput(new TaskTableInput("AUX_CAL"));
		task11.addInput(new TaskTableInput("AUX_INS"));
		task11.addInput(new TaskTableInput("Orbit"));
		task11.addInput(new TaskTableInput("AUX_ATT"));

		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.DB, TaskTableMandatoryEnum.YES, "IW_GRDH_1S",
				TaskTableFileNameType.DIRECTORY));
		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.DB, TaskTableMandatoryEnum.NO, "IW_GRDH_1A",
				TaskTableFileNameType.DIRECTORY));

		return task11;
	}

	private static TaskTableTask buildTaskIWStats() {
		TaskTableTask task11 = new TaskTableTask();
		task11.setName("stats");
		task11.setVersion("02.84");
		task11.setCritical(true);
		task11.setCriticityLevel(2);
		task11.setFileName("/usr/local/components/S1IPF/bin/stats.sh");

		task11.addInput(new TaskTableInput("AUX_PP1"));
		task11.addInput(new TaskTableInput("AUX_CAL"));
		task11.addInput(new TaskTableInput("AUX_INS"));
		task11.addInput(new TaskTableInput("Orbit"));
		task11.addInput(new TaskTableInput("AUX_ATT"));

		task11.addOutput(new TaskTableOuput(TaskTableOutputDestination.DB, TaskTableMandatoryEnum.YES, "IW_GRDH_1S",
				TaskTableFileNameType.DIRECTORY));

		return task11;
	}
}
