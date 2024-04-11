/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.common.errors.mqi;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiRouteNotAvailable extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8248616873024871315L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Category
     */
    private final ProductFamily family;

    /**
     * @param topic
     * @param productName
     * @param message
     * @param e
     */
    public MqiRouteNotAvailable(final ProductCategory category,
            final ProductFamily family) {
        super(ErrorCode.MQI_ROUTE_NOT_AVAILABLE,
                String.format("No route available"));
        this.category = category;
        this.family = family;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [family %s] [msg %s]", category,
                family, getMessage());
    }

}
