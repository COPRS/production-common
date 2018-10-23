package esa.s1pdgs.cpoc.jobgenerator.utils;

import java.util.Arrays;
import java.util.Calendar;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Route;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteFrom;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteTo;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Routing;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestL1Utils {

    public static L1Routing buildL1Routing() {
        L1Routing r = new L1Routing();
        r.addRoute(new L1Route(new L1RouteFrom("EN", "A"),
                new L1RouteTo(Arrays.asList("EN_RAW__0_GRDF_1.xml",
                        "EN_RAW__0_GRDH_1.xml", "EN_RAW__0_GRDM_1.xml",
                        "EN_RAW__0_SLC__1.xml", "EN_RAW__0_SLC__1_GRDF_1.xml",
                        "EN_RAW__0_SLC__1_GRDH_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("EN", "B"),
                new L1RouteTo(Arrays.asList("EN_RAW__0_SLC__1.xml",
                        "EN_RAW__0_SLC__1_GRDF_1.xml",
                        "EN_RAW__0_SLC__1_GRDH_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("EW", "A"),
                new L1RouteTo(Arrays.asList("EW_RAW__0_GRDH_1.xml",
                        "EW_RAW__0_GRDM_1.xml", "EW_RAW__0_SLC__1.xml",
                        "EW_RAW__0_SLC__1_GRDH_1.xml",
                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("EW", "B"),
                new L1RouteTo(Arrays.asList("EW_RAW__0_SLC__1.xml",
                        "EW_RAW__0_SLC__1_GRDH_1.xml",
                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("IW", "A"),
                new L1RouteTo(Arrays.asList("IW_RAW__0_GRDH_1.xml",
                        "IW_RAW__0_GRDM_1.xml", "IW_RAW__0_SLC__1.xml",
                        "IW_RAW__0_SLC__1_GRDH_1.xml",
                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("IW", "B"),
                new L1RouteTo(Arrays.asList("IW_RAW__0_SLC__1.xml",
                        "IW_RAW__0_SLC__1_GRDH_1.xml",
                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("S[1-6]", "A"),
                new L1RouteTo(Arrays.asList("SM_RAW__0_GRDF_1.xml",
                        "SM_RAW__0_GRDH_1.xml", "SM_RAW__0_GRDM_1.xml",
                        "SM_RAW__0_SLC__1.xml", "SM_RAW__0_SLC__1_GRDF_1.xml",
                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));
        r.addRoute(new L1Route(new L1RouteFrom("S[1-6]", "B"),
                new L1RouteTo(Arrays.asList("SM_RAW__0_SLC__1.xml",
                        "SM_RAW__0_SLC__1_GRDF_1.xml",
                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));

        return r;
    }

    public static AppDataJobDto<LevelProductDto> buildJobGeneration(
            boolean preSearchInfo) throws InternalErrorException {
        AppDataJobDto<LevelProductDto> ret = new AppDataJobDto<>();
        ret.setIdentifier(123);
        ret.setState(AppDataJobDtoState.GENERATING);
        ret.setPod("hostname");
        ret.setLevel(ApplicationLevel.L0);

        GenericMessageDto<LevelProductDto> message1 =
                new GenericMessageDto<LevelProductDto>(1, "input-key",
                        new LevelProductDto(
                                "S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE",
                                "S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE",
                                ProductFamily.L0_ACN, "NRT"));
        ret.setMessages(Arrays.asList(message1));

        Calendar start1 = Calendar.getInstance();
        start1.set(2017, Calendar.DECEMBER, 13, 14, 59, 48);
        Calendar stop1 = Calendar.getInstance();
        stop1.set(2017, Calendar.DECEMBER, 13, 15, 17, 25);
        AppDataJobProductDto product = new AppDataJobProductDto();
        product.setMissionId("S1");
        product.setProductName(
                "S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE");
        product.setSatelliteId("A");
        product.setStartTime("2017-12-13T12:16:23.000000Z");
        product.setStopTime("2017-12-13T12:16:56.000000Z");
        product.setAcquisition("IW");
        product.setProcessMode("NRT");
        if (preSearchInfo) {
            product.setProductType("IW_RAW__0S");
            product.setDataTakeId("021735");
            product.setInsConfId(6);
            product.setNumberSlice(3);
            product.setTotalNbOfSlice(10);
            product.setSegmentStartDate("2017-12-13T12:16:23.224083Z");
            product.setSegmentStopDate("2017-12-13T12:16:56.224083Z");
        }
        ret.setProduct(product);

        AppDataJobGenerationDto gen1 = new AppDataJobGenerationDto();
        gen1.setTaskTable("IW_RAW__0_GRDH_1.xml");
        gen1.setState(AppDataJobGenerationDtoState.INITIAL);
        AppDataJobGenerationDto gen2 = new AppDataJobGenerationDto();
        gen2.setTaskTable("IW_RAW__0_GRDM_1.xml");
        gen2.setState(AppDataJobGenerationDtoState.READY);
        AppDataJobGenerationDto gen3 = new AppDataJobGenerationDto();
        gen3.setTaskTable("IW_RAW__0_SLC__1.xml");
        gen3.setState(AppDataJobGenerationDtoState.PRIMARY_CHECK);
        AppDataJobGenerationDto gen4 = new AppDataJobGenerationDto();
        gen4.setTaskTable("IW_RAW__0_SLC__1_GRDH_1.xml");
        gen4.setState(AppDataJobGenerationDtoState.SENT);
        ret.setGenerations(Arrays.asList(gen1, gen2, gen3, gen4));

        return ret;
    }
}
