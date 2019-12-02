package esa.s1pdgs.cpoc.ipf.preparation.worker.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestL0Utils {

    public static TaskTable buildTaskTableAIOP() {
        return TestGenericUtils.buildTaskTableAIOP();
    }

//    public static JobOrder buildJobOrderL20171109175634707000125() {
//        return buildJobOrderL20171109175634707000125(false);
//    }
//
//    public static JobOrder buildJobOrderL20171109175634707000125(
//            boolean xmlOnly) {
//        JobOrder template = TestGenericUtils.buildJobOrderTemplateAIOP(xmlOnly);
//
//        template.getConf().setSensingTime(new JobOrderSensingTime(
//                "20171213_145948000000", "20171213_151725000000"));
//        template.getConf()
//                .addProcParam(new JobOrderProcParam("Mission_Id", "S1A"));
//
//        JobOrderInput input1 = new JobOrderInput();
//        input1.setFileType("MPL_ORBPRE");
//        input1.setFileNameType(JobOrderFileNameType.PHYSICAL);
//        input1.addFilename(
//                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
//                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
//        input1.addTimeInterval(new JobOrderTimeInterval("20171208_200309000000",
//                "20171215_200309000000",
//                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF"));
//        JobOrderInput input2 = new JobOrderInput();
//        input2.setFileType("MPL_ORBSCT");
//        input2.setFileNameType(JobOrderFileNameType.PHYSICAL);
//        input2.addFilename(
//                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF",
//                "S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
//        input2.addTimeInterval(new JobOrderTimeInterval("20140403_224609000000",
//                "99991231_235959000000",
//                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF"));
//        JobOrderInput input3 = new JobOrderInput();
//        input3.setFileType("AUX_OBMEMC");
//        input3.setFileNameType(JobOrderFileNameType.PHYSICAL);
//        input3.addFilename(
//                "/data/localWD/564061776/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml",
//                "S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml");
//        input3.addTimeInterval(new JobOrderTimeInterval("20140201_000000000000",
//                "99991231_235959000000",
//                "/data/localWD/564061776/S1A_OPER_AUX_OBMEMC_PDMC_20140201T000000.xml"));
//
//        template.getProcs().get(0).addInput(input1);
//        template.getProcs().get(0).addInput(input2);
//        template.getProcs().get(0).addInput(input3);
//
//        return template;
//    }
//
//    public static EdrsSessionFile createEdrsSessionFileChannel1(
//            boolean xmlOnlyForRaws) {
//
//        EdrsSessionFile r = new EdrsSessionFile();
//        r.setSessionId("L20171109175634707000125");
//        r.setStartTime("2017-12-13T14:59:48Z");
//        r.setStopTime("2017-12-13T15:17:25Z");
//        r.setRawNames(
//                TestL0Utils.getEdrsSessionFileRawsChannel1(xmlOnlyForRaws));
//        return r;
//    }
//
//    public static EdrsSessionFile createEdrsSessionFileChannel2(
//            boolean xmlOnlyForRaws) {
//
//        EdrsSessionFile r = new EdrsSessionFile();
//        r.setSessionId("L20171109175634707000125");
//        r.setStartTime("2017-12-13T14:59:48Z");
//        r.setStopTime("2017-12-13T15:17:25Z");
//        r.setRawNames(
//                TestL0Utils.getEdrsSessionFileRawsChannel2(xmlOnlyForRaws));
//        return r;
//    }
    
    public static CatalogEvent newCatalogEvent(
    		final String productName, 
    		final String keyObs, 
    		final ProductFamily family,
    		final String moder
	) {
        // FIXME
    	return new CatalogEvent();
    }


    public static CatalogEvent newCatalogEvent(
    		final String productName, 
    		final String keyObs, 
    		final int channelId, 
    		final EdrsSessionFileType type, 
    		final String missionId,
			final String satelliteId, 
			final String stationCode, 
			final String sessionId
	) {
    	final Map<String,String> map = new HashMap<>();
    	map.put("channelId", String.valueOf(channelId));
    	map.put("missionId", missionId);
    	map.put("satelliteId", satelliteId);
    	map.put("stationCode", stationCode);
      	map.put("sessionId", sessionId);
    	
    	final ObjectMapper mapper = new ObjectMapper();
    	final JsonNode jsonNode = mapper.convertValue(map, JsonNode.class);
    	
    	final CatalogEvent event = new CatalogEvent();
    	event.setProductFamily(ProductFamily.EDRS_SESSION);
    	event.setProductType(type.name());
    	event.setKeyObjectStorage(keyObs);
    	event.setMetadata(jsonNode);    	
    	return event;
	}

//
    public static AppDataJob buildAppDataEdrsSession(
            final boolean xmlOnlyForRaws) {
        return buildAppDataEdrsSession(xmlOnlyForRaws, "S1", true, true);
    }

    public static AppDataJob buildAppDataEdrsSessionWithRaw2(
            final boolean xmlOnlyForRaws) {
        return buildAppDataEdrsSession(xmlOnlyForRaws, "S1", false, true);
    }

    public static AppDataJob buildAppDataEdrsSession(
            final boolean xmlOnlyForRaws, final String missionId, final boolean raw1,
            final boolean raw2) {
        final AppDataJob ret = new AppDataJob();
        ret.setId(123);
        ret.setState(AppDataJobState.GENERATING);
        ret.setPod("hostname");
        ret.setLevel(ApplicationLevel.L0);

        final List<GenericMessageDto<CatalogEvent>> messages = new ArrayList<>();
        if (raw1) {
            final GenericMessageDto<CatalogEvent> message1 =
                    new GenericMessageDto<CatalogEvent>(1, "input-key",
                            newCatalogEvent("obs1", "/path/of/inbox", 1,
                                    EdrsSessionFileType.SESSION, missionId,
                                    "A", "WILE", "sessionId"));
            messages.add(message1);
        }
        if (raw2) {
            final GenericMessageDto<CatalogEvent> message2 =
                    new GenericMessageDto<CatalogEvent>(1, "input-key",
                            newCatalogEvent("obs2", "/path/of/inbox", 2,
                                    EdrsSessionFileType.SESSION, missionId,
                                    "A", "WILE", "sessionId"));
            messages.add(message2);
        }
        
        ret.setMessages(messages.stream().collect(Collectors.toList()));

        final AppDataJobProduct product = new AppDataJobProduct();
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

        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("TaskTable.AIOP.xml");
        gen1.setState(AppDataJobGenerationState.INITIAL);
        ret.setGenerations(Arrays.asList(gen1));

        return ret;
    }

    public static List<AppDataJobFile> getRawsChannel1(
            final boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new AppDataJobFile(
                    "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
        }

        return Arrays.asList(new AppDataJobFile(
                "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
    }

    public static List<AppDataJobFile> getRawsChannel2(
            final boolean xmlOnlyForRaws) {
        if (xmlOnlyForRaws) {
            return Arrays.asList(new AppDataJobFile(
                    "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                    new AppDataJobFile(
                            "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
        }

        return Arrays.asList(new AppDataJobFile(
                "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw",
                "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
                new AppDataJobFile(
                        "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw",
                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
    }

//    public static List<EdrsSessionFileRaw> getEdrsSessionFileRawsChannel1(
//            boolean xmlOnlyForRaws) {
//        if (xmlOnlyForRaws) {
//            return Arrays.asList(new EdrsSessionFileRaw(
//                    "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
//        }
//
//        return Arrays.asList(new EdrsSessionFileRaw(
//                "DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw",
//                "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00001.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00002.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00003.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00004.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00005.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00006.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00007.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00008.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00009.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00010.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00011.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00012.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00013.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00014.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00015.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00016.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00017.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00018.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00019.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00020.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00021.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00022.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00023.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00024.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00025.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00026.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00027.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00028.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00029.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00030.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00031.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00032.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00033.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00034.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw",
//                        "S1A/L20171109175634707000125/ch01/DCS_02_L20171109175634707000125_ch1_DSDB_00035.raw"));
//    }
//
//    public static List<EdrsSessionFileRaw> getEdrsSessionFileRawsChannel2(
//            boolean xmlOnlyForRaws) {
//        if (xmlOnlyForRaws) {
//            return Arrays.asList(new EdrsSessionFileRaw(
//                    "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
//                    new EdrsSessionFileRaw(
//                            "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
//        }
//
//        return Arrays.asList(new EdrsSessionFileRaw(
//                "DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw",
//                "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00001.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00002.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00003.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00004.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00005.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00006.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00007.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00008.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00009.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00010.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00011.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00012.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00013.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00014.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00015.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00016.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00017.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00018.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00019.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00020.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00021.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00022.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00023.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00024.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00025.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00026.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00027.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00028.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00029.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00030.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00031.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00032.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00033.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00034.raw"),
//                new EdrsSessionFileRaw(
//                        "DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw",
//                        "S1A/L20171109175634707000125/ch02/DCS_02_L20171109175634707000125_ch2_DSDB_00035.raw"));
//    }
}
