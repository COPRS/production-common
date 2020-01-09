package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class TestL0Utils {
    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	
    private static final String SEGMENT_PATTERN = "^([0-9a-z]{2})([0-9a-z]{1})_(([0-9a-z]{2})_RAW__0S([0-9a-z_]{2}))_"
    		+ "([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$";
    
    private static final String SLICE_PATTERN = "^([0-9a-z]{2})([0-9a-z]){1}_(([0-9a-z]{2})_RAW__0([0-9a-z_]{3}))_"
    		+ "([0-9a-z]{15})_([0-9a-z]{15})_([0-9a-z_]{6})_([0-9a-z_]{6})\\w{1,}\\.SAFE(/.*)?$";
    
    
    private static final String formatDate(final String date) {
       	return DateUtils.convertToAnotherFormat(date,
                TIME_FORMATTER,
                AppDataJobProduct.TIME_FORMATTER);
    }
    
    public static CatalogEvent newSegmentCatalogEvent(    		
    		final String productName, 
    		final String keyObs, 
    		final ProductFamily family,
    		final String mode
	) {
    	return newCatalogEvent(SEGMENT_PATTERN, productName, keyObs, family, mode);    			
    }
    
    public static CatalogEvent newSliceCatalogEvent(    		
    		final String productName, 
    		final String keyObs, 
    		final ProductFamily family,
    		final String mode
	) {
    	return newCatalogEvent(SLICE_PATTERN, productName, keyObs, family, mode);    			
    }
    
    static CatalogEvent newCatalogEvent(
    		final String lePattern,
    		final String productName, 
    		final String keyObs, 
    		final ProductFamily family,
    		final String mode
	) {
    	final Pattern pattern = Pattern.compile(lePattern, Pattern.CASE_INSENSITIVE);
    	
    	final Matcher m = pattern.matcher(productName);
    	if (!m.matches()) {
    		throw new RuntimeException("No worky worky! " + productName);
    	}    	
    	final Map<String,Object> map = new HashMap<>();

    	map.put("missionId", m.group(1));
    	map.put("satelliteId", m.group(2)); 
    	
    	final String type =  m.group(3) + "_RAW__0S";
       	map.put("swathtype", m.group(4));    	
    	map.put("polarisation", m.group(5));
      	map.put("startTime", formatDate(m.group(6)));
    	map.put("stopTime", formatDate(m.group(7)));
    	map.put("datatakeId", m.group(9));
    	map.put("processMode",  mode);
    	map.put("stationCode",  "WILE");
    	
    	final CatalogEvent event = new CatalogEvent();
    	event.setProductFamily(family);
    	event.setProductType(type);
    	event.setKeyObjectStorage(keyObs);
    	event.setMetadata(map);    	
    	return event;
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
    	final Map<String,Object> map = new HashMap<>();
    	map.put("channelId", String.valueOf(channelId));
    	map.put("missionId", missionId);
    	map.put("satelliteId", satelliteId);
    	map.put("stationCode", stationCode);
      	map.put("sessionId", sessionId);

    	final CatalogEvent event = new CatalogEvent();
    	event.setCreationDate(new Date(0L));
    	event.setProductFamily(ProductFamily.EDRS_SESSION);
    	event.setProductType(type.name());
    	event.setKeyObjectStorage(keyObs);
    	event.setMetadata(map);    	
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
        gen1.setCreationDate(new Date(0L));
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
