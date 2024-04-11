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

package esa.s1pdgs.cpoc.prip.frontend.processor.visitor;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;

public class ProductsFilterVisitorTest {

	@Test
	public void testConvertToLocalDateTime() throws ExpressionVisitException {
		final String datetime0 = "2012-01-01T00:00:00Z";
		LocalDateTime result0 = ProductsFilterVisitor.convertToLocalDateTime(datetime0);
		assertEquals(LocalDateTime.ofInstant(Instant.parse(datetime0), ZoneOffset.UTC), result0);		

		final String datetime1 = "2012-01-01T00:00:00.1Z";
		LocalDateTime result1 = ProductsFilterVisitor.convertToLocalDateTime(datetime1);
		assertEquals(LocalDateTime.ofInstant(Instant.parse(datetime1), ZoneOffset.UTC), result1);		

		final String datetime2 = "2012-01-01T00:00:00.12Z";
		LocalDateTime result2 = ProductsFilterVisitor.convertToLocalDateTime(datetime2);
		assertEquals(LocalDateTime.ofInstant(Instant.parse(datetime2), ZoneOffset.UTC), result2);		

		final String datetime3 = "2012-01-01T00:00:00.123Z";		
		LocalDateTime result3 = ProductsFilterVisitor.convertToLocalDateTime(datetime3);
		assertEquals(LocalDateTime.ofInstant(Instant.parse(datetime3), ZoneOffset.UTC), result3);
		assertEquals(DateUtils.parse(datetime3), result3); // reference value from previous calculation method		
	}

}
