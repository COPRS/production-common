package esa.s1pdgs.cpoc.compression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;

public class TestUtils {

    public final static String WORKDIR = "./test_work_dir/";

    public static LevelJobDto buildL0LevelJobDto() {
        LevelJobDto dto = new LevelJobDto(ProductFamily.L0_JOB, "SESSIONID", "FAST", WORKDIR, WORKDIR + "JobOrder.xml");

        dto.addInput(buildAuxiliaryInputDto("AUX_OBMEMC.xml"));
        dto.addInput(buildAuxiliaryInputDto("MPL_OBRP.xml"));
        dto.addInput(buildAuxiliaryInputDto("MPL_DLF.xml"));
        dto.addInput(buildLevelJobInputDto());
        dto.addInput(buildRawInputDto("SESSIONID_ch1_001.raw", 1));
        dto.addInput(buildRawInputDto("SESSIONID_ch2_001.raw", 2));
        dto.addInput(buildRawInputDto("SESSIONID_ch1_002.raw", 1));
        dto.addInput(buildRawInputDto("SESSIONID_ch2_002.raw", 2));
        dto.addInput(new LevelJobInputDto("EDRS_SESSION", WORKDIR + "ch01/testrename.raw",
                "SESSIONID_ch1_003.raw"));

        dto.addPool(buildPoolDto1());
        dto.addPool(buildPoolDto2());
        dto.addPool(buildPoolDto3());

        dto.addOutput(buildProductOutputDto("^.*SM_RAW__0S.*$"));
        dto.addOutput(buildProductOutputDto("^.*IW_RAW__0S.*$"));
        dto.addOutput(buildAcnOutputDto("^.*SM_RAW__0A.*$"));
        dto.addOutput(buildAcnOutputDto("^.*IW_RAW__0A.*$"));
        dto.addOutput(buildAcnOutputDto("^.*EW_RAW__0A.*$"));
        dto.addOutput(buildReportOutputDto("^S1[A|B|_]_OPER_REP_PASS.*.EOF$"));
        dto.addOutput(buildReportOutputDto("^report*.XML$"));

        return dto;
    }

    public static List<ObsDownloadObject> getL0DownloadFile() {
        return getL0DownloadFile(buildL0LevelJobDto());
    }

    public static List<ObsDownloadObject> getL0DownloadFile(LevelJobDto l0Job) {

        List<ObsDownloadObject> downloadToBatch = new ArrayList<>();
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(0).getFamily()),
                l0Job.getInputs().get(0).getContentRef(),
                (new File(l0Job.getInputs().get(0).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(1).getFamily()),
                l0Job.getInputs().get(1).getContentRef(),
                (new File(l0Job.getInputs().get(1).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(2).getFamily()),
                l0Job.getInputs().get(2).getContentRef(),
                (new File(l0Job.getInputs().get(2).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(4).getFamily()),
                l0Job.getInputs().get(4).getContentRef(),
                (new File(l0Job.getInputs().get(4).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(5).getFamily()),
                l0Job.getInputs().get(5).getContentRef(),
                (new File(l0Job.getInputs().get(5).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(6).getFamily()),
                l0Job.getInputs().get(6).getContentRef(),
                (new File(l0Job.getInputs().get(6).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(7).getFamily()),
                l0Job.getInputs().get(7).getContentRef(),
                (new File(l0Job.getInputs().get(7).getLocalPath())
                        .getParent())));
        downloadToBatch.add(new ObsDownloadObject(
                ProductFamily.fromValue(l0Job.getInputs().get(8).getFamily()),
                l0Job.getInputs().get(8).getContentRef(),
                (new File(l0Job.getInputs().get(8).getLocalPath())
                        .getParent())));
        return downloadToBatch;
    }

    public static LevelJobInputDto buildRawInputDto(String filename, int channelId) {
        String localPath = WORKDIR + "ch0" + channelId + "/" + filename;
        String keyObjectStorage = filename;
        return new LevelJobInputDto("EDRS_SESSION", localPath, keyObjectStorage);
    }

    public static LevelJobInputDto buildAuxiliaryInputDto(String filename) {
        String localPath = WORKDIR + filename;
        String keyObjectStorage = filename;
        return new LevelJobInputDto("AUXILIARY_FILE", localPath, keyObjectStorage);
    }

    public static LevelJobInputDto buildLevelJobInputDto() {
        String localPath = WORKDIR + "JobOrder.xml";
        String keyObjectStorage = "<xml>\n<balise1></balise1>";
        return new LevelJobInputDto("JOB_ORDER", localPath, keyObjectStorage);
    }

    public static LevelJobInputDto buildBlankInputDto() {
        String localPath = WORKDIR + "blank_file.xml";
        String keyObjectStorage = "blank.xml";
        return new LevelJobInputDto("BLANK", localPath, keyObjectStorage);
    }

    public static LevelJobInputDto buildInvalidInputDto() {
        String localPath = WORKDIR + "invalid.safe";
        String keyObjectStorage = "invalid.safe";
        return new LevelJobInputDto("TGKLH", localPath, keyObjectStorage);
    }

    public static LevelJobPoolDto buildPoolDto1() {
        LevelJobPoolDto r = new LevelJobPoolDto();
        r.addTask(new LevelJobTaskDto("echo task 1 1"));
        return r;
    }

    public static LevelJobPoolDto buildPoolDto2() {
        LevelJobPoolDto r = new LevelJobPoolDto();
        r.addTask(new LevelJobTaskDto("echo task 2 1"));
        r.addTask(new LevelJobTaskDto("echo task 2 2"));
        r.addTask(new LevelJobTaskDto("echo task 2 3"));
        return r;
    }

    public static LevelJobPoolDto buildPoolDto3() {
        LevelJobPoolDto r = new LevelJobPoolDto();
        r.addTask(new LevelJobTaskDto("echo task 3 1"));
        return r;
    }

    public static LevelJobOutputDto buildReportOutputDto(String regexp) {
        return new LevelJobOutputDto("L0_REPORT", regexp);
    }

    public static LevelJobOutputDto buildProductOutputDto(String regexp) {
        return new LevelJobOutputDto("L0_SLICE", regexp);
    }

    public static LevelJobOutputDto buildAcnOutputDto(String regexp) {
        return new LevelJobOutputDto("L0_ACN", regexp);
    }

    public static LevelJobOutputDto buildL0BlankOutputDto(String regexp) {
        return new LevelJobOutputDto("L0_BLANK", regexp);
    }

    public static LevelJobOutputDto buildSegmentReportOutputDto(String regexp) {
        return new LevelJobOutputDto("L0_SEGMENT_REPORT", regexp);
    }

    public static LevelJobOutputDto buildL1ProductOutputDto(String regexp) {
        return new LevelJobOutputDto("L1_SLICE", regexp);
    }

    public static LevelJobOutputDto buildL1AcnOutputDto(String regexp) {
        return new LevelJobOutputDto("L1_ACN", regexp);
    }

    public static LevelJobOutputDto buildL1ReportOutputDto(String regexp) {
        return new LevelJobOutputDto("L1_REPORT", regexp);
    }

    public static LevelJobOutputDto buildBlankOutputDto(String regexp) {
        return new LevelJobOutputDto("BLANK", regexp);
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
