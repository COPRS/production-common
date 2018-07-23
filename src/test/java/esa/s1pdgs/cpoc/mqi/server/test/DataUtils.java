package esa.s1pdgs.cpoc.mqi.server.test;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;

public class DataUtils {

    public static MqiLightMessageDto getLightMessage1() {
        return new MqiLightMessageDto(ProductCategory.AUXILIARY_FILES, 11110,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES, 1, 1234);
    }

    public static MqiLightMessageDto getLightMessage2() {
        return new MqiLightMessageDto(ProductCategory.AUXILIARY_FILES, 11111,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES, 1, 1235);
    }
}
