package esa.s1pdgs.cpoc.jobgenerator.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFileRaw;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestL0Utils {

    public static TaskTable buildTaskTableAIOP() {
        return TestGenericUtils.buildTaskTableAIOP();
    }

    public static JobOrder buildJobOrderL20171109175634707000125() {
        return buildJobOrderL20171109175634707000125(false);
    }

    public static JobOrder buildJobOrderL20171109175634707000125(
            boolean xmlOnly) {
        JobOrder template = TestGenericUtils.buildJobOrderTemplateAIOP(xmlOnly);

        template.getConf().setSensingTime(new JobOrderSensingTime(
                "20171213_145948000000", "20171213_151725000000"));
        template.getConf()
                .addProcParam(new JobOrderProcParam("Mission_Id", "S1A"));

        JobOrderInput input1 = new JobOrderInput();
        input1.setFileType("MPL_ORBPRE");
        input1.setFileNameType(JobOrderFileNameType.PHYSICAL);
        input1.addFilename(
                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        input1.addTimeInterval(new JobOrderTimeInterval("20171208_200309000000",
                "20171215_200309000000",
                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF"));
        JobOrderInput input2 = new JobOrderInput();
        input2.setFileType("MPL_ORBSCT");
        input2.setFileNameType(JobOrderFileNameType.PHYSICAL);
        input2.addFilename(
                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF",
                "S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
        input2.addTimeInterval(new JobOrderTimeInterval("20140403_224609000000",
                "99991231_235959000000",
                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF"));
        JobOrderInput input3 = new JobOrderInput();
        input3.setFileType("AUX_OBMEMC");
        input3.setFileNameType(JobOrderFileNameType.PHYSICAL);
        input3.addFilename(
                "/data/localWD/564061776/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
        input3.addTimeInterval(new JobOrderTimeInterval("20140201_000000000000",
                "99991231_235959000000",
                "/data/localWD/564061776/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"));

        template.getProcs().get(0).addInput(input1);
        template.getProcs().get(0).addInput(input2);
        template.getProcs().get(0).addInput(input3);

        return template;
    }

    public static EdrsSessionFile createEdrsSessionFileChannel1(
            boolean xmlOnlyForRaws) {

        EdrsSessionFile r = new EdrsSessionFile();
        r.setSessionId("L20171109175634707000125");
        r.setStartTime("2017-12-13T14:59:48Z");
        r.setStopTime("2017-12-13T15:17:25Z");
        r.setRawNames(
                TestL0Utils.getEdrsSessionFileRawsChannel1(xmlOnlyForRaws));
        return r;
    }

    public static EdrsSessionFile createEdrsSessionFileChannel2(
            boolean xmlOnlyForRaws) {

        EdrsSessionFile r = new EdrsSessionFile();
        r.setSessionId("L20171109175634707000125");
        r.setStartTime("2017-12-13T14:59:48Z");
        r.setStopTime("2017-12-13T15:17:25Z");
        r.setRawNames(
                TestL0Utils.getEdrsSessionFileRawsChannel2(xmlOnlyForRaws));
        return r;
    }

    public static AppDataJobDto<EdrsSessionDto> buildAppDataEdrsSession(
            boolean xmlOnlyForRaws) {
        return buildAppDataEdrsSession(xmlOnlyForRaws, "S1", true, true);
    }

    public static AppDataJobDto<EdrsSessionDto> buildAppDataEdrsSessionWithRaw2(
            boolean xmlOnlyForRaws) {
        return buildAppDataEdrsSession(xmlOnlyForRaws, "S1", false, true);
    }

    public static AppDataJobDto<EdrsSessionDto> buildAppDataEdrsSession(
            boolean xmlOnlyForRaws, String missionId, boolean raw1,
            boolean raw2) {
        AppDataJobDto<EdrsSessionDto> ret = new AppDataJobDto<>();
        ret.setIdentifier(123);
        ret.setState(AppDataJobDtoState.GENERATING);
        ret.setPod("hostname");
        ret.setLevel(ApplicationLevel.L0);

        List<GenericMessageDto<EdrsSessionDto>> messages = new ArrayList<>();
        if (raw1) {
            GenericMessageDto<EdrsSessionDto> message1 =
                    new GenericMessageDto<EdrsSessionDto>(1, "input-key",
                            new EdrsSessionDto("obs1", 1,
                                    EdrsSessionFileType.SESSION, missionId,
                                    "A", "WILE"));
            messages.add(message1);
        }
        if (raw2) {
            GenericMessageDto<EdrsSessionDto> message2 =
                    new GenericMessageDto<EdrsSessionDto>(1, "input-key",
                            new EdrsSessionDto("obs2", 2,
                                    EdrsSessionFileType.SESSION, missionId,
                                    "A", "WILE"));
            messages.add(message2);
        }
        ret.setMessages(messages);

        AppDataJobProductDto product = new AppDataJobProductDto();
        product.setMissionId(missionId);
        product.setProductName("L20171109175634707000125");
        if (raw1) {
            product.setRaws1(getRawsChannel1(xmlOnlyForRaws));
        }
        if (raw2) {
            product.setRaws2(getRawsChannel2(xmlOnlyForRaws));
        }
        product.setSessionId("L20171109175634707000125");
        product.setSatelliteId("A");
        product.setStationCode("WILE");
        product.setStartTime("2017-12-13T14:59:48.123456Z");
        product.setStopTime("2017-12-13T15:17:25.142536Z");
        product.setInsConfId(-1);
        product.setProcessMode("");
        ret.setProduct(product);

        AppDataJobGenerationDto gen1 = new AppDataJobGenerationDto();
        gen1.setTaskTable("TaskTable.AIOP.xml");
        gen1.setState(AppDataJobGenerationDtoState.INITIAL);
        ret.setGenerations(Arrays.asList(gen1));

        return ret;
    }

    public static List<AppDataJobFileDto> getRawsChannel1(
            boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new AppDataJobFileDto(
                    "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
        }

        return Arrays.asList(new AppDataJobFileDto(
                "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
    }

    public static List<AppDataJobFileDto> getRawsChannel2(
            boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new AppDataJobFileDto(
                    "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                    new AppDataJobFileDto(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
        }

        return Arrays.asList(new AppDataJobFileDto(
                "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                new AppDataJobFileDto(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
    }

    public static List<EdrsSessionFileRaw> getEdrsSessionFileRawsChannel1(
            boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new EdrsSessionFileRaw(
                    "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
        }

        return Arrays.asList(new EdrsSessionFileRaw(
                "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
    }

    public static List<EdrsSessionFileRaw> getEdrsSessionFileRawsChannel2(
            boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new EdrsSessionFileRaw(
                    "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                    new EdrsSessionFileRaw(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
        }

        return Arrays.asList(new EdrsSessionFileRaw(
                "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                new EdrsSessionFileRaw(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
    }
}
