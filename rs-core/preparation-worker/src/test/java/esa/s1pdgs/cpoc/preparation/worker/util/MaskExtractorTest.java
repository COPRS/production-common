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

package esa.s1pdgs.cpoc.preparation.worker.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MaskExtractorTest {

	@Test
	public void extract() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		MaskExtractor e = new MaskExtractor();

		List<Map<String, Object>> extract = e
				.extract(new File("src/test/resources/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF"));
		Assert.assertEquals(175, extract.size());
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree("{\"name\":\"Costa Rica\",\"geometry\":{\"coordinates\":[[[-82.96578304719736,8.225027980985985],[-83.50843726269431,8.446926581247283],[-83.71147396516908,8.656836249216866],[-83.59631303580665,8.830443223501419],[-83.63264156770784,9.051385809765321],[-83.90988562695372,9.29080272057358],[-84.30340165885636,9.487354030795714],[-84.64764421256866,9.615537421095707],[-84.71335079622777,9.908051866083852],[-84.97566036654133,10.086723130733006],[-84.91137488477024,9.795991522658923],[-85.11092342806532,9.55703969974131],[-85.33948828809227,9.83454214114866],[-85.66078650586698,9.933347479690724],[-85.79744483106285,10.134885565629034],[-85.79170874707843,10.439337266476613],[-85.65931372754666,10.75433095951172],[-85.94172543002176,10.895278428587801],[-85.7125404528073,11.088444932494824],[-85.56185197624418,11.217119248901597],[-84.90300330273895,10.952303371621896],[-84.67306901725627,11.082657172078143],[-84.35593075228104,10.999225572142905],[-84.19017859570485,10.793450018756674],[-83.89505449088595,10.726839097532446],[-83.65561174186158,10.938764146361422],[-83.40231970898296,10.395438137244652],[-83.01567664257517,9.992982082555555],[-82.54619625520348,9.566134751824677],[-82.93289099804358,9.476812038608173],[-82.92715491405916,9.074330145702916],[-82.71918311230053,8.925708726431495],[-82.86865719270477,8.807266343618522],[-82.82977067740516,8.62629547773237],[-82.91317643912421,8.42351715741907],[-82.96578304719736,8.225027980985985]]],\"type\":\"Polygon\"}}");
		JsonNode actual = mapper.readTree(mapper.writeValueAsString(extract.get(2)));
		assertTrue(expected.equals(actual));
	}

}
