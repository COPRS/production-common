package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.xml.TestGenericUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;

public class TestL0SegmentUtils {

    public static TaskTable buildTaskTableL0ASP() {
        return TestGenericUtils.buildTaskTableL0ASP();
    }

    public static JobOrder buildJobOrderL20171109175634707000125() {
        return buildJobOrderL20171109175634707000125(false);
    }

    public static JobOrder buildJobOrderL20171109175634707000125(
            final boolean xmlOnly) {
        final JobOrder template = TestGenericUtils.buildJobOrderTemplateAIOP(xmlOnly);

        template.getConf().setSensingTime(new JobOrderSensingTime(
                "20171213_145948000000", "20171213_151725000000"));
        template.getConf()
                .addProcParam(new JobOrderProcParam("Mission_Id", "S1A"));

        final JobOrderInput input1 = new JobOrderInput();
        input1.setFileType("MPL_ORBPRE");
        input1.setFileNameType(JobOrderFileNameType.PHYSICAL);
        input1.addFilename(
                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF",
                "S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF");
        input1.addTimeInterval(new JobOrderTimeInterval("20171208_200309000000",
                "20171215_200309000000",
                "/data/localWD/564061776/S1A_OPER_MPL_ORBPRE_20171208T200309_20171215T200309_0001.EOF"));
        final JobOrderInput input2 = new JobOrderInput();
        input2.setFileType("MPL_ORBSCT");
        input2.setFileNameType(JobOrderFileNameType.PHYSICAL);
        input2.addFilename(
                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF",
                "S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF");
        input2.addTimeInterval(new JobOrderTimeInterval("20140403_224609000000",
                "99991231_235959000000",
                "/data/localWD/564061776/S1A_OPER_MPL_ORBSCT_20140507T150704_99999999T999999_0020.EOF"));
        final JobOrderInput input3 = new JobOrderInput();
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

    public static AppDataJob buildAppData() {
        final AppDataJob ret = new AppDataJob();
        ret.setId(123);
        ret.setCreationDate(new Date());
        ret.setState(AppDataJobState.GENERATING);
        ret.setPod("hostname");
        ret.setLevel(ApplicationLevel.L0_SEGMENT);

        final List<GenericMessageDto<CatalogEvent>> messages = new ArrayList<>();
        final CatalogEvent event1 = new CatalogEvent();
        event1.setProductName("S1A_WV_RAW__0SSV_20180913T234452_20180913T235538_023686_0294FC_1BDE.SAFE");
        event1.setKeyObjectStorage("kobs");
        event1.setProductFamily(ProductFamily.L0_SEGMENT);

        final GenericMessageDto<CatalogEvent> message1 =
                new GenericMessageDto<>(1, "input-key", event1);
        messages.add(message1);
        ret.setMessages(new ArrayList<>(messages));

        final Calendar start1 = Calendar.getInstance();
        start1.set(2017, Calendar.DECEMBER, 13, 14, 59, 48);
        final Calendar stop1 = Calendar.getInstance();
        stop1.set(2017, Calendar.DECEMBER, 13, 15, 17, 25);
        final AppDataJobProduct product = new AppDataJobProduct();
        product.setMissionId("S1");
        product.setProductName("l0_segments_of_0294FC");
        product.setSatelliteId("A");
        product.setAcquisition("IW");
        ret.setProduct(product);

        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("TaskTable.L0ASP.xml");
        gen1.setState(AppDataJobGenerationState.INITIAL);
        gen1.setCreationDate(new Date());
        ret.setGeneration(gen1);

        return ret;
    }

    public static void setMessageToBuildData(final AppDataJob job,
            final List<String> segmentNames) {
        job.setMessages(new ArrayList<>());
        int id = 1;
        for (final String name : segmentNames) {
            final CatalogEvent event = new CatalogEvent();
            event.setProductName(name);
            event.setKeyObjectStorage(name);
            event.setProductFamily(ProductFamily.L0_SEGMENT);
            job.getMessages()
                    .add(new GenericMessageDto<>(id, "input-key", event));
            System.out.println("added message with event " + event);
            id++;
        }
    }
}
