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

package standalone.prip.frontend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class PripTestDataGenerator {

    private final static int num = 10000;
	
	private static Random random = new Random();

	public static void main(String[] args) throws IOException {
		
		for (int j= 0; j < 200; j++) {

			File f = new File("/tmp","generated-"+j+".json");
	
			FileWriter writer = new FileWriter(f);
	
			for (int i = 0; i < num; i++) {
	
				String id = "00000000-0000-0000-0000-"+(j+i+1);
				String name = "S2B_OPER_MSI_L1C_TL_MPS__20191001T112642_A013418_T34UGA_N02."+(j+i+1);
				String firstLon = nextLon();
				String firstLat = nextLat();
				writer.write("{ \"index\" : { \"_index\" : \"prip\" } }\n");
				writer.write("{\"id\":\""+id+"\",\"obsKey\":\""+name+"\",\"name\":\""+name+"\",\"productFamily\":\"L2_ZIP\",\"contentType\":\"application/zip\",\"contentLength\":1000000,\"contentDateStart\":\"2021-06-06T10:00:00.000000Z\",\"contentDateEnd\":\"2021-06-06T11:00:00.000000Z\",\"creationDate\":\"2021-06-06T11:00:01.654321Z\",\"evictionDate\":\"2021-06-13T11:00:01.654321Z\",\"checksum\":[{\"algorithm\":\"MD5\",\"value\":\"d84d1fb66546a3d6d72c66571cc58540\",\"checksum_date\":\"2021-06-06T11:00:01.777Z\"}],\"footprint\":{\"type\":\"polygon\",\"coordinates\":[[["+firstLon+","+firstLat+"],["+nextLon()+","+nextLat()+"],["+nextLon()+","+nextLat()+"],["+nextLon()+","+nextLat()+"],["+firstLon+","+firstLat+"]]]}}\n");
			}
			writer.close();
		
		}

	}

	private static String nextLon() {

		return String.format("%.4f", ((random.nextInt(2) << 1) - 1) * random.nextDouble() * 180).replace(',', '.');
	}

	private static  String nextLat() {

		return String.format("%.4f", ((random.nextInt(2) << 1) - 1) * random.nextDouble() * 90).replace(',', '.');
	}

}