package esa.s1pdgs.cpoc.mqi.server.test;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.GenericKafkaUtils;

public class DataUtils {

    public static AppCatMessageDto<ProductionEvent> getLightMessage1() {
        return new AppCatMessageDto<>(ProductCategory.AUXILIARY_FILES, 11110,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES, 1, 1234);
    }

    public static AppCatMessageDto<ProductionEvent> getLightMessage2() {
        return new AppCatMessageDto<>(ProductCategory.AUXILIARY_FILES, 11111,
                GenericKafkaUtils.TOPIC_AUXILIARY_FILES, 1, 1235);
    }
}
