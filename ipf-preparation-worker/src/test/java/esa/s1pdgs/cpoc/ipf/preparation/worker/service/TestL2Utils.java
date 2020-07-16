package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteFrom;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteTo;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouting;

public class TestL2Utils {

    public static LevelProductsRouting buildL2Routing() {
        final LevelProductsRouting r = new LevelProductsRouting();
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("WV", "A"),
        		  new LevelProductsRouteTo(Arrays.asList("WV_RAW__0_OCN__2.xml"))));
        r.addRoute(new LevelProductsRoute(new LevelProductsRouteFrom("WV", "B"),
                new LevelProductsRouteTo(Arrays.asList("WV_RAW__0_OCN__2.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("EW", "A"),
//                new L2RouteTo(Arrays.asList("EW_RAW__0_GRDH_1.xml",
//                        "EW_RAW__0_GRDM_1.xml", "EW_RAW__0_SLC__1.xml",
//                        "EW_RAW__0_SLC__1_GRDH_1.xml",
//                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("EW", "B"),
//                new L2RouteTo(Arrays.asList("EW_RAW__0_SLC__1.xml",
//                        "EW_RAW__0_SLC__1_GRDH_1.xml",
//                        "EW_RAW__0_SLC__1_GRDM_1.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("IW", "A"),
//                new L2RouteTo(Arrays.asList("IW_RAW__0_GRDH_1.xml",
//                        "IW_RAW__0_GRDM_1.xml", "IW_RAW__0_SLC__1.xml",
//                        "IW_RAW__0_SLC__1_GRDH_1.xml",
//                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("IW", "B"),
//                new L2RouteTo(Arrays.asList("IW_RAW__0_SLC__1.xml",
//                        "IW_RAW__0_SLC__1_GRDH_1.xml",
//                        "IW_RAW__0_SLC__1_GRDM_1.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("S[1-6]", "A"),
//                new L2RouteTo(Arrays.asList("SM_RAW__0_GRDF_1.xml",
//                        "SM_RAW__0_GRDH_1.xml", "SM_RAW__0_GRDM_1.xml",
//                        "SM_RAW__0_SLC__1.xml", "SM_RAW__0_SLC__1_GRDF_1.xml",
//                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));
//        r.addRoute(new L2Route(new L2RouteFrom("S[1-6]", "B"),
//                new L2RouteTo(Arrays.asList("SM_RAW__0_SLC__1.xml",
//                        "SM_RAW__0_SLC__1_GRDF_1.xml",
//                        "SM_RAW__0_SLC__1_GRDH_1.xml"))));

        return r;
    }

    public static AppDataJob buildJobGeneration(
            final boolean preSearchInfo) throws InternalErrorException {
        final AppDataJob ret = new AppDataJob();
        ret.setId(123);
        ret.setState(AppDataJobState.GENERATING);
        ret.setPod("hostname");
        ret.setLevel(ApplicationLevel.L2);

        final CatalogEvent cat = new CatalogEvent();
        cat.setProductName("S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE");
        cat.setKeyObjectStorage("S1A_IW_RAW__0SDV_20171213T142312_20171213T142344_019685_02173E_07F5.SAFE");
        cat.setProductFamily(ProductFamily.L0_ACN);
        
        final GenericMessageDto<CatalogEvent> message1 =  new GenericMessageDto<CatalogEvent>(1, "input-key",cat);
        ret.setMessages(Arrays.asList(message1));

        final Calendar start1 = Calendar.getInstance();
        start1.set(2017, Calendar.DECEMBER, 13, 14, 59, 48);
        final Calendar stop1 = Calendar.getInstance();
        stop1.set(2017, Calendar.DECEMBER, 13, 15, 17, 25);
        final AppDataJobProduct product = new AppDataJobProduct();
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

        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("IW_RAW__0_GRDH_1.xml");
        gen1.setState(AppDataJobGenerationState.INITIAL);
        gen1.setCreationDate(new Date(0L));
        final AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("IW_RAW__0_GRDM_1.xml");
        gen2.setState(AppDataJobGenerationState.READY);
        gen2.setCreationDate(new Date(0L));
        final AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("IW_RAW__0_SLC__1.xml");
        gen3.setState(AppDataJobGenerationState.PRIMARY_CHECK);
        gen3.setCreationDate(new Date(0L));
        final AppDataJobGeneration gen4 = new AppDataJobGeneration();
        gen4.setTaskTable("IW_RAW__0_SLC__1_GRDH_1.xml");
        gen4.setState(AppDataJobGenerationState.SENT);
        gen4.setCreationDate(new Date(0L));
        ret.setGeneration(gen1);

        return ret;
    }
}
