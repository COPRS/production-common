package fr.viveris.s1pdgs.level0.wrapper;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobOutputDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobTaskDto;

public class TestUtils {

	public final static String WORKDIR = "./test_work_dir/";

	public static JobDto buildL0JobDto() {
		JobDto dto = new JobDto("SESSIONID", WORKDIR, WORKDIR + "JobOrder.xml");
		
		dto.addInput(buildAuxiliaryInputDto("AUX_OBMEMC.xml"));
		dto.addInput(buildAuxiliaryInputDto("MPL_OBRP.xml"));
		dto.addInput(buildAuxiliaryInputDto("MPL_DLF.xml"));
		dto.addInput(buildJobInputDto());
		dto.addInput(buildRawInputDto("SESSIONID_ch1_001.raw", 1));
		dto.addInput(buildRawInputDto("SESSIONID_ch1_002.raw", 1));
		dto.addInput(buildRawInputDto("SESSIONID_ch2_001.raw", 2));
		dto.addInput(buildRawInputDto("SESSIONID_ch2_002.raw", 2));
		dto.addInput(new JobInputDto("RAW", WORKDIR + "ch01/testrename.raw", "SESSIONID_ch1_003.raw"));
		
		dto.addPool(buildPoolDto1());
		dto.addPool(buildPoolDto2());
		dto.addPool(buildPoolDto3());
		
		dto.addOutput(buildProductOutputDto("^.*SM_RAW__0S.*$"));
		dto.addOutput(buildAcnOutputDto("^.*SM_RAW__0A.*$"));
		dto.addOutput(buildAcnOutputDto("^.*SM_RAW__0A.*$"));
		dto.addOutput(buildAcnOutputDto("^.*SM_RAW__0A.*$"));
		dto.addOutput(buildReportOutputDto("^S1[A|B|_]_OPER_REP_PASS.*.EOF$"));
		
		return dto;
	}

	public static JobInputDto buildRawInputDto(String filename, int channelId) {
		String localPath = WORKDIR + "ch0" + channelId + "/" + filename;
		String keyObjectStorage = filename;
		return new JobInputDto("RAW", localPath, keyObjectStorage);
	}

	public static JobInputDto buildAuxiliaryInputDto(String filename) {
		String localPath = WORKDIR + filename;
		String keyObjectStorage = filename;
		return new JobInputDto("CONFIG", localPath, keyObjectStorage);
	}

	public static JobInputDto buildJobInputDto() {
		String localPath = WORKDIR + "JobOrder.xml";
		String keyObjectStorage = "<xml>\n<balise1></balise1>";
		return new JobInputDto("JOB", localPath, keyObjectStorage);
	}
	
	public static JobPoolDto buildPoolDto1() {
		JobPoolDto r = new JobPoolDto();
		r.addTask(new JobTaskDto("echo task 1 1"));
		return r;
	}
	
	public static JobPoolDto buildPoolDto2() {
		JobPoolDto r = new JobPoolDto();
		r.addTask(new JobTaskDto("echo task 2 1"));
		r.addTask(new JobTaskDto("echo task 2 2"));
		r.addTask(new JobTaskDto("echo task 2 3"));
		return r;
	}
	
	public static JobPoolDto buildPoolDto3() {
		JobPoolDto r = new JobPoolDto();
		r.addTask(new JobTaskDto("echo task 3 1"));
		return r;
	}
	
	public static JobOutputDto buildReportOutputDto(String regexp) {
		return new JobOutputDto("L0_REPORT", regexp);
	}
	
	public static JobOutputDto buildProductOutputDto(String regexp) {
		return new JobOutputDto("L0_PRODUCT", regexp);
	}
	
	public static JobOutputDto buildAcnOutputDto(String regexp) {
		return new JobOutputDto("L0_ACN", regexp);
	}
	
	public static String getAbsolutePath(String file) {
		// Extract last final path
		String basePath = file;
		int lastIndex = file.lastIndexOf('/');
		if (lastIndex != -1) {
			basePath = file.substring(0, lastIndex);
		}
		return basePath;
	}

}
