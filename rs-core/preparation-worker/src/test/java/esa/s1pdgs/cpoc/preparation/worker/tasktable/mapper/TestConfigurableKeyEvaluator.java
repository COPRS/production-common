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

package esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public class TestConfigurableKeyEvaluator {

	@Test
	public final void testSentinel1DefaultPattern() {
		final ConfigurableKeyEvaluator uut = new ConfigurableKeyEvaluator("$(product.swathtype)_$(product.satelliteId)");
		final AppDataJobProduct prod = new AppDataJobProduct();
		prod.getMetadata().put("foo", "baaaaar");
		prod.getMetadata().put("swathtype", "WV");
		prod.getMetadata().put("satelliteId", "B");		
		assertEquals("WV_B", uut.apply(prod));
	}
}
